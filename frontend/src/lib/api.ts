import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8386/api/v1";

const api = axios.create({
    baseURL: BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

// ── Request interceptor: attach access token ──────────────────────────────
// Support both localStorage (remember-me) and sessionStorage (session-only)
function getToken(key: string): string | null {
    return localStorage.getItem(key) || sessionStorage.getItem(key);
}

api.interceptors.request.use(
    (config) => {
        const token = getToken("accessToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// ── Response interceptor: handle 401 → try refresh → retry ───────────────
let isRefreshing = false;
let failedQueue: Array<{
    resolve: (value: string) => void;
    reject: (reason?: unknown) => void;
}> = [];

function processQueue(error: unknown, token: string | null = null) {
    failedQueue.forEach(({ resolve, reject }) => {
        if (error) {
            reject(error);
        } else {
            resolve(token as string);
        }
    });
    failedQueue = [];
}

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        const isLoginEndpoint = originalRequest?.url?.includes("/auth/login");
        const isRefreshEndpoint = originalRequest?.url?.includes("/auth/refresh");

        if (
            error.response?.status === 401 &&
            !originalRequest._retry &&
            !isLoginEndpoint &&
            !isRefreshEndpoint
        ) {
            if (isRefreshing) {
                return new Promise<string>((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers.Authorization = `Bearer ${token}`;
                        return api(originalRequest);
                    })
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = getToken("refreshToken");

            if (!refreshToken) {
                isRefreshing = false;
                clearSession();
                window.location.href = "/login";
                return Promise.reject(error);
            }

            try {
                const { data } = await axios.post(`${BASE_URL}/auth/refresh`, {
                    refreshToken,
                });

                const newAccessToken: string = data.accessToken;
                const newRefreshToken: string = data.refreshToken;

                // Respect original storage type (sessionStorage vs localStorage)
                const storageToUse = localStorage.getItem("refreshToken") ? localStorage : sessionStorage;
                storageToUse.setItem("accessToken", newAccessToken);
                storageToUse.setItem("refreshToken", newRefreshToken);

                // Keep stored user in sync with refresh payload (department, avatar, etc.)
                const raw = storageToUse.getItem("user");
                if (raw) {
                    try {
                        const prev = JSON.parse(raw) as Record<string, unknown>;
                        const merged = {
                            ...prev,
                            id: data.userId ?? prev.id,
                            fullName: data.fullName ?? prev.fullName,
                            email: data.email ?? prev.email,
                            role: data.role ?? prev.role,
                            ...(data.department !== undefined && { department: data.department }),
                            ...(data.avatarUrl !== undefined && { avatarUrl: data.avatarUrl }),
                        };
                        storageToUse.setItem("user", JSON.stringify(merged));
                    } catch {
                        /* ignore */
                    }
                }

                api.defaults.headers.common.Authorization = `Bearer ${newAccessToken}`;
                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

                processQueue(null, newAccessToken);
                return api(originalRequest);
            } catch (refreshError) {
                processQueue(refreshError, null);
                clearSession();
                window.location.href = "/login";
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

function clearSession() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("refreshToken");
    sessionStorage.removeItem("user");
}

export default api;
