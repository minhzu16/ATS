import api from "@/lib/api";
import type { JobDetailResponse } from "@/types/job-detail";
import type { JobListItem } from "@/types/job";

export interface JobCreatePayload {
    title: string;
    description: string;
    headcount: number;
}

export interface JobUpdatePayload {
    title: string;
    description: string;
    headcount: number;
}

export async function createJob(payload: JobCreatePayload): Promise<JobListItem> {
    const { data } = await api.post<JobListItem>("/jobs", payload);
    return data;
}

export async function updateJob(jobId: string, payload: JobUpdatePayload): Promise<JobListItem> {
    const { data } = await api.put<JobListItem>(`/jobs/${jobId}`, payload);
    return data;
}

export async function getPendingJobs(): Promise<JobListItem[]> {
    const { data } = await api.get<JobListItem[]>("/jobs/pending");
    return data;
}

export async function approveJob(jobId: string): Promise<JobListItem> {
    const { data } = await api.put<JobListItem>(`/jobs/${jobId}/approve`);
    return data;
}

export async function rejectJob(jobId: string): Promise<JobListItem> {
    const { data } = await api.put<JobListItem>(`/jobs/${jobId}/reject`);
    return data;
}

export async function getJobForEdit(jobId: string): Promise<JobDetailResponse> {
    const { data } = await api.get<JobDetailResponse>(`/jobs/${jobId}/edit`);
    return data;
}
