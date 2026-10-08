export type JobStatus =
    | "DRAFT"
    | "PENDING_APPROVAL"
    | "APPROVED"
    | "REJECTED"
    | "CLOSED";

export interface JobListItem {
    jobId: string;
    /** Department UUID for this job (from API). */
    departmentId: string | null;
    /** Display name from API (preferred over departmentId in tables). */
    departmentName?: string | null;
    title: string;
    status: JobStatus;
}

/** Option row for job dropdowns (e.g. add candidate), aligned with {@link JobListItem}. */
export interface JobOption {
    jobId: string;
    departmentId?: string | null;
    title: string;
    status: JobStatus;
}

export interface JobPageResponse {
    content: JobListItem[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
