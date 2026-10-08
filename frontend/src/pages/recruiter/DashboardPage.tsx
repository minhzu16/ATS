import { useCallback, useEffect, useState } from "react";
import { Briefcase, Calendar, CalendarOff, Loader2, Users } from "lucide-react";
import StatCard from "@/components/shared/StatCard";
import { Badge } from "@/components/ui/badge";
import { useAuth } from "@/hooks/useAuth";
import {
    fetchRecruiterDashboardStats,
    type DashboardStats,
} from "@/services/recruiterDashboardService";

function initialsFromName(name: string): string {
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return "?";
    if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

type StageKey =
    | "APPLIED"
    | "SCREENING"
    | "INTERVIEW"
    | "OFFER"
    | "HIRED"
    | "REJECTED"
    | string;

const STAGE_VARIANT: Record<
    string,
    "default" | "secondary" | "outline" | "destructive"
> = {
    APPLIED: "secondary",
    SCREENING: "default",
    INTERVIEW: "outline",
    OFFER: "default",
    HIRED: "default",
    REJECTED: "destructive",
};

const STAGE_CLASS: Record<string, string> = {
    APPLIED:
        "bg-slate-50 text-slate-800 ring-1 ring-slate-600/20 hover:bg-slate-50",
    SCREENING:
        "bg-blue-50 text-blue-700 ring-1 ring-blue-700/10 hover:bg-blue-50",
    INTERVIEW:
        "bg-purple-50 text-purple-700 ring-1 ring-purple-700/10 hover:bg-purple-50",
    OFFER: "bg-green-50 text-green-700 ring-1 ring-green-700/10 hover:bg-green-50",
    HIRED: "bg-emerald-50 text-emerald-800 ring-1 ring-emerald-700/15 hover:bg-emerald-50",
    REJECTED: "bg-red-50 text-red-800 ring-1 ring-red-600/20 hover:bg-red-50",
};

function stageLabel(stage: string): string {
    const map: Record<string, string> = {
        APPLIED: "Applied",
        SCREENING: "Screening",
        INTERVIEW: "Interview",
        OFFER: "Offer",
        HIRED: "Hired",
        REJECTED: "Rejected",
    };
    return map[stage] ?? stage.replace(/_/g, " ");
}

function formatTime(iso: string | null): string {
    if (!iso) return "—";
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return iso;
    return d.toLocaleTimeString(undefined, {
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function DashboardPage() {
    const { user } = useAuth();
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const data = await fetchRecruiterDashboardStats();
            setStats(data);
        } catch (e) {
            setError(
                e instanceof Error ? e.message : "Failed to load dashboard"
            );
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        void load();
    }, [load]);

    const scopeHint =
        user?.role === "HR_MANAGER"
            ? "Organization-wide metrics"
            : user?.department
              ? `Department: ${user.department}`
              : user?.role === "HR" || user?.role === "INTERVIEWER"
                ? "Assign a department to see scoped data"
                : null;

    if (loading && !stats) {
        return (
            <div className="flex min-h-[240px] items-center justify-center gap-2 text-muted-foreground">
                <Loader2 className="h-6 w-6 animate-spin" />
                <span>Loading dashboard…</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-6 text-destructive">
                <p className="font-medium">Could not load dashboard</p>
                <p className="mt-1 text-sm opacity-90">{error}</p>
            </div>
        );
    }

    if (!stats) {
        return null;
    }

    return (
        <div className="space-y-8">
            {scopeHint && (
                <p className="text-sm text-muted-foreground">{scopeHint}</p>
            )}

            <div className="grid grid-cols-1 gap-4 md:grid-cols-3 md:gap-6">
                <StatCard
                    title="Active Jobs"
                    value={stats.activeJobs}
                    icon={Briefcase}
                    iconBgClass="bg-blue-50"
                    iconColorClass="text-blue-600"
                />
                <StatCard
                    title="New Candidates"
                    value={stats.newCandidates}
                    icon={Users}
                    iconBgClass="bg-green-50"
                    iconColorClass="text-green-600"
                />
                <StatCard
                    title="Interviews Today"
                    value={stats.interviewsToday}
                    icon={Calendar}
                    iconBgClass="bg-purple-50"
                    iconColorClass="text-purple-600"
                />
            </div>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
                <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                    <h3 className="mb-4 text-lg font-semibold text-card-foreground">
                        Recent Applications
                    </h3>
                    <div className="space-y-1">
                        {stats.recentApplications.length === 0 ? (
                            <p className="py-8 text-center text-sm text-muted-foreground">
                                No applications yet in this scope.
                            </p>
                        ) : (
                            stats.recentApplications.map((app) => {
                                const stage = app.stage as StageKey;
                                const variant =
                                    STAGE_VARIANT[stage] ?? "secondary";
                                const cls =
                                    STAGE_CLASS[stage] ??
                                    "bg-muted text-muted-foreground";
                                return (
                                    <div
                                        key={app.applicationId}
                                        className="flex items-center justify-between border-b border-border py-3 last:border-0"
                                    >
                                        <div className="flex min-w-0 items-center gap-3">
                                            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-muted text-sm font-bold text-muted-foreground">
                                                {initialsFromName(
                                                    app.candidateName
                                                )}
                                            </div>
                                            <div className="min-w-0">
                                                <p className="truncate text-sm font-medium text-foreground">
                                                    {app.candidateName ||
                                                        "Candidate"}
                                                </p>
                                                <p className="truncate text-xs text-muted-foreground">
                                                    Applied for {app.jobTitle}
                                                </p>
                                            </div>
                                        </div>
                                        <Badge
                                            variant={variant}
                                            className={cls}
                                        >
                                            {stageLabel(app.stage)}
                                        </Badge>
                                    </div>
                                );
                            })
                        )}
                    </div>
                </div>

                <div className="rounded-lg border border-border bg-card p-6 shadow-sm">
                    <h3 className="mb-4 text-lg font-semibold text-card-foreground">
                        Today&apos;s Interviews
                    </h3>
                    {stats.todaysInterviews.length === 0 ? (
                        <div className="flex h-48 items-center justify-center text-muted-foreground">
                            <div className="text-center">
                                <CalendarOff className="mx-auto mb-2 h-10 w-10 opacity-30" />
                                <p className="text-sm">
                                    No interviews scheduled for today in this
                                    scope.
                                </p>
                            </div>
                        </div>
                    ) : (
                        <ul className="divide-y divide-border">
                            {stats.todaysInterviews.map((iv) => (
                                <li
                                    key={iv.interviewId}
                                    className="flex flex-col gap-1 py-3 first:pt-0"
                                >
                                    <div className="flex items-start justify-between gap-2">
                                        <div>
                                            <p className="text-sm font-medium text-foreground">
                                                {iv.candidateName}
                                            </p>
                                            <p className="text-xs text-muted-foreground">
                                                {iv.jobTitle}
                                            </p>
                                        </div>
                                        <span className="shrink-0 text-sm font-medium text-primary">
                                            {formatTime(iv.scheduledAt)}
                                        </span>
                                    </div>
                                    <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                                        {iv.location && (
                                            <span>{iv.location}</span>
                                        )}
                                        {iv.status && (
                                            <Badge variant="outline">
                                                {iv.status.replace(/_/g, " ")}
                                            </Badge>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            </div>
        </div>
    );
}
