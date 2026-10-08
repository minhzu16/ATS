import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { JobTable, Pagination, SearchBar } from "@/components/JobList";
import { useAuth } from "@/hooks/useAuth";
import { getJobsPage } from "@/services/jobs/api";
import type { JobListItem } from "@/types/job";
import { cn } from "@/lib/utils";
import type { AxiosError } from "axios";

const PAGE_SIZE_OPTIONS = [20, 50, 100] as const;

export default function JobListPage() {
    const { user } = useAuth();
    const [jobs, setJobs] = useState<JobListItem[]>([]);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState<number>(20);
    const [keywordInput, setKeywordInput] = useState("");
    const [keyword, setKeyword] = useState("");
    const keywordDebounceBoot = useRef(true);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (keywordDebounceBoot.current) {
            keywordDebounceBoot.current = false;
            setKeyword(keywordInput);
            return;
        }
        const t = window.setTimeout(() => {
            setKeyword(keywordInput);
            setPage(0);
        }, 300);
        return () => window.clearTimeout(t);
    }, [keywordInput]);

    const load = useCallback(async () => {
        if (!user) {
            setLoading(false);
            return;
        }
        if (user.role === "SYSTEM_ADMIN") {
            setLoading(false);
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const data = await getJobsPage({ page, size, keyword: keyword || undefined });
            setJobs(data.content);
            setTotalElements(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (e) {
            const ax = e as AxiosError<{ message?: string }>;
            if (ax.response?.status === 403) {
                setError("You do not have permission to view the job list.");
            } else {
                setError("Failed to load jobs. Please try again.");
            }
            setJobs([]);
            setTotalElements(0);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    }, [user, page, size, keyword]);

    useEffect(() => {
        void load();
    }, [load]);

    const showPostJob = user?.role === "HR";

    if (user?.role === "SYSTEM_ADMIN") {
        return (
            <div className="space-y-4 p-6">
                <h1 className="text-base font-semibold leading-6 text-foreground">Job Postings</h1>
                <p className="rounded-lg border border-border bg-card p-4 text-sm text-muted-foreground">
                    Your role cannot access the job list. If you need job data, contact an HR manager.
                </p>
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-2xl font-bold tracking-tight text-foreground">Job Openings</h1>
                    <p className="mt-1 text-sm text-muted-foreground">
                        Corporate job requisitions, departmental postings, and lifecycle status
                    </p>
                </div>
                {showPostJob && (
                    <Button asChild size="sm" className="gap-1.5 shadow-xs">
                        <Link to="/jobs/new">
                            <Plus className="h-4 w-4" aria-hidden />
                            Post Opening
                        </Link>
                    </Button>
                )}
            </div>

            <div className="flex flex-col gap-3 rounded-xl border border-border bg-card p-4 shadow-xs sm:flex-row sm:items-center sm:justify-between">
                <div className="flex-1">
                    <SearchBar value={keywordInput} onChange={setKeywordInput} />
                </div>
                <div className="sm:w-36">
                    <select
                        id="job-page-size"
                        className={cn(
                            "flex h-9 w-full rounded-lg border border-input bg-background px-3 text-xs font-medium text-foreground",
                            "hover:border-border/80 focus:outline-none focus:ring-1 focus:ring-ring"
                        )}
                        value={size}
                        onChange={(e) => {
                            setSize(Number(e.target.value));
                            setPage(0);
                        }}
                    >
                        {PAGE_SIZE_OPTIONS.map((n) => (
                            <option key={n} value={n}>
                                {n} per page
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {error && (
                <p className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
                    {error}
                </p>
            )}

            <JobTable jobs={jobs} isLoading={loading} />

            <Pagination
                currentPage={page}
                totalPages={totalPages}
                pageSize={size}
                totalElements={totalElements}
                onPageChange={setPage}
            />
        </div>
    );
}
