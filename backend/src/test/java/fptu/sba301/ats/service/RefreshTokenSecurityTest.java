package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.RefreshToken;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.RefreshTokenRepository;
import fptu.sba301.ats.security.JwtService;
import fptu.sba301.ats.service.impl.RefreshTokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSecurityTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@ats.com")
                .role(Role.HR)
                .active(true)
                .accountLocked(false)
                .deleted(false)
                .build();
    }

    @Test
    void testVerifyRefreshToken_RevokedToken_TriggersTokenReuseDetection() {
        RefreshToken revokedToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("compromised-token")
                .user(user)
                .revoked(true)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("compromised-token")).thenReturn(Optional.of(revokedToken));
        when(refreshTokenRepository.findAllByUserAndRevokedFalse(user)).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                refreshTokenService.verifyRefreshToken("compromised-token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
        assertTrue(ex.getMessage().contains("token reuse detected"));
        // Verifies that all tokens for the user were revoked
        verify(refreshTokenRepository, times(1)).findAllByUserAndRevokedFalse(user);
    }

    @Test
    void testRefresh_InactiveUser_ThrowsUnauthorized() {
        user.setActive(false);

        RefreshToken validToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("valid-token")
                .user(user)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(validToken));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                refreshTokenService.refresh("valid-token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void testVerifyRefreshToken_ExpiredToken_ThrowsUnauthorized() {
        RefreshToken expiredToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("expired-token")
                .user(user)
                .revoked(false)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                refreshTokenService.verifyRefreshToken("expired-token")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
