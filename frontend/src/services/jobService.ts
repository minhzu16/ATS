import api from "@/lib/api";
import type { JobOption } from "@/types/job";

export const jobService = {
  async getJobs(): Promise<JobOption[]> {
    const { data } = await api.get<JobOption[]>("/jobs");
    return data;
  },
};
