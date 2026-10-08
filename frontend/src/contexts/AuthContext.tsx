import {
    createContext,
    useState,
    useEffect,
    useCallback,
    type ReactNode,
} from "react";
import type { AuthContextType, LoginCredentials, User } from "@/types/auth";
import { authService } from "@/services/authService";

export const AuthContext = createContext<AuthContextType | null>(null);

// Tokens can be in either localStorage (remember me) or sessionStorage (session only)
function getStoredItem(key: string): string | null {
    return localStorage.getItem(key) || sessionStorage.getItem(key);
}

function clearSession() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    sessionStorage.removeItem("accessToken");
    sessionStorage.removeItem("refreshToken");
    sessionStorage.removeItem("user");
}

function buildUserFromStorage(): User | null {
    try {
        const raw = getStoredItem("user");
        if (!raw) return null;
        const parsed = JSON.parse(raw) as
            | (Partial<User> & { name?: string; full_name?: string })
            | null;
        if (!parsed) return null;

        const fullName =
            parsed.fullName?.trim() ||
            parsed.name?.trim() ||
            parsed.full_name?.trim() ||
            "";

        if (!parsed.id || !parsed.email || !parsed.role || !fullName) {
            return null;
        }

        return {
            id: parsed.id,
            email: parsed.email,
            role: parsed.role,
            fullName,
            department: parsed.department,
            avatarUrl: parsed.avatarUrl,
        };
    } catch {
        return null;
    }
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<User | null>(buildUserFromStorage);
    const [isLoading, setIsLoading] = useState(false);

    // Restore session on mount: if token exists but user missing, clear stale data
    useEffect(() => {
        const accessToken = getStoredItem("accessToken");
        const storedUser = buildUserFromStorage();
        if (!accessToken || !storedUser) {
            clearSession();
            setUser(null);
        }
    }, []);

    const login = useCallback(
        async (credentials: LoginCredentials, rememberMe = false): Promise<User> => {
            setIsLoading(true);
            try {
                const response = await authService.login(
                    credentials.email,
                    credentials.password
                );

                const userData: User = {
                    id: response.userId,
                    fullName: response.fullName,
                    email: response.email,
                    role: response.role,
                    department: response.department,
                    avatarUrl: response.avatarUrl,
                };

                const storage = rememberMe ? localStorage : sessionStorage;
                storage.setItem("accessToken", response.accessToken);
                storage.setItem("refreshToken", response.refreshToken);
                storage.setItem("user", JSON.stringify(userData));

                setUser(userData);
                return userData;
            } finally {
                setIsLoading(false);
            }
        },
        []
    );

    const logout = useCallback(async (): Promise<void> => {
        const refreshToken = getStoredItem("refreshToken");
        try {
            if (refreshToken) {
                await authService.logout(refreshToken);
            }
        } catch {
            // Silently ignore logout API errors — we clean up locally regardless
        } finally {
            clearSession();
            setUser(null);
        }
    }, []);

    const updateUser = useCallback((updates: Partial<User>) => {
        setUser((prev) => {
            if (!prev) return null;
            const updated = { ...prev, ...updates };
            if (localStorage.getItem("user")) localStorage.setItem("user", JSON.stringify(updated));
            if (sessionStorage.getItem("user")) sessionStorage.setItem("user", JSON.stringify(updated));
            return updated;
        });
    }, []);

    return (
        <AuthContext.Provider value={{ user, isLoading, login, logout, updateUser }}>
            {children}
        </AuthContext.Provider>
    );
}

