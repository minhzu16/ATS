import type { CandidateStage } from "@/types/candidate";
import type { JobStatus } from "@/types/job";

export interface JobApplicantItem {
    candidateId: string;
    fullName: string;
    email: string;
    stage: CandidateStage;
    /** Average scorecard score across interviews for this application, if any scores exist. */
    rating: number | null;
    appliedAt: string;
}

export interface JobDetailResponse {
    jobId: string;
    title: string;
    description: string | null;
    departmentName: string | null;
    hiringManagerId: string | null;
    hiringManagerName: string | null;
    status: JobStatus;
    headcount: number | null;
    /**
     * Active applications with optional average interview score (from scorecards).
     * May be omitted by older API builds; `getJobDetail` in detailApi fills via `/jobs/:id/applicants` when missing.
     */
    applicants?: JobApplicantItem[];
}
