import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { AxiosError } from "axios";
import { Button } from "@/components/ui/button";
import { JobDetailCard } from "@/components/JobDetail";
import { getJobDetail } from "@/services/jobs/detailApi";
import type { JobDetailResponse } from "@/types/job-detail";
import { useAuth } from "@/hooks/useAuth";

export default function JobDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { user } = useAuth();
    const [job, setJob] = useState<JobDetailResponse | null>(null);
    const [loading, setLoading] = useState(Boolean(id));
    const [error, setError] = useState<string | null>(id ? null : "Invalid job ID.");

    useEffect(() => {
        if (!id) return;
        let ignore = false;
        getJobDetail(id)
            .then((data) => {
                if (!ignore) {
                    setJob(data);
                    setError(null);
                    setLoading(false);
                }
            })
            .catch((e) => {
                if (!ignore) {
                    const ax = e as AxiosError<{ message?: string }>;
                    if (ax.response?.status === 403) {
                        setError("You do not have permission to view this job.");
                    } else if (ax.response?.status === 404) {
                        setError("Job not found.");
                    } else {
                        setError("Failed to load job detail. Please try again.");
                    }
                    setJob(null);
                    setLoading(false);
                }
            });
        return () => {
            ignore = true;
        };
    }, [id]);

    if (loading) {
        return (
            <div className="space-y-4 p-6">
                <h1 className="text-xl font-semibold text-foreground">Job Detail</h1>
                <p className="rounded-lg border border-border bg-card p-4 text-sm text-muted-foreground">Loading job detail...</p>
            </div>
        );
    }

    if (error || !job) {
        return (
            <div className="space-y-4 p-6">
                <h1 className="text-xl font-semibold text-foreground">Job Detail</h1>
                <p className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                    {error ?? "Unable to show this job."}
                </p>
                <div>
                    <Button variant="outline" onClick={() => navigate("/jobs")}>
                        Back to Jobs
                    </Button>
                </div>
            </div>
        );
    }

    return (
        <div className="space-y-6 p-6">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <Button variant="ghost" onClick={() => navigate("/jobs")} className="-ml-3 w-fit text-muted-foreground">
                    Back to Jobs
                </Button>
                {user?.role === "HR" && id && (
                    <Button asChild variant="outline" className="w-fit sm:ml-auto">
                        <Link to={`/jobs/${id}/edit`}>Edit job</Link>
                    </Button>
                )}
            </div>
            <JobDetailCard job={job} />
        </div>
    );
}
