import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Trophy, Medal, Award } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { evaluationService } from "@/services/evaluationService";
import type { CandidateRanking } from "@/types/evaluation";

const RANK_STYLES: Record<number, { bg: string; text: string; icon: typeof Trophy }> = {
  1: { bg: "bg-yellow-50", text: "text-yellow-700", icon: Trophy },
  2: { bg: "bg-gray-100", text: "text-gray-600", icon: Medal },
  3: { bg: "bg-orange-50", text: "text-orange-700", icon: Award },
};

function stageLabel(stage: string): string {
  return stage.charAt(0) + stage.slice(1).toLowerCase();
}

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
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Candidate Ranking</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Candidates ranked by interview performance for this job position
        </p>
      </div>

      {/* Summary Stats */}
      {!loading && rankings.length > 0 && (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
          <div className="rounded-lg border border-border bg-card p-5 shadow-sm">
            <p className="text-sm text-muted-foreground">Total Candidates</p>
            <p className="mt-1 text-2xl font-bold">{rankings.length}</p>
          </div>
          <div className="rounded-lg border border-border bg-card p-5 shadow-sm">
            <p className="text-sm text-muted-foreground">Highest Score</p>
            <p className="mt-1 text-2xl font-bold text-green-600">
              {rankings[0]?.overallScore.toFixed(1) ?? "—"}
            </p>
          </div>
          <div className="rounded-lg border border-border bg-card p-5 shadow-sm">
            <p className="text-sm text-muted-foreground">Average Score</p>
            <p className="mt-1 text-2xl font-bold">
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
                        <Badge variant="outline">{stageLabel(r.stage)}</Badge>
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
