package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.dto.request.ChangePasswordRequest;
import fptu.sba301.ats.dto.request.CreateUserRequest;
import fptu.sba301.ats.dto.request.UpdateUserRequest;
import fptu.sba301.ats.dto.response.UserResponse;
import fptu.sba301.ats.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;
import static fptu.sba301.ats.constant.AppConstant.USER_CONTROLLER_URL;

@RestController
@RequestMapping(BASE_URL + USER_CONTROLLER_URL)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── Current user ──────────────────────────────────────────────────────────

    /**
     * Returns the profile of the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUserByEmail(userDetails.getUsername()));
    }

    /**
     * Change password of the currently authenticated user.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    /**
     * Upload avatar for the currently authenticated user.
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    /**
     * List all non-deleted users. Admin only.
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsers(departmentId, page, size));
    }

    /**
     * Get a specific user by ID. Admin only.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Create a new user account. Admin only.
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "CREATE_USER", resource = "User")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    /**
     * Update user info (name, role, department). Admin only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "UPDATE_USER", resource = "User")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Lock a user account. Admin only.
     */
    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "LOCK_USER", resource = "User")
    public ResponseEntity<UserResponse> lockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.lockUser(id));
    }

    /**
     * Unlock a user account. Admin only.
     */
    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "UNLOCK_USER", resource = "User")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.unlockUser(id));
    }

    /**
     * Soft-delete a user. Admin only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "DELETE_USER", resource = "User")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resend activation invitation email. Admin only.
     */
    @PostMapping("/{id}/resend-invitation")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "RESEND_INVITATION", resource = "User")
    public ResponseEntity<Void> resendInvitation(@PathVariable UUID id) {
        userService.resendInvitation(id);
        return ResponseEntity.ok().build();
    }
}

