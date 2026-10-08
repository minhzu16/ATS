export type UserRole = "SYSTEM_ADMIN" | "HR" | "HR_MANAGER" | "INTERVIEWER";

export interface UserRecord {
    id: string;
    fullName: string;
    email: string;
    role: UserRole;
    active: boolean;
    accountLocked: boolean;
    department?: string;
    departmentId?: string;
    avatarUrl?: string;
    createdAt?: string;
}

export interface CreateUserPayload {
    fullName: string;
    email: string;
    role: UserRole;
    departmentId?: string;
}

export interface UpdateUserPayload {
    fullName?: string;
    role?: UserRole;
    departmentId?: string;
}

export interface ChangePasswordPayload {
    currentPassword: string;
    newPassword: string;
    confirmPassword: string;
}

export interface ForgotPasswordPayload {
    email: string;
}

export interface ResetPasswordPayload {
    token: string;
    newPassword: string;
    confirmPassword: string;
}

/** Shared payload for account activation and reset-via-link */
export interface SetPasswordPayload {
    token: string;
    password: string;
    confirmPassword: string;
}
