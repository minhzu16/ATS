package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.ChangePasswordRequest;
import fptu.sba301.ats.dto.request.CreateUserRequest;
import fptu.sba301.ats.dto.request.ForgotPasswordRequest;
import fptu.sba301.ats.dto.request.ResetPasswordRequest;
import fptu.sba301.ats.dto.request.SetPasswordRequest;
import fptu.sba301.ats.dto.request.UpdateUserRequest;
import fptu.sba301.ats.dto.response.UserResponse;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.EmailService;
import fptu.sba301.ats.service.UserService;
import fptu.sba301.ats.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;
    private final fptu.sba301.ats.service.RefreshTokenService refreshTokenService;

    // ── Current user ──────────────────────────────────────────────────────────

    @Override
    public UserResponse getCurrentUserByEmail(String email) {
        User user = findActiveUserByEmail(email);
        return toResponse(user);
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findActiveUserByEmail(email);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException("New password must be different from current password", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Security: Revoke all existing refresh tokens when password changes
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    public String uploadAvatar(String email, MultipartFile file) {
        fptu.sba301.ats.util.FileValidator.validateAvatar(file);
        User user = findActiveUserByEmail(email);
        Map<String, Object> result = cloudinaryService.uploadFile(file, "avatars");
        String avatarUrl = (String) result.get("secure_url");
        user.setAvatarURL(avatarUrl);
        userRepository.save(user);
        return avatarUrl;
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @Override
    public Page<UserResponse> getAllUsers(UUID departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;
        if (departmentId != null) {
            userPage = userRepository.findByDeletedFalseAndDepartmentId(departmentId, pageable);
        } else {
            userPage = userRepository.findByDeletedFalse(pageable);
        }
        return userPage.map(this::toResponse);
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
        return toResponse(user);
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new BusinessException("Email already in use: " + request.getEmail(), HttpStatus.CONFLICT);
        }

        Department department = resolveDepartment(request.getDepartmentId());

        // Generate activation token — valid 24 hours
        String activationToken = UUID.randomUUID().toString().replace("-", "");

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .passwordHash(null)          // No password until user activates
                .role(request.getRole())
                .department(department)
                .active(false)               // Not active until account is activated
                .deleted(false)
                .accountLocked(false)
                .activationToken(activationToken)
                .activationTokenExpiresAt(Instant.now().plusSeconds(86400)) // 24h
                .build();

        user = userRepository.save(user);

        // Send activation email asynchronously
        emailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationToken);

        return toResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        boolean roleChanged = request.getRole() != null && !request.getRole().equals(user.getRole());
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getDepartmentId() != null) {
            Department department = resolveDepartment(request.getDepartmentId());
            user.setDepartment(department);
        }

        userRepository.save(user);
        if (roleChanged) {
            refreshTokenService.revokeAllUserTokens(user);
        }
        return toResponse(user);
    }

    @Override
    public UserResponse lockUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (user.isAccountLocked()) {
            throw new BusinessException("Account is already locked", HttpStatus.BAD_REQUEST);
        }

        user.setAccountLocked(true);
        userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(user);
        return toResponse(user);
    }

    @Override
    public UserResponse unlockUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (!user.isAccountLocked()) {
            throw new BusinessException("Account is not locked", HttpStatus.BAD_REQUEST);
        }

        user.setAccountLocked(false);
        userRepository.save(user);
        return toResponse(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        user.setDeleted(true);
        user.setActive(false);
        userRepository.save(user);
        refreshTokenService.revokeAllUserTokens(user);
    }

    // ── Password Flows ────────────────────────────────────────────────────────

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // Do NOT reveal whether the email exists
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail()).orElse(null);

        if (user == null || !user.isActive() || user.isAccountLocked()) {
            // Silently return — security: don't leak account existence
            return;
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        user.setResetToken(token);
        user.setResetTokenExpiresAt(Instant.now().plusSeconds(900)); // 15 minutes
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), token);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("New password and confirm password do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByResetTokenAndDeletedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset token", HttpStatus.BAD_REQUEST));

        if (user.getResetTokenExpiresAt() == null || Instant.now().isAfter(user.getResetTokenExpiresAt())) {
            throw new BusinessException("Reset token has expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);

        // Security: Revoke all refresh tokens after password reset
        refreshTokenService.revokeAllUserTokens(user);
    }

    @Override
    public void activateAccount(SetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        User user = userRepository.findByActivationTokenAndDeletedFalse(request.getToken())
                .orElseThrow(() -> new BusinessException("Invalid or expired activation token", HttpStatus.BAD_REQUEST));

        if (user.getActivationTokenExpiresAt() == null || Instant.now().isAfter(user.getActivationTokenExpiresAt())) {
            throw new BusinessException("Activation link has expired. Please ask an admin to resend the invitation.", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);
        userRepository.save(user);
    }

    @Override
    public void resendInvitation(UUID id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        if (user.isActive() && user.getPasswordHash() != null) {
            throw new BusinessException("User account is already activated", HttpStatus.BAD_REQUEST);
        }

        String activationToken = UUID.randomUUID().toString().replace("-", "");
        user.setActivationToken(activationToken);
        user.setActivationTokenExpiresAt(Instant.now().plusSeconds(86400)); // 24h
        userRepository.save(user);

        emailService.sendActivationEmail(user.getEmail(), user.getFullName(), activationToken);
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));
    }

    private Department resolveDepartment(UUID departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(
                        "Department not found: " + departmentId, HttpStatus.NOT_FOUND));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .accountLocked(user.isAccountLocked())
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .avatarUrl(user.getAvatarURL())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
