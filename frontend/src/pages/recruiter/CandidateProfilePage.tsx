import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, ArrowRight, Download, FileText, Mail, Phone, X, Building, MapPin, Briefcase } from "lucide-react";
import { Button } from "@/components/ui/button";
import { candidateService } from "@/services/candidateService";
import type { CandidateDetailItem, CandidateDocumentItem, CandidateStage } from "@/types/candidate";
import { StageBadge } from "@/components/shared/StageBadge";

function initials(name: string): string {
  return name
    .split(" ")
    .map((part) => part[0])
    .slice(0, 2)
    .join("")
    .toUpperCase();
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
    if (!candidate) return;
    setUpdatingStage(true);
    setError("");
    try {
      await candidateService.updateCandidateStage(candidate.candidateId, stage);
      setCandidate({ ...candidate, stage });
    } catch {
      setError(`Failed to update candidate stage to ${stage}.`);
    } finally {
      setUpdatingStage(false);
    }
  };

  const handleMoveToInterview = async () => {
    await handleStageUpdate("INTERVIEW");
    navigate(`/candidates/${candidateId}/schedule-interview`);
  };

  if (loading) {
    return (
      <div className="flex min-h-[300px] items-center justify-center gap-2 text-muted-foreground">
        <div className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        <span className="text-sm">Loading candidate profile…</span>
      </div>
    );
  }

  if (!candidate) {
    return (
      <div className="rounded-xl border border-destructive/20 bg-destructive/5 p-6 text-destructive">
        <p className="font-semibold text-sm">Candidate not found</p>
        <p className="mt-1 text-xs opacity-90">{error || "Unable to locate candidate records."}</p>
        <Button
          variant="outline"
          size="sm"
          className="mt-4"
          onClick={() => navigate("/candidates")}
        >
          Return to Candidates
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Back button */}
      <div>
        <Button
          variant="ghost"
          size="sm"
          className="h-8 gap-1.5 text-xs text-muted-foreground hover:text-foreground"
          onClick={() => navigate("/candidates")}
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          <span>Back to candidate roster</span>
        </Button>
      </div>

      {/* Hero Header Dossier */}
      <div className="rounded-xl border border-border bg-card p-6 shadow-xs">
        <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-start gap-4">
            <div className="flex h-16 w-16 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-xl font-bold text-primary ring-1 ring-primary/20">
              {initials(candidate.fullName)}
            </div>
            <div className="space-y-1.5">
              <div className="flex flex-wrap items-center gap-3">
                <h1 className="text-2xl font-bold tracking-tight text-foreground">
                  {candidate.fullName}
                </h1>
                <StageBadge stage={candidate.stage} />
              </div>

              <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted-foreground">
                <span className="font-medium text-foreground/90">
                  Target: {candidate.jobTitle || "Open Application"}
                </span>
                {candidate.email && (
                  <span className="inline-flex items-center gap-1">
                    <Mail className="h-3.5 w-3.5 text-muted-foreground/70" />
                    {candidate.email}
                  </span>
                )}
                {candidate.phone && (
                  <span className="inline-flex items-center gap-1">
                    <Phone className="h-3.5 w-3.5 text-muted-foreground/70" />
                    {candidate.phone}
                  </span>
                )}
              </div>
            </div>
          </div>

          {canTakeScreeningActions && (
            <div className="flex items-center gap-2 self-start lg:self-center">
              <Button
                variant="outline"
                size="sm"
                className="h-9 gap-1.5 text-destructive hover:bg-destructive/10 hover:text-destructive"
                onClick={() => void handleStageUpdate("REJECTED")}
                disabled={updatingStage}
              >
                <X className="h-4 w-4" />
                Reject
              </Button>
              <Button
                size="sm"
                className="h-9 gap-1.5 shadow-xs"
                onClick={handleMoveToInterview}
                disabled={updatingStage}
              >
                <span>Move to Interview</span>
                <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-destructive/20 bg-destructive/5 p-4 text-xs text-destructive">
          {error}
        </div>
      )}

      {/* Main Grid: CV & Details */}
      <div className="grid gap-6 lg:grid-cols-12">
        {/* Left Column: CV Preview & Summary (8 cols) */}
        <div className="space-y-6 lg:col-span-8">
          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <div className="mb-4 flex items-center justify-between border-b border-border pb-3">
              <div>
                <h2 className="text-sm font-semibold text-foreground">Resume Document</h2>
                <p className="text-xs text-muted-foreground mt-0.5">Official submitted application dossier</p>
              </div>
              {mainDocument && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => triggerDownload(mainDocument.fileUrl, mainDocument.fileName)}
                  className="h-8 gap-1.5 text-xs"
                >
                  <Download className="h-3.5 w-3.5" />
                  Download file
                </Button>
              )}
            </div>

            {mainDocument && isPdfDocument(mainDocument) ? (
              <iframe
                title="CV Preview"
                src={mainDocument.fileUrl}
                className="h-[520px] w-full rounded-lg border border-border"
              />
            ) : (
              <div className="flex h-64 flex-col items-center justify-center rounded-lg border border-dashed border-border bg-muted/20 text-center text-muted-foreground">
                <FileText className="mb-2 h-10 w-10 opacity-30" />
                <p className="text-sm font-medium text-foreground">
                  {mainDocument ? "Document preview not available inline" : "No resume document attached"}
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  {mainDocument?.fileName || "Upload candidate resume via application update."}
                </p>
              </div>
            )}
          </div>

          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <h2 className="text-sm font-semibold text-foreground mb-1">Candidate Profile Summary</h2>
            <p className="text-xs text-muted-foreground mb-3">Extracted background and professional highlights</p>
            <div className="rounded-lg bg-muted/30 p-4 border border-border/50 text-xs leading-relaxed text-foreground/90 whitespace-pre-line">
              {candidate.summary || "No executive summary submitted for this profile."}
            </div>
          </div>
        </div>

        {/* Right Column: Metadata & Attachments (4 cols) */}
        <div className="space-y-6 lg:col-span-4">
          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <h3 className="text-xs font-semibold text-muted-foreground mb-3">
              Candidate Attributes
            </h3>
            <div className="divide-y divide-border text-xs">
              <div className="flex items-center justify-between py-2.5">
                <span className="flex items-center gap-1.5 text-muted-foreground">
                  <Briefcase className="h-3.5 w-3.5" />
                  Application Source
                </span>
                <span className="font-medium text-foreground">{candidate.source || "Direct Apply"}</span>
              </div>
              <div className="flex items-center justify-between py-2.5">
                <span className="flex items-center gap-1.5 text-muted-foreground">
                  <MapPin className="h-3.5 w-3.5" />
                  Location
                </span>
                <span className="font-medium text-foreground">{candidate.location || "Not specified"}</span>
              </div>
              <div className="flex items-center justify-between py-2.5">
                <span className="text-muted-foreground">Experience</span>
                <span className="font-medium text-foreground">
                  {candidate.experienceYears != null
                    ? `${candidate.experienceYears} Years`
                    : "Not specified"}
                </span>
              </div>
              <div className="flex items-center justify-between py-2.5">
                <span className="flex items-center gap-1.5 text-muted-foreground">
                  <Building className="h-3.5 w-3.5" />
                  Current Employer
                </span>
                <span className="font-medium text-foreground">{candidate.currentCompany || "Not specified"}</span>
              </div>
            </div>
          </div>

          <div className="rounded-xl border border-border bg-card p-5 shadow-xs">
            <h3 className="text-xs font-semibold text-muted-foreground mb-3">
              Submitted Documents
            </h3>
            <div className="space-y-2">
              {candidate.documents.length === 0 ? (
                <p className="text-xs text-muted-foreground py-2">No attached documents.</p>
              ) : (
                candidate.documents.map((document) => (
                  <div
                    key={document.documentId}
                    className="flex items-center justify-between rounded-lg border border-border p-2.5 text-xs transition-colors hover:bg-muted/40"
                  >
                    <div className="min-w-0 pr-2">
                      <p className="font-medium text-foreground truncate">{documentLabel(document)}</p>
                      <p className="text-[11px] text-muted-foreground font-mono-numbers">
                        {formatFileSize(document.fileSizeBytes)}
                      </p>
                    </div>
                    <Button
                      variant="ghost"
                      size="icon"
                      className="h-7 w-7 shrink-0 text-muted-foreground hover:text-foreground"
                      onClick={() => triggerDownload(document.fileUrl, document.fileName)}
                    >
                      <Download className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
