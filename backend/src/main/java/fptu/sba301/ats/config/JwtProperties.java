package fptu.sba301.ats.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret key is required and cannot be blank")
        @Size(min = 64, message = "JWT secret key must be at least 64 characters long")
        String secretKey,

        @Positive(message = "Access token expiration must be a positive number")
        long accessTokenExpirationMs,

        @Positive(message = "Refresh token expiration must be a positive number")
        long refreshTokenExpirationMs
) {
}
