import api from "@/lib/api";
import type { JobPageResponse } from "@/types/job";

export interface GetJobsPageParams {
    page: number;
    size: number;
    keyword?: string;
}

export async function getJobsPage(params: GetJobsPageParams): Promise<JobPageResponse> {
    const { data } = await api.get<JobPageResponse>("/jobs/page", {
        params: {
            page: params.page,
            size: params.size,
            ...(params.keyword?.trim() ? { keyword: params.keyword.trim() } : {}),
        },
    });
    return data;
}
