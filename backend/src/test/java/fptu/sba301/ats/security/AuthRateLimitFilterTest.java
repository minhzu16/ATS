package fptu.sba301.ats.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AuthRateLimitFilterTest {

    private MutableClock clock;
    private AuthRateLimitFilter filter;
    private static final Instant START_TIME = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START_TIME, ZoneOffset.UTC);
        filter = new AuthRateLimitFilter(clock, 10);
    }

    @Test
    @DisplayName("10 logins pass and the 11th gets 429 with Retry-After and exact error body")
    void tenLoginsPass_andEleventhGets429() throws Exception {
        String ip = "192.168.1.100";

        for (int i = 1; i <= 10; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            request.setRemoteAddr(ip);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(request, response, chain);

            assertEquals(200, response.getStatus(), "Request " + i + " should succeed");
            assertNotNull(chain.getRequest(), "Chain should be invoked for request " + i);
        }

        // 11th login attempt
        MockHttpServletRequest request11 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request11.setRemoteAddr(ip);
        MockHttpServletResponse response11 = new MockHttpServletResponse();
        MockFilterChain chain11 = new MockFilterChain();

        filter.doFilter(request11, response11, chain11);

        assertEquals(429, response11.getStatus(), "11th request should be rate-limited with status 429");
        assertNull(chain11.getRequest(), "Chain should not be called when rate-limited");
        String contentType = response11.getContentType();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));

        String retryAfterHeader = response11.getHeader("Retry-After");
        assertNotNull(retryAfterHeader, "Retry-After header must be present");
        int retryAfter = Integer.parseInt(retryAfterHeader);
        assertTrue(retryAfter >= 1 && retryAfter <= 60, "Retry-After should be between 1 and 60 seconds");

        String expectedBody = "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many authentication attempts, try again later\"}";
        assertEquals(expectedBody, response11.getContentAsString());
    }

    @Test
    @DisplayName("Another IP is not affected when one IP is rate limited")
    void anotherIpIsNotAffected() throws Exception {
        String ipA = "10.0.0.1";
        String ipB = "10.0.0.2";

        // Exhaust limit for IP A
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(ipA);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
        }

        // IP A 11th request is blocked
        MockHttpServletRequest reqA11 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        reqA11.setRemoteAddr(ipA);
        MockHttpServletResponse resA11 = new MockHttpServletResponse();
        MockFilterChain chainA11 = new MockFilterChain();
        filter.doFilter(reqA11, resA11, chainA11);
        assertEquals(429, resA11.getStatus());

        // IP B makes a request - must pass
        MockHttpServletRequest reqB = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        reqB.setRemoteAddr(ipB);
        MockHttpServletResponse resB = new MockHttpServletResponse();
        MockFilterChain chainB = new MockFilterChain();
        filter.doFilter(reqB, resB, chainB);

        assertEquals(200, resB.getStatus());
        assertNotNull(chainB.getRequest());
    }

    @Test
    @DisplayName("After 60 s the same IP passes again")
    void after60sSameIpPassesAgain() throws Exception {
        String ip = "172.16.0.5";

        // Exhaust limit
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
        }

        // Blocked at t = 0
        MockHttpServletRequest req11 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req11.setRemoteAddr(ip);
        MockHttpServletResponse res11 = new MockHttpServletResponse();
        MockFilterChain chain11 = new MockFilterChain();
        filter.doFilter(req11, res11, chain11);
        assertEquals(429, res11.getStatus());

        // Advance clock by 60 seconds
        clock.advance(Duration.ofSeconds(60));

        // 12th request at t = 60s should succeed as new window starts
        MockHttpServletRequest req12 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req12.setRemoteAddr(ip);
        MockHttpServletResponse res12 = new MockHttpServletResponse();
        MockFilterChain chain12 = new MockFilterChain();
        filter.doFilter(req12, res12, chain12);

        assertEquals(200, res12.getStatus(), "After 60s, request should succeed");
        assertNotNull(chain12.getRequest(), "Chain should be called after window resets");
    }

    @Test
    @DisplayName("GET /api/v1/auth/login and POST /api/v1/candidates are never limited")
    void nonTargetMethodsAndPathsNeverLimited() throws Exception {
        String ip = "192.168.1.50";

        // 15 GET requests to /api/v1/auth/login
        for (int i = 0; i < 15; i++) {
            MockHttpServletRequest getReq = new MockHttpServletRequest("GET", "/api/v1/auth/login");
            getReq.setRemoteAddr(ip);
            MockHttpServletResponse getRes = new MockHttpServletResponse();
            MockFilterChain getChain = new MockFilterChain();

            filter.doFilter(getReq, getRes, getChain);

            assertEquals(200, getRes.getStatus());
            assertNotNull(getChain.getRequest());
        }

        // 15 POST requests to /api/v1/candidates
        for (int i = 0; i < 15; i++) {
            MockHttpServletRequest postTxnReq = new MockHttpServletRequest("POST", "/api/v1/candidates");
            postTxnReq.setRemoteAddr(ip);
            MockHttpServletResponse postTxnRes = new MockHttpServletResponse();
            MockFilterChain postTxnChain = new MockFilterChain();

            filter.doFilter(postTxnReq, postTxnRes, postTxnChain);

            assertEquals(200, postTxnRes.getStatus());
            assertNotNull(postTxnChain.getRequest());
        }
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh and /api/v1/auth/forgot-password are rate limited")
    void refreshAndForgotPasswordEndpointsAreRateLimited() throws Exception {
        String ipRefresh = "10.1.1.1";
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
            req.setRemoteAddr(ipRefresh);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }
        MockHttpServletRequest ref11 = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        ref11.setRemoteAddr(ipRefresh);
        MockHttpServletResponse resRef11 = new MockHttpServletResponse();
        MockFilterChain chainRef11 = new MockFilterChain();
        filter.doFilter(ref11, resRef11, chainRef11);
        assertEquals(429, resRef11.getStatus());

        String ipForgot = "10.1.1.2";
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/forgot-password");
            req.setRemoteAddr(ipForgot);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }
        MockHttpServletRequest forgot11 = new MockHttpServletRequest("POST", "/api/v1/auth/forgot-password");
        forgot11.setRemoteAddr(ipForgot);
        MockHttpServletResponse resForgot11 = new MockHttpServletResponse();
        MockFilterChain chainForgot11 = new MockFilterChain();
        filter.doFilter(forgot11, resForgot11, chainForgot11);
        assertEquals(429, resForgot11.getStatus());
    }

    @Test
    @DisplayName("Retry-After accurately reflects remaining seconds until window reset")
    void retryAfterReflectsRemainingSeconds() throws Exception {
        String ip = "10.2.2.2";
        // 10 requests at t = 0
        for (int i = 0; i < 10; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
        }

        // Advance 25 seconds
        clock.advance(Duration.ofSeconds(25));

        // 11th request at t = 25s
        MockHttpServletRequest req11 = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req11.setRemoteAddr(ip);
        MockHttpServletResponse res11 = new MockHttpServletResponse();
        MockFilterChain chain11 = new MockFilterChain();
        filter.doFilter(req11, res11, chain11);

        assertEquals(429, res11.getStatus());
        assertEquals("35", res11.getHeader("Retry-After"));
    }

    @Test
    @DisplayName("When map exceeds 10_000 keys, expired entries are cleaned up")
    void mapExceeding10000KeysDropsExpiredEntries() throws Exception {
        Instant oldTime = START_TIME.minus(Duration.ofSeconds(120));
        for (int i = 0; i < 10_005; i++) {
            filter.windows.put("expired-ip-" + i, new AuthRateLimitFilter.Window(oldTime, 1));
        }
        assertTrue(filter.windows.size() > 10_000);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        req.setRemoteAddr("new-ip-1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals(200, res.getStatus());
        // All 10,005 old expired entries should have been dropped, leaving only new-ip-1
        assertEquals(1, filter.windows.size());
    }

    private static class MutableClock extends Clock {
        private final ZoneId zone;
        private final AtomicReference<Instant> currentInstant;

        MutableClock(Instant initialInstant, ZoneId zone) {
            this.currentInstant = new AtomicReference<>(initialInstant);
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(currentInstant.get(), zone);
        }

        @Override
        public Instant instant() {
            return currentInstant.get();
        }

        void advance(Duration duration) {
            currentInstant.updateAndGet(prev -> prev.plus(duration));
        }
    }
}
