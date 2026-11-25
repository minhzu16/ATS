import type { UserRole } from "@/types/user";

export type StatusFilter = "" | "active" | "locked" | "inactive";

export const ROLE_LABELS: Record<UserRole, string> = {
    SYSTEM_ADMIN: "Admin",
    HR: "HR",
    HR_MANAGER: "HR Manager",
    INTERVIEWER: "Interviewer",
};
