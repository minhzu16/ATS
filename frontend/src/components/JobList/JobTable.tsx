import type { JobListItem } from "@/types/job";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { Link } from "react-router-dom";

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

function statusBadgeClass(status: JobListItem["status"]): string {
    switch (status) {
        case "APPROVED":
            return "border-transparent bg-secondary text-secondary-foreground";
        case "DRAFT":
        case "PENDING_APPROVAL":
            return "border-transparent bg-muted text-muted-foreground";
        case "REJECTED":
            return "border-transparent bg-destructive/15 text-destructive";
        case "CLOSED":
            return "border-transparent bg-muted text-muted-foreground";
        default:
            return "";
    }
}

export function JobTable({ jobs, isLoading }: JobTableProps) {
    return (
        <div className="mt-8 flow-root">
            <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
                    <div className="overflow-hidden rounded-lg border border-border bg-card shadow-sm ring-1 ring-border/50">
                        <table className="min-w-full divide-y divide-border">
                            <thead className="bg-muted/50">
                                <tr>
                                    <th
                                        scope="col"
                                        className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-foreground sm:pl-6"
                                    >
                                        Department
                                    </th>
                                    <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-foreground">
                                        Title
                                    </th>
                                    <th scope="col" className="px-3 py-3.5 text-left text-sm font-semibold text-foreground">
                                        Status
                                    </th>
                                    <th scope="col" className="px-3 py-3.5 text-right text-sm font-semibold text-foreground">
                                        Action
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-border bg-card">
                                {isLoading && (
                                    <tr>
                                        <td colSpan={4} className="px-6 py-8 text-center text-sm text-muted-foreground">
                                            Loading…
                                        </td>
                                    </tr>
                                )}
                                {!isLoading && jobs.length === 0 && (
                                    <tr>
                                        <td colSpan={4} className="px-6 py-8 text-center text-sm text-muted-foreground">
                                            No jobs found.
                                        </td>
                                    </tr>
                                )}
                                {!isLoading &&
                                    jobs.map((job) => (
                                        <tr key={job.jobId} className="hover:bg-muted/40">
                                            <td className="whitespace-nowrap py-4 pl-4 pr-3 text-sm text-muted-foreground sm:pl-6">
                                                {job.departmentName?.trim() || "—"}
                                            </td>
                                            <td className="whitespace-nowrap px-3 py-4 text-sm font-medium text-foreground">
                                                {job.title}
                                            </td>
                                            <td className="whitespace-nowrap px-3 py-4 text-sm">
                                                <Badge
                                                    variant="outline"
                                                    className={cn("rounded-full text-xs font-semibold", statusBadgeClass(job.status))}
                                                >
                                                    {statusLabel(job.status)}
                                                </Badge>
                                            </td>
                                            <td className="whitespace-nowrap px-3 py-4 text-right text-sm">
                                                <Button asChild size="sm" variant="outline">
                                                    <Link to={`/jobs/${job.jobId}`}>View Detail</Link>
                                                </Button>
                                            </td>
                                        </tr>
                                    ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    );
}
