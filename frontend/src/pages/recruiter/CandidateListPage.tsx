import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Plus, Star, ChevronRight, Upload, Filter, Users } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { candidateService } from "@/services/candidateService";
import type { CandidateListItem, CandidateStage } from "@/types/candidate";
import { StageBadge, stageLabel } from "@/components/shared/StageBadge";
import AddCandidateDialog from "@/components/shared/AddCandidateDialog";
import ImportCandidatesDialog from "@/components/shared/ImportCandidatesDialog";

const STAGES: CandidateStage[] = [
  "APPLIED",
  "SCREENING",
  "INTERVIEW",
  "OFFER",
  "HIRED",
  "REJECTED",
];

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
    return `${minutes}m ago`;
  }

  if (diffMs < day) {
    const hours = Math.floor(diffMs / hour);
    return `${hours}h ago`;
  }

  const days = Math.floor(diffMs / day);
  return `${days}d ago`;
}

function initials(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function CandidateListPage() {
  const navigate = useNavigate();
  const [candidates, setCandidates] = useState<CandidateListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [jobFilter, setJobFilter] = useState("ALL");
  const [stageFilter, setStageFilter] = useState<"ALL" | CandidateStage>("ALL");
  const [addOpen, setAddOpen] = useState(false);
  const [importOpen, setImportOpen] = useState(false);

  const loadCandidates = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await candidateService.getCandidates();
      setCandidates(data);
    } catch {
      setError("Failed to load candidates.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadCandidates();
  }, []);

  const jobOptions = useMemo(() => {
    const unique = new Set(candidates.map((item) => item.jobTitle));
    return ["ALL", ...Array.from(unique)];
  }, [candidates]);

  const filteredCandidates = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    const byAppliedPosition =
      jobFilter === "ALL"
        ? candidates
        : candidates.filter((item) => item.jobTitle === jobFilter);

    const byStage =
      stageFilter === "ALL"
        ? byAppliedPosition
        : byAppliedPosition.filter((item) => item.stage === stageFilter);

    if (!normalizedSearch) {
      return byStage;
    }

    return byStage.filter((item) =>
      item.fullName.toLowerCase().includes(normalizedSearch) ||
      item.email.toLowerCase().includes(normalizedSearch)
    );
  }, [candidates, search, jobFilter, stageFilter]);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Candidates
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Applicant talent pool and active recruitment progression
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <Button
            size="sm"
            variant="outline"
            className="h-9 gap-1.5"
            onClick={() => setImportOpen(true)}
          >
            <Upload className="h-4 w-4" />
            Import CSV
          </Button>
          <Button
            size="sm"
            className="h-9 gap-1.5 shadow-xs"
            onClick={() => setAddOpen(true)}
          >
            <Plus className="h-4 w-4" />
            Add Candidate
          </Button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="rounded-xl border border-border bg-card shadow-xs">
        {/* Search & Filter Toolbar */}
        <div className="flex flex-col gap-3 border-b border-border p-4 md:flex-row md:items-center md:justify-between">
          <div className="relative w-full md:max-w-md">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search by candidate name or email…"
              className="h-9 pl-9 text-sm"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground mr-1">
              <Filter className="h-3.5 w-3.5" />
              <span>Filter:</span>
            </div>
            <select
              value={jobFilter}
              onChange={(event) => setJobFilter(event.target.value)}
              className="h-9 rounded-lg border border-input bg-background px-3 text-xs font-medium text-foreground transition-colors hover:border-border/80 focus:outline-none focus:ring-1 focus:ring-ring"
            >
              <option value="ALL">All Requisitions</option>
              {jobOptions
                .filter((item) => item !== "ALL")
                .map((job) => (
                  <option key={job} value={job}>
                    {job}
                  </option>
                ))}
            </select>
            <select
              value={stageFilter}
              onChange={(event) => setStageFilter(event.target.value as "ALL" | CandidateStage)}
              className="h-9 rounded-lg border border-input bg-background px-3 text-xs font-medium text-foreground transition-colors hover:border-border/80 focus:outline-none focus:ring-1 focus:ring-ring"
            >
              <option value="ALL">All Stages</option>
              {STAGES.map((stage) => (
                <option key={stage} value={stage}>
                  {stageLabel(stage)}
                </option>
              ))}
            </select>
          </div>
        </div>

        {error && (
          <div className="p-6 text-sm text-destructive bg-destructive/5 border-b border-destructive/20">
            {error}
          </div>
        )}

        {/* Candidate Table */}
        {!error && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-border bg-muted/40 text-xs font-semibold text-muted-foreground">
                <tr>
                  <th className="px-5 py-3">Candidate</th>
                  <th className="px-5 py-3">Applied Position</th>
                  <th className="px-5 py-3">Pipeline Stage</th>
                  <th className="px-5 py-3">Evaluation Score</th>
                  <th className="px-5 py-3">Applied</th>
                  <th className="px-5 py-3 text-right">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loading ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-12 text-center text-sm text-muted-foreground">
                      <div className="flex flex-col items-center justify-center gap-2">
                        <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
                        <span>Loading candidate roster…</span>
                      </div>
                    </td>
                  </tr>
                ) : filteredCandidates.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-16 text-center text-sm text-muted-foreground">
                      <div className="mx-auto flex max-w-xs flex-col items-center">
                        <Users className="h-10 w-10 text-muted-foreground/40 mb-3" />
                        <p className="font-medium text-foreground">No matching candidates</p>
                        <p className="mt-1 text-xs text-muted-foreground">
                          {search || jobFilter !== "ALL" || stageFilter !== "ALL"
                            ? "Try adjusting your filters or search keywords."
                            : "Get started by adding or importing your first candidate."}
                        </p>
                        {(search || jobFilter !== "ALL" || stageFilter !== "ALL") && (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="mt-3 text-xs"
                            onClick={() => {
                              setSearch("");
                              setJobFilter("ALL");
                              setStageFilter("ALL");
                            }}
                          >
                            Reset filters
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ) : (
                  filteredCandidates.map((item) => (
                    <tr
                      key={`${item.candidateId}-${item.jobTitle}`}
                      className="group cursor-pointer transition-colors hover:bg-muted/30"
                      onClick={() => navigate(`/candidates/${item.candidateId}`)}
                    >
                      <td className="px-5 py-3.5">
                        <div className="flex items-center gap-3">
                          <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-xs font-bold text-primary ring-1 ring-primary/20">
                            {initials(item.fullName)}
                          </div>
                          <div className="min-w-0">
                            <p className="font-medium text-foreground truncate group-hover:text-primary transition-colors">
                              {item.fullName}
                            </p>
                            <p className="text-xs text-muted-foreground truncate">
                              {item.email}
                            </p>
                          </div>
                        </div>
                      </td>
                      <td className="px-5 py-3.5 text-xs font-medium text-foreground/80">
                        {item.jobTitle}
                      </td>
                      <td className="px-5 py-3.5">
                        <StageBadge stage={item.stage} />
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="inline-flex items-center gap-1 text-xs font-medium text-foreground">
                          <Star className="h-3.5 w-3.5 fill-amber-400 text-amber-400" />
                          <span>{item.rating != null ? item.rating : "—"}</span>
                        </div>
                      </td>
                      <td className="px-5 py-3.5 text-xs text-muted-foreground">
                        {formatRelativeDate(item.appliedAt)}
                      </td>
                      <td className="px-5 py-3.5 text-right">
                        <ChevronRight className="ml-auto h-4 w-4 text-muted-foreground/40 group-hover:text-foreground group-hover:translate-x-0.5 transition-all" />
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {/* Footer info bar */}
        {!loading && !error && filteredCandidates.length > 0 && (
          <div className="border-t border-border px-5 py-3 text-xs text-muted-foreground flex items-center justify-between">
            <span>
              Showing {filteredCandidates.length} of {candidates.length} candidates
            </span>
            <span className="text-[11px] text-muted-foreground/70">
              Click any candidate to review dossier and evaluations
            </span>
          </div>
        )}
      </div>

      <AddCandidateDialog
        open={addOpen}
        onOpenChange={setAddOpen}
        onSuccess={() => void loadCandidates()}
      />
      <ImportCandidatesDialog
        open={importOpen}
        onOpenChange={setImportOpen}
        onSuccess={() => void loadCandidates()}
      />
    </div>
  );
}
