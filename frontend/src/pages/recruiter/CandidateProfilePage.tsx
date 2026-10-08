import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, ArrowRight, Download, FileText, Mail, Phone, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { candidateService } from "@/services/candidateService";
import type { CandidateDetailItem, CandidateDocumentItem, CandidateStage } from "@/types/candidate";

function initials(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

function stageLabel(stage: CandidateStage): string {
  return stage.charAt(0) + stage.slice(1).toLowerCase();
}

function formatFileSize(size: number | null): string {
  if (!size || size <= 0) return "-";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${Math.round(size / 1024)} KB`;
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function documentLabel(document: CandidateDocumentItem): string {
  return document.fileName || document.fileType || "Document";
}

function isPdfDocument(document: CandidateDocumentItem | null): boolean {
  if (!document) return false;
  const byType = (document.fileType || "").toLowerCase().includes("pdf");
  const byName = (document.fileName || "").toLowerCase().endsWith(".pdf");
  const byUrl = (document.fileUrl || "").toLowerCase().includes(".pdf");
  return byType || byName || byUrl;
}

function toDownloadUrl(url: string): string {
  if (url.includes("res.cloudinary.com") && url.includes("/upload/")) {
    return url.replace("/upload/", "/upload/fl_attachment/");
  }
  return url;
}

function triggerDownload(url: string, fileName?: string): void {
  const anchor = document.createElement("a");
  anchor.href = toDownloadUrl(url);
  anchor.target = "_blank";
  anchor.rel = "noreferrer";
  if (fileName) {
    anchor.setAttribute("download", fileName);
  }
  document.body.appendChild(anchor);
  anchor.click();
  document.body.removeChild(anchor);
}

export default function CandidateProfilePage() {
  const { candidateId } = useParams<{ candidateId: string }>();
  const navigate = useNavigate();

  const [candidate, setCandidate] = useState<CandidateDetailItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [updatingStage, setUpdatingStage] = useState(false);

  const canTakeScreeningActions = candidate?.stage === "SCREENING";

  useEffect(() => {
    if (!candidateId) {
      setError("Invalid candidate id.");
      setLoading(false);
      return;
    }

    const loadDetail = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await candidateService.getCandidateDetail(candidateId);
        setCandidate(data);
      } catch {
        setError("Failed to load candidate profile.");
      } finally {
        setLoading(false);
      }
    };

    void loadDetail();
  }, [candidateId]);

  const mainDocument = useMemo(() => {
    if (!candidate?.documents?.length) return null;
    return candidate.documents.find((d) => isPdfDocument(d)) ?? candidate.documents[0];
  }, [candidate]);

  const handleStageUpdate = async (stage: CandidateStage) => {
    if (!candidateId || updatingStage) return;

    setUpdatingStage(true);
    setError("");
    try {
      const updated = await candidateService.updateCandidateStage(candidateId, stage);
      setCandidate(updated);
    } catch {
      setError("Failed to update stage.");
    } finally {
      setUpdatingStage(false);
    }
  };

  const handleMoveToInterview = () => {
    if (!candidateId) return;
    navigate(`/candidates/${candidateId}/schedule-interviews`);
  };

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading candidate profile...</p>;
  }

  if (error && !candidate) {
    return <p className="text-sm text-destructive">{error}</p>;
  }

  if (!candidate) {
    return <p className="text-sm text-muted-foreground">Candidate not found.</p>;
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="sm" onClick={() => navigate("/candidates")}> 
          <ArrowLeft className="h-4 w-4" />
          Back to Candidates
        </Button>
      </div>

      <section className="rounded-lg border border-border bg-card">
        <div className="flex flex-col gap-4 border-b border-border p-5 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-start gap-4">
            <span className="inline-flex h-14 w-14 items-center justify-center rounded-full bg-muted text-lg font-semibold text-primary">
              {initials(candidate.fullName)}
            </span>
            <div className="space-y-1">
              <h1 className="text-3xl font-semibold tracking-tight">Candidate Profile</h1>
              <p className="text-2xl font-semibold leading-none">{candidate.fullName}</p>
              <div className="flex flex-wrap items-center gap-4 text-sm text-muted-foreground">
                <span className="inline-flex items-center gap-1">
                  <Mail className="h-4 w-4" />
                  {candidate.email || "-"}
                </span>
                <span className="inline-flex items-center gap-1">
                  <Phone className="h-4 w-4" />
                  {candidate.phone || "-"}
                </span>
              </div>
              <div className="flex items-center gap-2 text-sm">
                <span className="text-muted-foreground">Applied for</span>
                <span className="font-semibold">{candidate.jobTitle || "-"}</span>
                <Badge variant="outline">{stageLabel(candidate.stage)}</Badge>
              </div>
            </div>
          </div>

          {canTakeScreeningActions && (
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => void handleStageUpdate("REJECTED")}
                disabled={updatingStage}
              >
                <X className="h-4 w-4" />
                Reject
              </Button>
              <Button
                onClick={handleMoveToInterview}
                disabled={updatingStage}
              >
                Move to Interview
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </div>

        <div className="flex items-center gap-8 px-5 py-3 text-sm">
          <button type="button" className="border-b-2 border-primary pb-2 font-medium text-foreground">
            Profile & CV
          </button>
          <button type="button" className="pb-2 text-muted-foreground" disabled>
            Scorecards
          </button>
          <button type="button" className="pb-2 text-muted-foreground" disabled>
            History
          </button>
        </div>
      </section>

      {error && <p className="text-sm text-destructive">{error}</p>}

      <section className="grid gap-4 xl:grid-cols-[2fr_1fr]">
        <div className="space-y-4">
          <div className="rounded-lg border border-border bg-card p-5">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-2xl font-semibold">CV Preview</h2>
              {mainDocument && (
                <button
                  type="button"
                  onClick={() => triggerDownload(mainDocument.fileUrl, mainDocument.fileName)}
                  className="inline-flex items-center gap-1 text-sm font-medium text-primary"
                >
                  <Download className="h-4 w-4" />
                  Download PDF
                </button>
              )}
            </div>

            {mainDocument && isPdfDocument(mainDocument) ? (
              <iframe
                title="CV Preview"
                src={mainDocument.fileUrl}
                className="h-72 w-full rounded-md border border-border"
              />
            ) : (
              <div className="flex h-72 flex-col items-center justify-center rounded-md border border-border bg-muted/20 text-center text-muted-foreground">
                <FileText className="mb-3 h-12 w-12" />
                <p className="text-base font-medium">Preview is available for PDF files</p>
                <p className="text-sm">{mainDocument?.fileName || "No document uploaded"}</p>
              </div>
            )}
          </div>

          <div className="rounded-lg border border-border bg-card p-5">
            <h2 className="mb-3 text-2xl font-semibold">Summary</h2>
            <p className="text-sm leading-7 text-muted-foreground">{candidate.summary || "-"}</p>
          </div>
        </div>

        <div className="space-y-4">
          <div className="rounded-lg border border-border bg-card p-5">
            <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-muted-foreground">Details</h3>
            <dl className="space-y-3 text-sm">
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Source</dt>
                <dd className="font-medium">{candidate.source || "-"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Location</dt>
                <dd className="font-medium">{candidate.location || "-"}</dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Experience</dt>
                <dd className="font-medium">
                  {candidate.experienceYears !== null && candidate.experienceYears !== undefined
                    ? `${candidate.experienceYears} Years`
                    : "-"}
                </dd>
              </div>
              <div className="flex justify-between gap-4">
                <dt className="text-muted-foreground">Current Company</dt>
                <dd className="font-medium">{candidate.currentCompany || "-"}</dd>
              </div>
            </dl>
          </div>

          <div className="rounded-lg border border-border bg-card p-5">
            <h3 className="mb-4 text-sm font-semibold uppercase tracking-wide text-muted-foreground">Documents</h3>
            <div className="space-y-2">
              {candidate.documents.length === 0 ? (
                <p className="text-sm text-muted-foreground">No documents.</p>
              ) : (
                candidate.documents.map((document) => (
                  <button
                    type="button"
                    key={document.documentId}
                    onClick={() => triggerDownload(document.fileUrl, document.fileName)}
                    className="flex items-center justify-between rounded-md border border-border px-3 py-2 hover:bg-muted/20"
                  >
                    <div>
                      <p className="text-sm font-medium">{documentLabel(document)}</p>
                      <p className="text-xs text-muted-foreground">{formatFileSize(document.fileSizeBytes)}</p>
                    </div>
                    <Download className="h-4 w-4 text-muted-foreground" />
                  </button>
                ))
              )}
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
