import api from "@/lib/api";
import type { AuthApiResponse } from "@/types/auth";

export const authService = {
    async login(email: string, password: string): Promise<AuthApiResponse> {
        const { data } = await api.post<AuthApiResponse>("/auth/login", {
            email,
            password,
        });
        return data;
    },

    async logout(refreshToken: string): Promise<void> {
        await api.post("/auth/logout", { refreshToken });
    },

    async refresh(refreshToken: string): Promise<AuthApiResponse> {
        const { data } = await api.post<AuthApiResponse>("/auth/refresh", {
            refreshToken,
        });
        return data;
    },

    async forgotPassword(email: string): Promise<{ message: string }> {
        const { data } = await api.post<{ message: string }>("/auth/forgot-password", { email });
        return data;
    },

    async resetPassword(token: string, newPassword: string, confirmPassword: string): Promise<void> {
        await api.post("/auth/reset-password", { token, newPassword, confirmPassword });
    },

    async activateAccount(token: string, password: string, confirmPassword: string): Promise<{ message: string }> {
        const { data } = await api.post<{ message: string }>("/auth/activate", {
            token,
            password,
            confirmPassword,
        });
        return data;
    },
};


