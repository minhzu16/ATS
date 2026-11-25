package fptu.sba301.ats.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int WINDOW_SECONDS = 60;
    private static final int MAX_MAP_SIZE = 10_000;
    private static final String RATE_LIMIT_RESPONSE_BODY =
            "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many authentication attempts, try again later\"}";
    private static final Set<String> TARGET_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/activate"
    );

    record Window(Instant windowStart, int count) {}

    private final Clock clock;
    private final int perMinuteLimit;
    final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            Clock clock,
            @Value("${app.security.auth-rate-limit.per-minute:10}") int perMinuteLimit
    ) {
        this.clock = clock;
        this.perMinuteLimit = perMinuteLimit;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path == null) {
            path = request.getServletPath();
        }
        if (path == null) {
            return true;
        }

        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return !TARGET_PATHS.contains(path);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Instant now = clock.instant();

        if (windows.size() > MAX_MAP_SIZE) {
            windows.entrySet().removeIf(entry ->
                    !now.isBefore(entry.getValue().windowStart().plusSeconds(WINDOW_SECONDS)));
        }

        String clientKey = request.getRemoteAddr();
        if (clientKey == null) {
            clientKey = "";
        }

        Window updated = windows.compute(clientKey, (key, existing) -> {
            if (existing == null
                    || now.isBefore(existing.windowStart())
                    || !now.isBefore(existing.windowStart().plusSeconds(WINDOW_SECONDS))) {
                return new Window(now, 1);
            }
            return new Window(existing.windowStart(), Math.min(existing.count() + 1, perMinuteLimit + 1));
        });

        if (updated.count() > perMinuteLimit) {
            long secondsRemaining = Duration.between(now, updated.windowStart().plusSeconds(WINDOW_SECONDS)).toSeconds();
            long retryAfter = Math.max(1L, secondsRemaining);

            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(RATE_LIMIT_RESPONSE_BODY);
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
