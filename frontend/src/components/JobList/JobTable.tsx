import type { JobListItem } from "@/types/job";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Link } from "react-router-dom";
import { Building2, ChevronRight, Briefcase } from "lucide-react";

interface JobTableProps {
    jobs: JobListItem[];
    isLoading: boolean;
}

function statusLabel(status: JobListItem["status"]): string {
    return status
        .split("_")
        .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
        .join(" ");
}

const STATUS_CONFIG: Record<string, { bg: string; dot: string; text: string; border: string }> = {
    APPROVED: {
        bg: "bg-emerald-50 dark:bg-emerald-950/30",
        dot: "bg-emerald-500",
        text: "text-emerald-700 dark:text-emerald-300",
        border: "border-emerald-200 dark:border-emerald-800/60",
    },
    PENDING_APPROVAL: {
        bg: "bg-amber-50 dark:bg-amber-950/30",
        dot: "bg-amber-500",
        text: "text-amber-800 dark:text-amber-300",
        border: "border-amber-200 dark:border-amber-800/60",
    },
    DRAFT: {
        bg: "bg-slate-50 dark:bg-slate-900/40",
        dot: "bg-slate-400",
        text: "text-slate-700 dark:text-slate-300",
        border: "border-slate-200 dark:border-slate-700",
    },
    REJECTED: {
        bg: "bg-rose-50 dark:bg-rose-950/30",
        dot: "bg-rose-500",
        text: "text-rose-700 dark:text-rose-300",
        border: "border-rose-200 dark:border-rose-800/60",
    },
    CLOSED: {
        bg: "bg-muted/50",
        dot: "bg-muted-foreground",
        text: "text-muted-foreground",
        border: "border-border",
    },
};

export function JobTable({ jobs, isLoading }: JobTableProps) {
    return (
        <div className="overflow-hidden rounded-xl border border-border bg-card shadow-xs">
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-border text-left text-sm">
                    <thead className="bg-muted/40 text-xs font-semibold text-muted-foreground">
                        <tr>
                            <th scope="col" className="px-5 py-3">
                                Requisition
                            </th>
                            <th scope="col" className="px-5 py-3">
                                Department
                            </th>
                            <th scope="col" className="px-5 py-3">
                                Status
                            </th>
                            <th scope="col" className="px-5 py-3 text-right">
                                Action
                            </th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-border bg-card">
                        {isLoading && (
                            <tr>
                                <td colSpan={4} className="px-6 py-12 text-center text-sm text-muted-foreground">
                                    <div className="flex flex-col items-center justify-center gap-2">
                                        <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                                        <span>Loading job requisitions…</span>
                                    </div>
                                </td>
                            </tr>
                        )}
                        {!isLoading && jobs.length === 0 && (
                            <tr>
                                <td colSpan={4} className="px-6 py-16 text-center text-sm text-muted-foreground">
                                    <div className="mx-auto flex max-w-xs flex-col items-center">
                                        <Briefcase className="h-10 w-10 text-muted-foreground/30 mb-2" />
                                        <p className="font-medium text-foreground">No requisitions found</p>
                                        <p className="mt-1 text-xs text-muted-foreground">
                                            No active job postings match your criteria.
                                        </p>
                                    </div>
                                </td>
                            </tr>
                        )}
                        {!isLoading &&
                            jobs.map((job) => {
                                const conf = STATUS_CONFIG[job.status] ?? STATUS_CONFIG.DRAFT;
                                return (
                                    <tr
                                        key={job.jobId}
                                        className="group transition-colors hover:bg-muted/30"
                                    >
                                        <td className="px-5 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary ring-1 ring-primary/20">
                                                    <Briefcase className="h-4 w-4" />
                                                </div>
                                                <div className="min-w-0">
                                                    <Link
                                                        to={`/jobs/${job.jobId}`}
                                                        className="font-medium text-foreground hover:text-primary transition-colors line-clamp-1"
                                                    >
                                                        {job.title}
                                                    </Link>
                                                    <p className="text-xs text-muted-foreground">
                                                        ID: {job.jobId.slice(0, 8)}
                                                    </p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="whitespace-nowrap px-5 py-4 text-xs font-medium text-muted-foreground">
                                            <div className="flex items-center gap-1.5">
                                                <Building2 className="h-3.5 w-3.5 text-muted-foreground/60" />
                                                <span>{job.departmentName?.trim() || "Unassigned"}</span>
                                            </div>
                                        </td>
                                        <td className="whitespace-nowrap px-5 py-4 text-xs">
                                            <span
                                                className={cn(
                                                    "inline-flex items-center gap-1.5 rounded-md border px-2 py-0.5 text-xs font-medium",
                                                    conf.bg,
                                                    conf.border,
                                                    conf.text
                                                )}
                                            >
                                                <span
                                                    className={cn("h-1.5 w-1.5 rounded-full", conf.dot)}
                                                    aria-hidden="true"
                                                />
                                                {statusLabel(job.status)}
                                            </span>
                                        </td>
                                        <td className="whitespace-nowrap px-5 py-4 text-right">
                                            <Button
                                                asChild
                                                size="sm"
                                                variant="ghost"
                                                className="h-8 gap-1 text-xs text-muted-foreground hover:text-foreground"
                                            >
                                                <Link to={`/jobs/${job.jobId}`}>
                                                    <span>View details</span>
                                                    <ChevronRight className="h-3.5 w-3.5" />
                                                </Link>
                                            </Button>
                                        </td>
                                    </tr>
                                );
                            })}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
