import api from "@/lib/api";
import type {
    UserRecord,
    CreateUserPayload,
    UpdateUserPayload,
    ChangePasswordPayload,
} from "@/types/user";
import type { PaginatedResponse } from "@/types/admin";

export const userService = {
    /** Current session user (includes department name). */
    async getMe(): Promise<UserRecord> {
        const { data } = await api.get<UserRecord>("/users/me");
        return data;
    },

    async getUsers(page = 0, size = 10, departmentId?: string): Promise<PaginatedResponse<UserRecord>> {
        const { data } = await api.get<PaginatedResponse<UserRecord>>("/users", {
            params: { page, size, departmentId }
        });
        return data;
    },

    async getUserById(id: string): Promise<UserRecord> {
        const { data } = await api.get<UserRecord>(`/users/${id}`);
        return data;
    },

    async createUser(payload: CreateUserPayload): Promise<UserRecord> {
        const { data } = await api.post<UserRecord>("/users", payload);
        return data;
    },

    async updateUser(id: string, payload: UpdateUserPayload): Promise<UserRecord> {
        const { data } = await api.put<UserRecord>(`/users/${id}`, payload);
        return data;
    },

    async lockUser(id: string): Promise<UserRecord> {
        const { data } = await api.patch<UserRecord>(`/users/${id}/lock`);
        return data;
    },

    async unlockUser(id: string): Promise<UserRecord> {
        const { data } = await api.patch<UserRecord>(`/users/${id}/unlock`);
        return data;
    },

    async deleteUser(id: string): Promise<void> {
        await api.delete(`/users/${id}`);
    },

    async changePassword(payload: ChangePasswordPayload): Promise<void> {
        await api.post("/users/me/change-password", payload);
    },

    async uploadAvatar(file: File): Promise<{ avatarUrl: string }> {
        const formData = new FormData();
        formData.append("file", file);
        const { data } = await api.post<{ avatarUrl: string }>("/users/me/avatar", formData, {
            headers: {
                "Content-Type": "multipart/form-data",
            },
        });
        return data;
    },
};
