import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Trophy, Medal, Award, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { evaluationService } from "@/services/evaluationService";
import type { CandidateRanking } from "@/types/evaluation";
import { StageBadge } from "@/components/shared/StageBadge";

const RANK_STYLES: Record<number, { bg: string; text: string; icon: typeof Trophy }> = {
  1: { bg: "bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300", text: "text-amber-700", icon: Trophy },
  2: { bg: "bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300", text: "text-slate-600", icon: Medal },
  3: { bg: "bg-orange-50 text-orange-700 dark:bg-orange-950/40 dark:text-orange-300", text: "text-orange-700", icon: Award },
};

export default function CandidateRankingPage() {
  const { jobId } = useParams<{ jobId: string }>();
  const navigate = useNavigate();
  const [rankings, setRankings] = useState<CandidateRanking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!jobId) return;
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await evaluationService.getCandidateRanking(jobId);
        setRankings(data);
      } catch {
        setError("Failed to load rankings.");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [jobId]);

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <Button
            variant="ghost"
            size="sm"
            className="mb-2 h-8 gap-1.5 text-xs text-muted-foreground hover:text-foreground -ml-2"
            onClick={() => navigate(jobId ? `/jobs/${jobId}` : "/jobs")}
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            <span>Back to job opening</span>
          </Button>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Candidate Ranking</h1>
          <p className="mt-1 text-xs text-muted-foreground">
            Candidates ordered by cumulative interview evaluation score
          </p>
        </div>
      </div>

      {/* Summary Stats */}
      {!loading && rankings.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <p className="text-xs font-medium text-muted-foreground">Evaluated Candidates</p>
            <p className="mt-1 text-2xl font-bold font-mono-numbers text-foreground">{rankings.length}</p>
          </div>
          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <p className="text-xs font-medium text-muted-foreground">Top Score</p>
            <p className="mt-1 text-2xl font-bold font-mono-numbers text-emerald-600">
              {rankings[0]?.overallScore.toFixed(1) ?? "—"}
            </p>
          </div>
          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <p className="text-xs font-medium text-muted-foreground">Average Score</p>
            <p className="mt-1 text-2xl font-bold font-mono-numbers text-foreground">
              {rankings.length > 0
                ? (rankings.reduce((sum, r) => sum + r.overallScore, 0) / rankings.length).toFixed(1)
                : "—"}
            </p>
          </div>
        </div>
      )}

      {error && <p className="py-4 text-sm text-destructive">{error}</p>}

      {/* Ranking Table */}
      <section className="rounded-lg border border-border bg-card p-4">
        <div className="overflow-hidden rounded-lg border border-border">
          <table className="min-w-full divide-y divide-border">
            <thead className="bg-muted/30">
              <tr>
                <th className="px-5 py-3 text-left text-sm font-semibold">Rank</th>
                <th className="px-5 py-3 text-left text-sm font-semibold">Candidate</th>
                <th className="px-5 py-3 text-left text-sm font-semibold">Score</th>
                <th className="px-5 py-3 text-left text-sm font-semibold">Experience</th>
                <th className="px-5 py-3 text-left text-sm font-semibold">Stage</th>
                <th className="px-5 py-3 text-left text-sm font-semibold">Applied</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                    Loading rankings...
                  </td>
                </tr>
              ) : rankings.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-5 py-10 text-center text-sm text-muted-foreground">
                    No candidates with scores found.
                  </td>
                </tr>
              ) : (
                rankings.map((r) => {
                  const rankStyle = RANK_STYLES[r.rank];
                  const RankIcon = rankStyle?.icon;
                  return (
                    <tr
                      key={r.applicationId}
                      className="cursor-pointer bg-background transition-colors hover:bg-muted/20"
                      onClick={() => navigate(`/candidates/${r.candidateId}`)}
                    >
                      <td className="px-5 py-4">
                        {rankStyle ? (
                          <span
                            className={`inline-flex h-8 w-8 items-center justify-center rounded-full ${rankStyle.bg} ${rankStyle.text}`}
                          >
                            {RankIcon && <RankIcon className="h-4 w-4" />}
                          </span>
                        ) : (
                          <span className="inline-flex h-8 w-8 items-center justify-center rounded-full bg-muted text-sm font-semibold text-muted-foreground">
                            {r.rank}
                          </span>
                        )}
                      </td>
                      <td className="px-5 py-4 font-medium">{r.candidateName}</td>
                      <td className="px-5 py-4">
                        <span
                          className={`text-sm font-semibold ${
                            r.overallScore >= 7
                              ? "text-green-600"
                              : r.overallScore >= 4
                                ? "text-yellow-600"
                                : "text-red-600"
                          }`}
                        >
                          {r.overallScore.toFixed(1)}
                        </span>
                      </td>
                      <td className="px-5 py-4 text-sm text-muted-foreground">
                        {r.experienceYears != null ? `${r.experienceYears} years` : "—"}
                      </td>
                      <td className="px-5 py-4">
                        <StageBadge stage={r.stage} />
                      </td>
                      <td className="px-5 py-4 text-sm text-muted-foreground">
                        {new Date(r.appliedAt).toLocaleDateString()}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
