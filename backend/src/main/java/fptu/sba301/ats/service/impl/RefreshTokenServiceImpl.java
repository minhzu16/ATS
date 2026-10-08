package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.response.AuthResponse;
import fptu.sba301.ats.entity.RefreshToken;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.repository.RefreshTokenRepository;
import fptu.sba301.ats.security.JwtService;
import fptu.sba301.ats.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenDurationMs;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new fptu.sba301.ats.exception.BusinessException("Refresh token not found", org.springframework.http.HttpStatus.UNAUTHORIZED));

        if (refreshToken.isRevoked()) {
            // Token reuse detection: an already revoked token was presented!
            // Revoke all tokens for this user to protect against session hijacking.
            if (refreshToken.getUser() != null) {
                revokeAllUserTokens(refreshToken.getUser());
            }
            throw new fptu.sba301.ats.exception.BusinessException("Refresh token revoked: token reuse detected", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new fptu.sba301.ats.exception.BusinessException("Refresh token expired", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        return refreshToken;
    }

    @Override
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Override
    public void revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    @Override
    public void rotateToken(RefreshToken oldToken) {
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
    }

    @Override
    public AuthResponse refresh(String refreshTokenStr) {
        RefreshToken oldToken = verifyRefreshToken(refreshTokenStr);
        User user = oldToken.getUser();

        if (user == null || user.isDeleted() || !user.isActive() || user.isAccountLocked()) {
            throw new fptu.sba301.ats.exception.BusinessException("User account is inactive, locked, or deleted", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }

        // Rotate token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = createRefreshToken(user);
        String newAccessToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newToken.getToken())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .avatarUrl(user.getAvatarURL())
                .build();
    }

    @Override
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }
}

