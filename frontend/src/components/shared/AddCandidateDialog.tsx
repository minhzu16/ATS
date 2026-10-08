import { useState, useEffect } from "react";
import { Loader2, Upload, X } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { candidateService } from "@/services/candidateService";
import { jobService } from "@/services/jobService";
import type { JobOption } from "@/types/job";

interface AddCandidateDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

const SOURCES = ["LinkedIn", "Referral", "Website", "Job Fair", "Agency", "Other"];

export default function AddCandidateDialog({ open, onOpenChange, onSuccess }: AddCandidateDialogProps) {
  const [jobs, setJobs] = useState<JobOption[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [documents, setDocuments] = useState<File[]>([]);

  const [form, setForm] = useState({
    fullName: "",
    email: "",
    phone: "",
    currentCompany: "",
    source: "",
    location: "",
    experienceYears: "",
    summary: "",
    jobId: "",
  });

  useEffect(() => {
    if (open) {
      jobService.getJobs().then(setJobs).catch(() => setJobs([]));
    }
  }, [open]);

  function handleChange(e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  function handleFiles(e: React.ChangeEvent<HTMLInputElement>) {
    if (e.target.files) {
      setDocuments(Array.from(e.target.files));
    }
  }

  function removeFile(index: number) {
    setDocuments((prev) => prev.filter((_, i) => i !== index));
  }

  function resetForm() {
    setForm({ fullName: "", email: "", phone: "", currentCompany: "", source: "", location: "", experienceYears: "", summary: "", jobId: "" });
    setDocuments([]);
    setError("");
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.fullName.trim()) {
      setError("Full name is required.");
      return;
    }
    setLoading(true);
    setError("");

    const payload: Record<string, unknown> = {
      fullName: form.fullName,
      email: form.email || undefined,
      phone: form.phone || undefined,
      currentCompany: form.currentCompany || undefined,
      source: form.source || undefined,
      location: form.location || undefined,
      experienceYears: form.experienceYears ? Number(form.experienceYears) : undefined,
      summary: form.summary || undefined,
      jobId: form.jobId || undefined,
    };

    const formData = new FormData();
    formData.append("request", new Blob([JSON.stringify(payload)], { type: "application/json" }));
    documents.forEach((file) => formData.append("documents", file));

    try {
      await candidateService.createCandidate(formData);
      resetForm();
      onOpenChange(false);
      onSuccess();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? "Failed to add candidate. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!loading) { resetForm(); onOpenChange(v); } }}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Add Candidate</DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 py-2">
          {/* Row 1 */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="fullName">Full Name <span className="text-destructive">*</span></Label>
              <Input id="fullName" name="fullName" value={form.fullName} onChange={handleChange} placeholder="Nguyen Van A" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="email">Email</Label>
              <Input id="email" name="email" type="email" value={form.email} onChange={handleChange} placeholder="email@example.com" />
            </div>
          </div>

          {/* Row 2 */}
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" name="phone" value={form.phone} onChange={handleChange} placeholder="0901234567" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="currentCompany">Current Company</Label>
              <Input id="currentCompany" name="currentCompany" value={form.currentCompany} onChange={handleChange} placeholder="FPT Software" />
            </div>
          </div>

          {/* Row 3 */}
          <div className="grid grid-cols-3 gap-4">
            <div className="space-y-1">
              <Label htmlFor="location">Location</Label>
              <Input id="location" name="location" value={form.location} onChange={handleChange} placeholder="Ha Noi" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="experienceYears">Experience (years)</Label>
              <Input id="experienceYears" name="experienceYears" type="number" min="0" max="50" value={form.experienceYears} onChange={handleChange} placeholder="3" />
            </div>
            <div className="space-y-1">
              <Label htmlFor="source">Source</Label>
              <select id="source" name="source" value={form.source} onChange={handleChange} className="h-9 w-full rounded-md border border-input bg-background px-3 text-sm">
                <option value="">— Select —</option>
                {SOURCES.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>

          {/* Summary */}
          <div className="space-y-1">
            <Label htmlFor="summary">Summary</Label>
            <textarea
              id="summary"
              name="summary"
              value={form.summary}
              onChange={handleChange}
              rows={3}
              placeholder="Brief profile overview..."
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring resize-none"
            />
          </div>

          {/* Job picker */}
          <div className="space-y-1">
            <Label htmlFor="jobId">Applying For (optional)</Label>
            <select id="jobId" name="jobId" value={form.jobId} onChange={handleChange} className="h-9 w-full rounded-md border border-input bg-background px-3 text-sm">
              <option value="">— None (save without job) —</option>
              {jobs.map((job) => (
                <option key={job.jobId} value={job.jobId}>{job.title} ({job.status})</option>
              ))}
            </select>
          </div>

          {/* File upload */}
          <div className="space-y-2">
            <Label>Documents / CV (optional)</Label>
            <label className="flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-input bg-muted/30 px-4 py-3 text-sm text-muted-foreground hover:bg-muted/50 transition-colors">
              <Upload className="h-4 w-4" />
              Click to attach files (PDF, DOC…)
              <input type="file" multiple className="hidden" onChange={handleFiles} accept=".pdf,.doc,.docx" />
            </label>
            {documents.length > 0 && (
              <ul className="space-y-1">
                {documents.map((f, i) => (
                  <li key={i} className="flex items-center justify-between rounded-md bg-muted/40 px-3 py-1.5 text-sm">
                    <span className="truncate">{f.name}</span>
                    <button type="button" onClick={() => removeFile(i)} className="ml-2 text-muted-foreground hover:text-destructive">
                      <X className="h-4 w-4" />
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          {error && <p className="text-sm text-destructive">{error}</p>}

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" onClick={() => { resetForm(); onOpenChange(false); }} disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" disabled={loading}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              Add Candidate
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
