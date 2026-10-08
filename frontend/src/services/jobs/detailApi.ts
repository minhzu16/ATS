import api from "@/lib/api";
import type { JobApplicantItem, JobDetailResponse } from "@/types/job-detail";

/**
 * Loads job detail and always resolves `applicants`.
 * If the server omits `applicants` (older deployments), loads them from `GET /jobs/:id/applicants`.
 */
export async function getJobDetail(jobId: string): Promise<JobDetailResponse> {
    const { data } = await api.get<JobDetailResponse>(`/jobs/${jobId}`);
    let applicants = data.applicants;
    if (applicants == null) {
        try {
            applicants = await getJobApplicants(jobId);
        } catch {
            applicants = [];
        }
    }
    return { ...data, applicants };
}

export async function getJobApplicants(jobId: string): Promise<JobApplicantItem[]> {
    const { data } = await api.get<JobApplicantItem[]>(`/jobs/${jobId}/applicants`);
    return data;
}
