import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { FileText, CheckCircle2, XCircle, Send, Download, Clock, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { offerService } from "@/services/offerService";
import { onboardingService } from "@/services/onboardingService";
import type { Offer, OfferApproval, OfferStatus, ApprovalStatus } from "@/types/offer";
import type { CandidateStage } from "@/types/candidate";
import { useAuth } from "@/hooks/useAuth";
import { toast } from "sonner";

function stageLabel(stage: CandidateStage): string {
  return stage.charAt(0) + stage.slice(1).toLowerCase();
}

const STATUS_STYLES: Record<OfferStatus, string> = {
  DRAFT: "bg-gray-100 text-gray-700 ring-1 ring-gray-300",
  PENDING_APPROVAL: "bg-yellow-50 text-yellow-800 ring-1 ring-yellow-600/20",
  APPROVED: "bg-green-50 text-green-700 ring-1 ring-green-700/10",
  REJECTED: "bg-red-50 text-red-700 ring-1 ring-red-700/10",
  SENT: "bg-blue-50 text-blue-700 ring-1 ring-blue-700/10",
  ACCEPTED: "bg-emerald-50 text-emerald-700 ring-1 ring-emerald-700/10",
  DECLINED: "bg-orange-50 text-orange-700 ring-1 ring-orange-700/10",
};

export default function OfferDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [offer, setOffer] = useState<Offer | null>(null);
  const [history, setHistory] = useState<OfferApproval[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [approvalComment, setApprovalComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [hasOnboarding, setHasOnboarding] = useState(false);

  const isHrManager = user?.role === "HR_MANAGER";
  const canSubmitForApproval = user?.role === "HR" || user?.role === "HR_MANAGER";

  const loadOffer = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [offerData, historyData] = await Promise.all([
        offerService.getOffer(id),
        offerService.getApprovalHistory(id),
      ]);
      setOffer(offerData);
      setHistory(historyData);
      if (offerData.applicationId) {
        try {
          await onboardingService.getByApplicationId(offerData.applicationId);
          setHasOnboarding(true);
        } catch {
          setHasOnboarding(false);
        }
      }
    } catch {
      setError("Failed to load offer details.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadOffer();
  }, [id]);

  const handleSubmitForApproval = async () => {
    if (!id) return;
    setSubmitting(true);
    try {
      await offerService.submitForApproval(id);
      await loadOffer();
    } catch {
      alert("Failed to submit for approval.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleApproval = async (status: ApprovalStatus) => {
    if (!id) return;
    setSubmitting(true);
    try {
      await offerService.approveOrReject(id, { status, comment: approvalComment });
      setApprovalComment("");
      toast.success(status === "APPROVED" ? "Offer approved successfully." : "Offer rejected.");
      await loadOffer();
    } catch {
      toast.error("Failed to process approval.");
    } finally {
      setSubmitting(false);
    }
  };

  const handlePreviewPdf = async () => {
    if (!id) return;
    try {
      const blob = await offerService.getOfferPdfBlob(id);
      const url = window.URL.createObjectURL(blob);
      window.open(url, "_blank");
    } catch {
      toast.error("Failed to preview PDF.");
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading offer...
      </div>
    );
  }

  if (error || !offer) {
    return <p className="py-8 text-center text-sm text-destructive">{error || "Offer not found."}</p>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">Offer Details</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            Offer for {offer.candidateName}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {offer.applicationStage && (
            <Badge variant="secondary" className="text-sm">
              Candidate stage: {stageLabel(offer.applicationStage)}
            </Badge>
          )}
          <Badge variant="outline" className={`text-sm ${STATUS_STYLES[offer.status]}`}>
            {offer.status.replace(/_/g, " ")}
          </Badge>
        </div>
      </div>

      <section className="rounded-lg border border-border bg-muted/30 p-4 text-sm text-muted-foreground">
        <p className="font-medium text-foreground">Offer workflow and pipeline stages</p>
        <ul className="mt-2 list-inside list-disc space-y-1">
          <li>
            <strong className="text-foreground">Submit for approval</strong> (while the candidate is in{" "}
            <strong className="text-foreground">Interview</strong>): moves the candidate to{" "}
            <strong className="text-foreground">Offer</strong> and sends the offer to the HR Manager.
          </li>
          <li>
            <strong className="text-foreground">Approve</strong> (HR Manager): moves the candidate from{" "}
            <strong className="text-foreground">Offer</strong> to <strong className="text-foreground">Hired</strong>.
          </li>
          <li>
            <strong className="text-foreground">Reject</strong> (HR Manager): moves the candidate from{" "}
            <strong className="text-foreground">Offer</strong> to <strong className="text-foreground">Rejected</strong>.
          </li>
        </ul>
      </section>

      {/* Offer Info Card */}
      <section className="rounded-lg border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-4 text-lg font-semibold text-card-foreground">
          <FileText className="mb-0.5 mr-2 inline-block h-5 w-5" />
          Offer Information
        </h3>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <InfoRow label="Candidate" value={offer.candidateName} />
          <InfoRow label="Job" value={offer.jobTitle} />
          <InfoRow label="Position" value={offer.positionTitle} />
          <InfoRow label="Salary" value={`$${offer.salary.toLocaleString()}`} />
          <InfoRow label="Start Date" value={offer.startDate ?? "—"} />
          <InfoRow label="Created" value={new Date(offer.createdAt).toLocaleDateString()} />
        </div>
        {offer.benefits && (
          <div className="mt-4">
            <p className="text-sm font-medium text-muted-foreground">Benefits</p>
            <p className="mt-1 whitespace-pre-wrap text-sm">{offer.benefits}</p>
          </div>
        )}
        {offer.notes && (
          <div className="mt-4">
            <p className="text-sm font-medium text-muted-foreground">Notes</p>
            <p className="mt-1 whitespace-pre-wrap text-sm">{offer.notes}</p>
          </div>
        )}
      </section>

      {/* Actions */}
      <section className="flex flex-wrap gap-3">
        {offer.status === "DRAFT" && (
          <>
            <Button variant="outline" onClick={() => navigate(`/offers/${id}/edit`)}>
              Edit Draft
            </Button>
            {canSubmitForApproval && (
              <Button onClick={handleSubmitForApproval} disabled={submitting}>
                <Send className="h-4 w-4" />
                {submitting ? "Submitting..." : "Submit for Approval"}
              </Button>
            )}
          </>
        )}
        {(offer.status === "APPROVED" || offer.status === "SENT") && (
          <>
            <Button variant="outline" onClick={handlePreviewPdf}>
              <Download className="h-4 w-4" />
              Preview PDF
            </Button>
            {offer.applicationId && (
              <Button
                variant="outline"
                onClick={() => navigate(`/onboarding/application/${offer.applicationId}`)}
              >
                {hasOnboarding ? (
                  <>
                    View Onboarding
                  </>
                ) : (
                  <>
                    <Plus className="h-4 w-4" />
                    Create Onboarding
                  </>
                )}
              </Button>
            )}
          </>
        )}
      </section>

      {/* HR Manager Approval */}
      {isHrManager && offer.status === "PENDING_APPROVAL" && (
        <section className="rounded-lg border border-border bg-card p-6 shadow-sm">
          <h3 className="mb-4 text-lg font-semibold text-card-foreground">Approval Action</h3>
          <div className="space-y-3">
            <div>
              <label className="mb-1 block text-sm font-medium">Comment</label>
              <textarea
                value={approvalComment}
                onChange={(e) => setApprovalComment(e.target.value)}
                placeholder="Add a comment (optional)..."
                rows={3}
                className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
              />
            </div>
            <div className="flex gap-2">
              <Button
                className="bg-green-600 text-white hover:bg-green-700"
                onClick={() => handleApproval("APPROVED")}
                disabled={submitting}
              >
                <CheckCircle2 className="h-4 w-4" />
                Approve
              </Button>
              <Button
                variant="destructive"
                onClick={() => handleApproval("REJECTED")}
                disabled={submitting}
              >
                <XCircle className="h-4 w-4" />
                Reject
              </Button>
            </div>
          </div>
        </section>
      )}

      {/* Approval History */}
      <section className="rounded-lg border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-4 text-lg font-semibold text-card-foreground">
          <Clock className="mb-0.5 mr-2 inline-block h-5 w-5" />
          Approval History
        </h3>
        {history.length === 0 ? (
          <p className="text-sm text-muted-foreground">No approval actions yet.</p>
        ) : (
          <div className="space-y-3">
            {history.map((h) => (
              <div key={h.id} className="flex items-start gap-3 rounded-lg border border-border p-4">
                {h.status === "APPROVED" ? (
                  <CheckCircle2 className="mt-0.5 h-5 w-5 shrink-0 text-green-600" />
                ) : (
                  <XCircle className="mt-0.5 h-5 w-5 shrink-0 text-red-600" />
                )}
                <div>
                  <p className="text-sm font-medium">
                    {h.approvedByName}{" "}
                    <span className={h.status === "APPROVED" ? "text-green-600" : "text-red-600"}>
                      {h.status.toLowerCase()}
                    </span>
                  </p>
                  {h.comment && (
                    <p className="mt-1 text-sm text-muted-foreground">{h.comment}</p>
                  )}
                  <p className="mt-1 text-xs text-muted-foreground">
                    {new Date(h.createdAt).toLocaleString()}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-sm font-medium text-muted-foreground">{label}</p>
      <p className="mt-0.5 text-sm font-medium">{value}</p>
    </div>
  );
}
