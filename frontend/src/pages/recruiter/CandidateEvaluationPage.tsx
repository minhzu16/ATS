import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, Star, Users, Calendar, Award, FileText } from "lucide-react";
import { Button } from "@/components/ui/button";
import { evaluationService } from "@/services/evaluationService";
import type { CandidateEvaluation } from "@/types/evaluation";

export default function CandidateEvaluationPage() {
  const { applicationId } = useParams<{ applicationId: string }>();
  const navigate = useNavigate();
  const [data, setData] = useState<CandidateEvaluation | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!applicationId) return;
    const fetchSummary = async () => {
      setLoading(true);
      try {
        const result = await evaluationService.getEvaluationSummary(applicationId);
        setData(result);
      } catch {
        setError("Failed to load evaluation summary.");
      } finally {
        setLoading(false);
      }
    };
    void fetchSummary();
  }, [applicationId]);

  if (loading) {
    return <div className="p-8 text-center text-muted-foreground">Loading evaluation summary...</div>;
  }

  if (error || !data) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" onClick={() => navigate(-1)} className="-ml-3">
          <ArrowLeft className="mr-2 h-4 w-4" /> Back
        </Button>
        <div className="p-8 text-center text-destructive">{error || "Data not found."}</div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl space-y-6">
      <Button variant="ghost" onClick={() => navigate(-1)} className="-ml-3 text-muted-foreground">
        <ArrowLeft className="mr-2 h-4 w-4" /> Back to Application
      </Button>

      <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">{data.candidateName}</h1>
          <p className="mt-1 flex items-center text-muted-foreground">
            <Award className="mr-2 h-4 w-4" /> Applying for:{" "}
            <span className="ml-1 font-medium text-foreground">{data.jobTitle}</span>
          </p>
        </div>
        <div className="flex items-center gap-3 rounded-lg border border-border bg-card px-5 py-3 shadow-sm">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
            <Star className="h-6 w-6 fill-current" />
          </div>
          <div>
            <p className="text-sm font-medium text-muted-foreground">Overall Score</p>
            <p className="text-2xl font-bold">{data.overallScore.toFixed(1)} <span className="text-sm font-normal text-muted-foreground">/ 10</span></p>
          </div>
        </div>
      </div>

      <h2 className="text-xl font-semibold tracking-tight">Interview Rounds ({data.interviews.length})</h2>

      {data.interviews.length === 0 ? (
        <div className="rounded-lg border border-dashed border-border p-12 text-center text-muted-foreground">
          <FileText className="mx-auto mb-3 h-10 w-10 opacity-20" />
          <p>No completed interviews found yet.</p>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {data.interviews.map((iv, idx) => (
            <div key={iv.interviewId} className="flex flex-col rounded-lg border border-border bg-card p-5 shadow-sm transition-shadow hover:shadow-md">
              <div className="mb-4 flex items-center justify-between">
                <Badge variant="outline" className="bg-muted text-foreground">
                  Round {idx + 1}
                </Badge>
                <div className="flex items-center gap-1 font-semibold text-primary">
                  <Star className="h-4 w-4 fill-current" />
                  <span>{iv.averageScore.toFixed(1)}</span>
                </div>
              </div>
              
              <div className="mb-4 space-y-2 text-sm text-muted-foreground">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4" />
                  <span>{new Date(iv.scheduledAt).toLocaleDateString()} at {new Date(iv.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4" />
                  <span>{iv.interviewerCount} {iv.interviewerCount === 1 ? 'Interviewer' : 'Interviewers'}</span>
                </div>
              </div>

              {iv.scorecards && iv.scorecards.length > 0 && (
                <div className="mt-auto border-t border-border pt-3 space-y-2">
                  <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Scorecards</p>
                  {iv.scorecards.map((sc) => (
                    <div key={sc.scorecardId} className="flex items-center justify-between text-sm">
                      <span className="text-foreground truncate max-w-[60%]">{sc.interviewerName ?? 'Unknown'}</span>
                      <span className={`font-semibold ${sc.overallScore != null && sc.overallScore >= 7 ? 'text-green-600' : sc.overallScore != null && sc.overallScore >= 4 ? 'text-yellow-600' : 'text-red-500'}`}>
                        {sc.overallScore != null ? sc.overallScore : '—'}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function Badge({ children, className }: any) {
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${className}`}>
      {children}
    </span>
  );
}
