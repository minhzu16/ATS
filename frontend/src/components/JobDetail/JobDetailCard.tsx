import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { MoreHorizontal, Star } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import type { JobApplicantItem, JobDetailResponse } from "@/types/job-detail";
import type { CandidateStage } from "@/types/candidate";
import { cn } from "@/lib/utils";

type TabKey = "overview" | "candidates" | "team";

interface JobDetailCardProps {
    job: JobDetailResponse;
}

function toLabel(status: JobDetailResponse["status"]): string {
    return status
        .split("_")
        .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
        .join(" ");
}

function statusClass(status: JobDetailResponse["status"]): string {
    switch (status) {
        case "APPROVED":
            return "border-transparent bg-secondary text-secondary-foreground";
        case "REJECTED":
            return "border-transparent bg-destructive/15 text-destructive";
        case "DRAFT":
        case "PENDING_APPROVAL":
        case "CLOSED":
            return "border-transparent bg-muted text-muted-foreground";
        default:
            return "border-transparent bg-muted text-muted-foreground";
    }
}

function splitDescription(description: string | null): string[] {
    if (!description) {
        return [];
    }
    return description
        .split(/\r?\n/)
        .map((line) => line.trim())
        .filter((line) => line.length > 0);
}

function stageLabel(stage: CandidateStage): string {
    return stage.charAt(0) + stage.slice(1).toLowerCase();
}

function formatRelativeDate(isoDate: string): string {
    const hasTimezone = /Z$|[+-]\d{2}:\d{2}$/.test(isoDate);
    const normalized = hasTimezone ? isoDate : `${isoDate}+07:00`;
    const appliedTime = new Date(normalized).getTime();
    if (Number.isNaN(appliedTime)) return "-";

    const diffMs = Date.now() - appliedTime;
    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;

    if (diffMs < hour) {
        const minutes = Math.max(1, Math.floor(diffMs / minute));
        return `${minutes} minute${minutes > 1 ? "s" : ""} ago`;
    }

    if (diffMs < day) {
        const hours = Math.floor(diffMs / hour);
        return `${hours} hour${hours > 1 ? "s" : ""} ago`;
    }

    const days = Math.floor(diffMs / day);
    return `${days} day${days > 1 ? "s" : ""} ago`;
}

function initials(name: string): string {
    return name
        .split(" ")
        .map((part) => part[0])
        .slice(0, 2)
        .join("")
        .toUpperCase();
}

function formatInterviewScore(rating: JobApplicantItem["rating"]): string {
    if (rating == null) return "—";
    const n = typeof rating === "number" ? rating : Number(rating);
    if (Number.isNaN(n)) return "—";
    return Number.isInteger(n) ? String(n) : n.toFixed(1);
}

export function JobDetailCard({ job }: JobDetailCardProps) {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState<TabKey>("overview");

    const descriptionLines = useMemo(() => splitDescription(job.description), [job.description]);
    const overviewDescription = descriptionLines.length > 0 ? descriptionLines : ["No description provided."];

    const applicants = job.applicants ?? [];

    return (
        <div className="space-y-6">
            <section className="rounded-lg bg-card shadow">
                <div className="flex flex-col justify-between gap-4 px-4 py-5 sm:px-6 lg:flex-row lg:items-start">
                    <div>
                        <h1 className="text-2xl font-bold text-foreground">{job.title}</h1>
                        <p className="mt-1 max-w-2xl text-sm text-muted-foreground">
                            {job.departmentName ?? "No department"} • Hiring Manager: {job.hiringManagerName ?? "Unassigned"}
                        </p>
                        <div className="mt-3 flex flex-wrap items-center gap-2">
                            <Badge variant="outline" className={cn("rounded-full text-xs font-semibold", statusClass(job.status))}>
                                {toLabel(job.status)}
                            </Badge>
                            <span className="text-sm text-muted-foreground">Headcount: {job.headcount ?? 0}</span>
                        </div>
                    </div>
                </div>

                <nav className="border-t border-border px-6" aria-label="Tabs">
                    <div className="-mb-px flex space-x-8">
                        {[
                            { key: "overview", label: "Overview" },
                            {
                                key: "candidates",
                                label: `Candidates (${applicants.length})`,
                            },
                            { key: "team", label: "Hiring Team" },
                        ].map((tab) => {
                            const selected = activeTab === tab.key;
                            return (
                                <button
                                    key={tab.key}
                                    type="button"
                                    onClick={() => setActiveTab(tab.key as TabKey)}
                                    className={cn(
                                        "whitespace-nowrap border-b-2 px-1 py-4 text-sm font-medium",
                                        selected
                                            ? "border-foreground text-foreground"
                                            : "border-transparent text-muted-foreground hover:border-border hover:text-foreground"
                                    )}
                                >
                                    {tab.label}
                                </button>
                            );
                        })}
                    </div>
                </nav>
            </section>

            {activeTab === "overview" && (
                <section className="rounded-lg bg-card p-6 shadow">
                    <h2 className="mb-2 text-lg font-bold text-foreground">Description</h2>
                    <div className="space-y-3 text-sm leading-6 text-foreground">
                        {overviewDescription.map((line, index) => (
                            <p key={`${line}-${index}`}>{line}</p>
                        ))}
                    </div>
                </section>
            )}

            {activeTab === "candidates" && (
                <section className="rounded-lg bg-card p-6 shadow">
                    <h2 className="mb-2 text-lg font-bold text-foreground">Candidates</h2>
                    <p className="mb-4 text-sm text-muted-foreground">
                        Active applications for this role. Interview score is the average of all scorecard scores submitted
                        after interviews for this application.
                    </p>
                    <div className="overflow-hidden rounded-lg border border-border">
                        <table className="min-w-full divide-y divide-border">
                            <thead className="bg-muted/30">
                                <tr>
                                    <th className="px-5 py-3 text-left text-sm font-semibold">Name</th>
                                    <th className="px-5 py-3 text-left text-sm font-semibold">Stage</th>
                                    <th className="px-5 py-3 text-left text-sm font-semibold">Interview score</th>
                                    <th className="px-5 py-3 text-left text-sm font-semibold">Applied</th>
                                    <th className="px-5 py-3 text-right text-sm font-semibold" />
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-border">
                                {applicants.length === 0 ? (
                                    <tr>
                                        <td colSpan={5} className="px-5 py-10 text-center text-sm text-muted-foreground">
                                            No applicants yet for this job.
                                        </td>
                                    </tr>
                                ) : (
                                    applicants.map((item) => (
                                        <tr
                                            key={item.candidateId}
                                            className="cursor-pointer bg-background hover:bg-muted/20"
                                            onClick={() => navigate(`/candidates/${item.candidateId}`)}
                                        >
                                            <td className="px-5 py-4">
                                                <div className="flex items-center gap-3">
                                                    <span className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-muted font-semibold text-primary">
                                                        {initials(item.fullName)}
                                                    </span>
                                                    <div>
                                                        <p className="font-medium">{item.fullName}</p>
                                                        <p className="text-sm text-muted-foreground">{item.email}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-5 py-4">
                                                <Badge variant="outline">{stageLabel(item.stage)}</Badge>
                                            </td>
                                            <td className="px-5 py-4">
                                                <div className="flex items-center gap-1 text-sm text-muted-foreground">
                                                    <Star className="h-4 w-4 shrink-0" />
                                                    {formatInterviewScore(item.rating)}
                                                </div>
                                            </td>
                                            <td className="px-5 py-4 text-sm text-muted-foreground">
                                                {formatRelativeDate(item.appliedAt)}
                                            </td>
                                            <td className="px-5 py-4 text-right text-muted-foreground">
                                                <MoreHorizontal className="ml-auto h-4 w-4" />
                                            </td>
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </section>
            )}

            {activeTab === "team" && (
                <section className="rounded-lg bg-card p-6 shadow">
                    <h2 className="mb-4 text-lg font-bold text-foreground">Hiring Team</h2>
                    <div className="rounded-md border border-border p-4">
                        <p className="text-sm font-medium text-foreground">{job.hiringManagerName ?? "Unassigned"}</p>
                        <p className="text-xs text-muted-foreground">Hiring Manager</p>
                    </div>
                </section>
            )}
        </div>
    );
}
