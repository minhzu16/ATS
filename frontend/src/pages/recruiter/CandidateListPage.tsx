import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Plus, Star, MoreHorizontal, Upload } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { candidateService } from "@/services/candidateService";
import type { CandidateListItem, CandidateStage } from "@/types/candidate";
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

    return byStage.filter((item) => item.fullName.toLowerCase().includes(normalizedSearch));
  }, [candidates, search, jobFilter, stageFilter]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Candidate List</h1>
      </div>

      <section className="space-y-2">
        <h2 className="text-xl font-semibold">Candidates</h2>
        <p className="text-sm text-muted-foreground">A list of all candidates across all jobs.</p>
      </section>

      <section className="rounded-lg border border-border bg-card p-4">
        <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div className="relative w-full md:max-w-xl">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search candidates..."
              className="pl-9"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <select
              value={jobFilter}
              onChange={(event) => setJobFilter(event.target.value)}
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
            >
              <option value="ALL">All Jobs</option>
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
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
            >
              <option value="ALL">All Stages</option>
              {STAGES.map((stage) => (
                <option key={stage} value={stage}>
                  {stageLabel(stage)}
                </option>
              ))}
            </select>
            <Button size="sm" className="h-9" variant="outline" onClick={() => setImportOpen(true)}>
              <Upload className="h-4 w-4" />
              Import CSV
            </Button>
            <Button size="sm" className="h-9" onClick={() => setAddOpen(true)}>
              <Plus className="h-4 w-4" />
              Add Candidate
            </Button>
          </div>
        </div>

        {error && <p className="py-4 text-sm text-destructive">{error}</p>}

        {!error && (
          <div className="overflow-hidden rounded-lg border border-border">
            <table className="min-w-full divide-y divide-border">
              <thead className="bg-muted/30">
                <tr>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Name</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Applied For</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Stage</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Rating</th>
                  <th className="px-5 py-3 text-left text-sm font-semibold">Applied Date</th>
                  <th className="px-5 py-3 text-right text-sm font-semibold" />
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {loading ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                      Loading candidates...
                    </td>
                  </tr>
                ) : filteredCandidates.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                      No candidates found.
                    </td>
                  </tr>
                ) : (
                  filteredCandidates.map((item) => (
                    <tr
                      key={`${item.candidateId}-${item.jobTitle}`}
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
                      <td className="px-5 py-4 text-sm text-muted-foreground">{item.jobTitle}</td>
                      <td className="px-5 py-4">
                        <Badge variant="outline">{stageLabel(item.stage)}</Badge>
                      </td>
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-1 text-sm text-muted-foreground">
                          <Star className="h-4 w-4" />
                          {item.rating ?? "-"}
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
        )}
      </section>

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
