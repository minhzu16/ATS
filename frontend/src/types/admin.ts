export interface Department {
    id: string;
    name: string;
    description?: string;
    employeeCount?: number;
    openPositions?: number;
    status?: boolean;
}

export interface SystemConfig {
    configKey: string;
    value: string;
    updatedAt?: string;
    updatedBy?: string;
}

export interface AuditLog {
    id: string;
    userId?: string;
    userEmail?: string;
    userFullName?: string;
    action: string;
    entityType: string;
    entityId?: string;
    oldValue?: string;
    newValue?: string;
    ipAddress?: string;
    userAgent?: string;
    createdAt: string;
}

export interface Notification {
    id: string;
    title: string;
    message: string;
    type: "INTERVIEW_PENDING" | "OFFER_APPROVAL_NEEDED" | "OFFER_APPROVED" | "OFFER_REJECTED" | "OFFER_ACCEPTED" | "ONBOARDING_ASSIGNED" | "SYSTEM_ALERT";
    read: boolean;
    createdAt: string;
}

export interface PaginatedResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    last: boolean;
    first: boolean;
    numberOfElements: number;
    empty: boolean;
}
