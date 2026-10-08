package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.ForgotPasswordRequest;
import fptu.sba301.ats.dto.request.LoginRequest;
import fptu.sba301.ats.dto.request.RefreshTokenRequest;
import fptu.sba301.ats.dto.request.ResetPasswordRequest;
import fptu.sba301.ats.dto.request.SetPasswordRequest;
import fptu.sba301.ats.dto.response.AuthResponse;
import fptu.sba301.ats.service.AuthService;
import fptu.sba301.ats.service.RefreshTokenService;
import fptu.sba301.ats.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static fptu.sba301.ats.constant.AppConstant.AUTH_CONTROLLER_URL;
import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + AUTH_CONTROLLER_URL)
@RequiredArgsConstructor
public class AuthController {
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @fptu.sba301.ats.annotation.LogAudit(action = "USER_LOGIN", resource = "AUTH")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @fptu.sba301.ats.annotation.LogAudit(action = "USER_LOGOUT", resource = "AUTH")
    public ResponseEntity<Map<String, String>> logout(
            @RequestBody RefreshTokenRequest request) {
        refreshTokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                refreshTokenService.refresh(request.getRefreshToken())
        );
    }

    /**
     * Forgot password — sends a reset link to the user's registered email.
     * Always returns a generic 200 regardless of whether the email exists (security).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "If an account with that email exists, a password reset link has been sent."
        ));
    }

    /**
     * Reset password via link from email (/reset-password?token=...).
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully."));
    }

    /**
     * Account activation — new user sets their password via the link from the activation email.
     * (/activate?token=...)
     */
    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(
            @Valid @RequestBody SetPasswordRequest request) {
        userService.activateAccount(request);
        return ResponseEntity.ok(Map.of("message", "Account activated successfully. You can now log in."));
    }
}
