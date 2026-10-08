import api from "@/lib/api";

export interface DashboardRecentApplication {
    applicationId: string;
    candidateName: string;
    jobTitle: string;
    stage: string;
    appliedAt: string | null;
}

export interface DashboardTodaysInterview {
    interviewId: string;
    candidateName: string;
    jobTitle: string;
    scheduledAt: string | null;
    location: string | null;
    status: string;
}

export interface DashboardStats {
    activeJobs: number;
    newCandidates: number;
    interviewsToday: number;
    offersSent: number;
    hiringPipeline: Record<string, number>;
    recentApplications: DashboardRecentApplication[];
    todaysInterviews: DashboardTodaysInterview[];
}

export async function fetchRecruiterDashboardStats(): Promise<DashboardStats> {
    const { data } = await api.get<DashboardStats>("/dashboard/stats");
    return data;
}
