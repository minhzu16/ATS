import { useCallback, useEffect, useState } from "react";
import { Briefcase, Calendar, CalendarOff, Users, ArrowUpRight } from "lucide-react";
import { useNavigate } from "react-router-dom";
import StatCard from "@/components/shared/StatCard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/hooks/useAuth";
import { StageBadge } from "@/components/shared/StageBadge";
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
    const navigate = useNavigate();
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
            ? "Organization-wide talent telemetry"
            : user?.department
              ? `Department telemetry: ${user.department}`
              : user?.role === "HR" || user?.role === "INTERVIEWER"
                ? "Department-scoped metrics"
                : null;

    if (loading && !stats) {
        return (
            <div className="flex min-h-[320px] items-center justify-center gap-3 text-muted-foreground">
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                <span className="text-sm font-medium">Loading talent workspace…</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="rounded-xl border border-destructive/20 bg-destructive/5 p-6 text-destructive">
                <p className="font-semibold text-sm">Dashboard metrics unavailable</p>
                <p className="mt-1 text-xs opacity-90">{error}</p>
                <Button variant="outline" size="sm" onClick={() => void load()} className="mt-4">
                    Retry connection
                </Button>
            </div>
        );
    }

    if (!stats) {
        return null;
    }

    return (
        <div className="space-y-6">
            {/* Top Overview Bar */}
            <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">
                        Talent Overview
                    </h1>
                    {scopeHint && (
                        <p className="mt-0.5 text-xs font-medium text-muted-foreground">
                            {scopeHint}
                        </p>
                    )}
                </div>
                <div className="flex items-center gap-2">
                    <Button
                        size="sm"
                        variant="outline"
                        className="h-8 text-xs gap-1"
                        onClick={() => navigate("/candidates")}
                    >
                        <span>All candidates</span>
                        <ArrowUpRight className="h-3.5 w-3.5" />
                    </Button>
                </div>
            </div>

            {/* Structured Metric Blocks */}
            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                <StatCard
                    title="Open Requisitions"
                    value={stats.activeJobs}
                    icon={Briefcase}
                    iconBgClass="bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300"
                    iconColorClass="text-blue-700 dark:text-blue-300"
                    description="Active postings accepting candidates"
                />
                <StatCard
                    title="Active Pool"
                    value={stats.newCandidates}
                    icon={Users}
                    iconBgClass="bg-indigo-50 text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-300"
                    iconColorClass="text-indigo-700 dark:text-indigo-300"
                    description="Candidates in review pipeline"
                />
                <StatCard
                    title="Scheduled Today"
                    value={stats.interviewsToday}
                    icon={Calendar}
                    iconBgClass="bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300"
                    iconColorClass="text-amber-700 dark:text-amber-300"
                    description="Rounds booked on calendar"
                />
            </div>

            {/* Split Content: Recent Applications vs Today's Schedule */}
            <div className="grid grid-cols-1 gap-6 lg:grid-cols-12">
                {/* Recent Applications (7 Cols) */}
                <div className="rounded-xl border border-border bg-card p-5 shadow-xs lg:col-span-7">
                    <div className="flex items-center justify-between pb-4 border-b border-border">
                        <div>
                            <h2 className="text-sm font-semibold text-foreground">
                                Incoming Applications
                            </h2>
                            <p className="text-xs text-muted-foreground mt-0.5">
                                Latest candidate submissions requiring review
                            </p>
                        </div>
                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-7 text-xs text-muted-foreground hover:text-foreground"
                            onClick={() => navigate("/candidates")}
                        >
                            View list
                        </Button>
                    </div>

                    <div className="divide-y divide-border">
                        {stats.recentApplications.length === 0 ? (
                            <div className="py-12 text-center text-xs text-muted-foreground">
                                No new applications currently awaiting triage in this scope.
                            </div>
                        ) : (
                            stats.recentApplications.map((app) => (
                                <div
                                    key={app.applicationId}
                                    className="group flex items-center justify-between py-3 cursor-pointer hover:bg-muted/30 px-1 -mx-1 rounded-md transition-colors"
                                    onClick={() => navigate("/candidates")}
                                >
                                    <div className="flex min-w-0 items-center gap-3">
                                        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-xs font-bold text-primary ring-1 ring-primary/20">
                                            {initialsFromName(app.candidateName)}
                                        </div>
                                        <div className="min-w-0">
                                            <p className="truncate text-xs font-semibold text-foreground group-hover:text-primary transition-colors">
                                                {app.candidateName || "Candidate"}
                                            </p>
                                            <p className="truncate text-[11px] text-muted-foreground">
                                                {app.jobTitle}
                                            </p>
                                        </div>
                                    </div>
                                    <StageBadge stage={app.stage} />
                                </div>
                            ))
                        )}
                    </div>
                </div>

                {/* Today's Interviews (5 Cols) */}
                <div className="rounded-xl border border-border bg-card p-5 shadow-xs lg:col-span-5">
                    <div className="flex items-center justify-between pb-4 border-b border-border">
                        <div>
                            <h2 className="text-sm font-semibold text-foreground">
                                Today&apos;s Interview Docket
                            </h2>
                            <p className="text-xs text-muted-foreground mt-0.5">
                                Scheduled evaluation sessions
                            </p>
                        </div>
                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-7 text-xs text-muted-foreground hover:text-foreground"
                            onClick={() => navigate("/interviews")}
                        >
                            Calendar
                        </Button>
                    </div>

                    {stats.todaysInterviews.length === 0 ? (
                        <div className="flex h-48 flex-col items-center justify-center text-center">
                            <CalendarOff className="mb-2 h-8 w-8 text-muted-foreground/30" />
                            <p className="text-xs font-medium text-foreground">
                                No interviews today
                            </p>
                            <p className="text-[11px] text-muted-foreground mt-0.5">
                                No evaluation meetings booked for today&apos;s roster.
                            </p>
                        </div>
                    ) : (
                        <ul className="divide-y divide-border">
                            {stats.todaysInterviews.map((iv) => (
                                <li
                                    key={iv.interviewId}
                                    className="flex flex-col gap-1.5 py-3 first:pt-3"
                                >
                                    <div className="flex items-start justify-between gap-2">
                                        <div className="min-w-0">
                                            <p className="text-xs font-semibold text-foreground truncate">
                                                {iv.candidateName}
                                            </p>
                                            <p className="text-[11px] text-muted-foreground truncate">
                                                {iv.jobTitle}
                                            </p>
                                        </div>
                                        <span className="shrink-0 rounded-md bg-primary/10 px-2 py-0.5 text-xs font-semibold text-primary font-mono-numbers">
                                            {formatTime(iv.scheduledAt)}
                                        </span>
                                    </div>
                                    <div className="flex flex-wrap items-center gap-1.5 text-[11px] text-muted-foreground">
                                        {iv.location && (
                                            <span className="truncate max-w-[180px]">{iv.location}</span>
                                        )}
                                        {iv.status && (
                                            <Badge variant="outline" className="text-[10px] py-0 px-1.5 font-normal">
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
