package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.LoginRequest;
import fptu.sba301.ats.dto.response.AuthResponse;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.security.JwtService;
import fptu.sba301.ats.service.AuthService;
import fptu.sba301.ats.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse login(LoginRequest request) {

        // Authenticate — throws BadCredentialsException / DisabledException / LockedException on failure
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Load user
        User user = userRepository
                .findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow();

        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .avatarUrl(user.getAvatarURL())
                .build();
    }
}

