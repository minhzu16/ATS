export type UserRole = "SYSTEM_ADMIN" | "HR" | "HR_MANAGER" | "INTERVIEWER";

export interface User {
    id: string;
    fullName: string;
    email: string;
    role: UserRole;
    department?: string;
    avatarUrl?: string;
}

export interface LoginCredentials {
    email: string;
    password: string;
}

/** Shape of the response from POST /api/v1/auth/login and /auth/refresh */
export interface AuthApiResponse {
    accessToken: string;
    refreshToken: string;
    userId: string;
    fullName: string;
    email: string;
    role: UserRole;
    department?: string;
    avatarUrl?: string;
}

export interface AuthContextType {
    user: User | null;
    isLoading: boolean;
    login: (credentials: LoginCredentials, rememberMe?: boolean) => Promise<User>;
    logout: () => Promise<void>;
    updateUser: (updates: Partial<User>) => void;
}

