import api from "@/lib/api";
import type { Department, SystemConfig, AuditLog, Notification, PaginatedResponse } from "@/types/admin";

export const adminService = {
    // Departments
    async getDepartments(page = 0, size = 10): Promise<PaginatedResponse<Department>> {
        const { data } = await api.get<PaginatedResponse<Department>>("/departments", {
            params: { page, size }
        });
        return data;
    },
    async createDepartment(payload: Partial<Department>): Promise<Department> {
        const { data } = await api.post<Department>("/departments", payload);
        return data;
    },
    async updateDepartment(id: string, payload: Partial<Department>): Promise<Department> {
        const { data } = await api.put<Department>(`/departments/${id}`, payload);
        return data;
    },
    async deleteDepartment(id: string): Promise<void> {
        await api.delete(`/departments/${id}`);
    },

    // System Config
    async getConfigs(page = 0, size = 50): Promise<PaginatedResponse<SystemConfig>> {
        const { data } = await api.get<PaginatedResponse<SystemConfig>>("/system-configs", {
            params: { page, size }
        });
        return data;
    },
    async updateConfig(payload: { configKey: string; value: string }): Promise<SystemConfig> {
        const { data } = await api.post<SystemConfig>("/system-configs", payload);
        return data;
    },

    // Audit Logs
    async getAuditLogs(page = 0, size = 15): Promise<PaginatedResponse<AuditLog>> {
        const { data } = await api.get<PaginatedResponse<AuditLog>>("/audit-logs", {
            params: { page, size }
        });
        return data;
    },
    async getAuditLogsByAction(action: string, page = 0, size = 15): Promise<PaginatedResponse<AuditLog>> {
        const { data } = await api.get<PaginatedResponse<AuditLog>>("/audit-logs/action", {
            params: { action, page, size }
        });
        return data;
    },

    // Notifications
    async getNotifications(page = 0, size = 10): Promise<PaginatedResponse<Notification>> {
        const { data } = await api.get<PaginatedResponse<Notification>>("/notifications", {
            params: { page, size }
        });
        return data;
    },
    async getAllNotificationsAdmin(page = 0, size = 20): Promise<PaginatedResponse<Notification>> {
        const { data } = await api.get<PaginatedResponse<Notification>>("/notifications/admin/all", {
            params: { page, size }
        });
        return data;
    },
    async markAsRead(id: string): Promise<void> {
        await api.patch(`/notifications/${id}/read`);
    },
    async markAllAsRead(): Promise<void> {
        await api.patch("/notifications/read-all");
    },
    async broadcastNotification(payload: { title: string; message: string; type?: string }): Promise<void> {
        await api.post("/notifications/broadcast", payload);
    },
    async sendToRole(payload: { role: string; title: string; message: string; type?: string }): Promise<void> {
        await api.post("/notifications/send-to-role", payload);
    },
    async deleteNotification(id: string): Promise<void> {
        await api.delete(`/notifications/${id}`);
    },

    // Export & Backup
    async exportAuditLogs(format: 'csv' | 'excel'): Promise<Blob> {
        const { data } = await api.get<Blob>(`/export/audit-logs/${format}`, { responseType: 'blob' });
        return data;
    },
    async exportUsers(format: 'csv' | 'excel'): Promise<Blob> {
        const { data } = await api.get<Blob>(`/export/users/${format}`, { responseType: 'blob' });
        return data;
    }
};
