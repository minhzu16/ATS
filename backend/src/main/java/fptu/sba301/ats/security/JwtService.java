package fptu.sba301.ats.security;

import fptu.sba301.ats.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static fptu.sba301.ats.constant.AuthConstants.EMAIL_KEY;
import static fptu.sba301.ats.constant.AuthConstants.ROLE_KEY;

@Service
@Log4j2
public class JwtService {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    private Key getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateToken(User user) {
        try {
            Instant now = Instant.now();
            Instant expiry = now.plusMillis(accessTokenExpirationMs);

            return Jwts.builder()
                    .setIssuer("ats-system")
                    .setSubject(user.getEmail())
                    .claim(ROLE_KEY, user.getRole())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiry))
                    .signWith(getSignKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token for user {}: {}",
                    user.getEmail(), e.getMessage());
            throw new JwtException(e.getMessage(), e);
        }
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .requireIssuer("ats-system")
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
