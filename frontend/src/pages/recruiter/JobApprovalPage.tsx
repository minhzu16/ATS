import { useCallback, useEffect, useState } from "react";
import type { AxiosError } from "axios";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { approveJob, getPendingJobs, rejectJob } from "@/services/jobs/commandApi";
import type { JobListItem } from "@/types/job";
import { cn } from "@/lib/utils";

function statusBadgeClass(status: JobListItem["status"]): string {
    switch (status) {
        case "PENDING_APPROVAL":
            return "border-transparent bg-muted text-muted-foreground";
        default:
            return "border-transparent bg-secondary text-secondary-foreground";
    }
}

export default function JobApprovalPage() {
    const [jobs, setJobs] = useState<JobListItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [actionId, setActionId] = useState<string | null>(null);

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await getPendingJobs();
            setJobs(data);
        } catch (e) {
            const ax = e as AxiosError<{ message?: string }>;
            if (ax.response?.status === 403) {
                setError("You do not have permission to view pending jobs.");
            } else {
                setError("Failed to load pending jobs.");
            }
            setJobs([]);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void load();
    }, [load]);

    const handleApprove = async (jobId: string) => {
        setActionId(jobId);
        setError(null);
        try {
            await approveJob(jobId);
            await load();
        } catch {
            setError("Could not approve this job. It may have been updated.");
        } finally {
            setActionId(null);
        }
    };

    const handleReject = async (jobId: string) => {
        setActionId(jobId);
        setError(null);
        try {
            await rejectJob(jobId);
            await load();
        } catch {
            setError("Could not reject this job. It may have been updated.");
        } finally {
            setActionId(null);
        }
    };

    return (
        <div className="space-y-6 p-6">
            <div>
                <h1 className="text-xl font-semibold text-foreground">Pending job approvals</h1>
                <p className="mt-1 text-sm text-muted-foreground">Review and approve or reject job postings.</p>
            </div>

            {error && (
                <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
                    {error}
                </p>
            )}

            {loading ? (
                <p className="text-sm text-muted-foreground">Loading…</p>
            ) : jobs.length === 0 ? (
                <p className="rounded-lg border border-border bg-card p-4 text-sm text-muted-foreground">No jobs pending approval.</p>
            ) : (
                <div className="overflow-x-auto rounded-lg border border-border">
                    <table className="w-full min-w-[480px] text-left text-sm">
                        <thead className="border-b border-border bg-muted/50">
                            <tr>
                                <th className="px-4 py-3 font-medium text-foreground">Job title</th>
                                <th className="px-4 py-3 font-medium text-foreground">Status</th>
                                <th className="px-4 py-3 font-medium text-foreground text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {jobs.map((job) => (
                                <tr key={job.jobId} className="border-b border-border last:border-0">
                                    <td className="px-4 py-3 text-foreground">{job.title}</td>
                                    <td className="px-4 py-3">
                                        <Badge variant="outline" className={cn("rounded-full text-xs font-semibold", statusBadgeClass(job.status))}>
                                            Pending approval
                                        </Badge>
                                    </td>
                                    <td className="px-4 py-3 text-right">
                                        <div className="flex justify-end gap-2">
                                            <Button
                                                size="sm"
                                                variant="default"
                                                disabled={actionId === job.jobId}
                                                onClick={() => void handleApprove(job.jobId)}
                                            >
                                                Approve
                                            </Button>
                                            <Button
                                                size="sm"
                                                variant="outline"
                                                disabled={actionId === job.jobId}
                                                onClick={() => void handleReject(job.jobId)}
                                            >
                                                Reject
                                            </Button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
