package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.ChangePasswordRequest;
import fptu.sba301.ats.dto.request.CreateUserRequest;
import fptu.sba301.ats.dto.request.ForgotPasswordRequest;
import fptu.sba301.ats.dto.request.ResetPasswordRequest;
import fptu.sba301.ats.dto.request.SetPasswordRequest;
import fptu.sba301.ats.dto.request.UpdateUserRequest;
import fptu.sba301.ats.dto.response.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserService {
    // Current user
    UserResponse getCurrentUserByEmail(String email);
    void changePassword(String email, ChangePasswordRequest request);
    String uploadAvatar(String email, MultipartFile file);

    // Admin - CRUD
    Page<UserResponse> getAllUsers(UUID departmentId, int page, int size);
    UserResponse getUserById(UUID id);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    UserResponse lockUser(UUID id);
    UserResponse unlockUser(UUID id);
    void deleteUser(UUID id);

    // Password reset
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);

    // Account activation
    void activateAccount(SetPasswordRequest request);
    void resendInvitation(UUID id);
}
