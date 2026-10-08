package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.response.AuthResponse;
import fptu.sba301.ats.entity.RefreshToken;
import fptu.sba301.ats.entity.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeToken(String token);

    void revokeAllUserTokens(User user);

    void rotateToken(RefreshToken oldToken);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
