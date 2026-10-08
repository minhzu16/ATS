import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { offerService } from "@/services/offerService";
import type { CreateOfferRequest } from "@/types/offer";
import { candidateService } from "@/services/candidateService";
import type { CandidateListItem } from "@/types/candidate";

export default function OfferFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [form, setForm] = useState<CreateOfferRequest>({
    candidateId: "",
    salary: 0,
    positionTitle: "",
    startDate: null,
    benefits: null,
    notes: null,
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [candidates, setCandidates] = useState<CandidateListItem[]>([]);
  const [editCandidateName, setEditCandidateName] = useState("");

  const interviewCandidates = useMemo(
    () => candidates.filter((c) => c.stage === "INTERVIEW"),
    [candidates],
  );

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        if (id) {
          const offer = await offerService.getOffer(id);
          setForm({
            candidateId: "",
            salary: offer.salary,
            positionTitle: offer.positionTitle,
            startDate: offer.startDate,
            benefits: offer.benefits,
            notes: offer.notes,
          });
          setEditCandidateName(offer.candidateName);
        } else {
          const list = await candidateService.getCandidates();
          setCandidates(list);
        }
      } catch {
        alert("Failed to load data.");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [id]);

  const handleChange = (field: keyof CreateOfferRequest, value: string | number | null) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    if ((!isEdit && !form.candidateId) || !form.positionTitle || form.salary <= 0) {
      alert("Please fill all required fields.");
      return;
    }
    setSaving(true);
    try {
      if (isEdit && id) {
        await offerService.updateDraft(id, form);
      } else {
        await offerService.createDraft(form);
      }
      navigate("/offers");
    } catch (err: unknown) {
      const msg =
        axios.isAxiosError(err) &&
        err.response?.data &&
        typeof err.response.data === "object" &&
        "message" in err.response.data
          ? String((err.response.data as { message: string }).message)
          : "Failed to save offer.";
      alert(msg);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading offer...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">
          {isEdit ? "Edit Offer" : "Create Offer"}
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {isEdit
            ? "Update the offer details"
            : "A new offer can only be created for candidates whose current stage is INTERVIEW."}
        </p>
      </div>

      <section className="mx-auto max-w-2xl rounded-lg border border-border bg-card p-6 shadow-sm">
        <div className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium">Candidate *</label>
            {isEdit ? (
              <Input value={editCandidateName} disabled />
            ) : interviewCandidates.length === 0 ? (
              <p className="rounded-md border border-border bg-muted/40 px-3 py-2 text-sm text-muted-foreground">
                No candidates in INTERVIEW stage. Move a candidate to INTERVIEW in the pipeline before creating an offer.
              </p>
            ) : (
              <select
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                value={form.candidateId}
                onChange={(e) => handleChange("candidateId", e.target.value)}
              >
                <option value="">Select a candidate (INTERVIEW only)</option>
                {interviewCandidates.map((c) => (
                  <option key={c.candidateId} value={c.candidateId}>
                    {c.fullName} ({c.jobTitle})
                  </option>
                ))}
              </select>
            )}
          </div>
          <div>
            <label className="mb-1 block text-sm font-medium">Position Title *</label>
            <Input
              value={form.positionTitle}
              onChange={(e) => handleChange("positionTitle", e.target.value)}
              placeholder="e.g. Senior Software Engineer"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Salary (USD) *</label>
            <Input
              type="number"
              value={form.salary || ""}
              onChange={(e) => handleChange("salary", parseFloat(e.target.value) || 0)}
              placeholder="e.g. 85000"
              min={0}
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Start Date</label>
            <Input
              type="date"
              value={form.startDate ?? ""}
              onChange={(e) => handleChange("startDate", e.target.value || null)}
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Benefits</label>
            <textarea
              value={form.benefits ?? ""}
              onChange={(e) => handleChange("benefits", e.target.value || null)}
              placeholder="Health insurance, 401k, etc."
              rows={3}
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium">Notes</label>
            <textarea
              value={form.notes ?? ""}
              onChange={(e) => handleChange("notes", e.target.value || null)}
              placeholder="Additional notes..."
              rows={3}
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
            />
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <Button variant="outline" onClick={() => navigate("/offers")}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={saving || (!isEdit && interviewCandidates.length === 0)}
          >
            {saving ? "Saving..." : isEdit ? "Update Offer" : "Create Draft"}
          </Button>
        </div>
      </section>
    </div>
  );
}
