import { useState, useRef } from "react";
import { FileText, Upload, X, Loader2, CheckCircle2, AlertCircle } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { candidateService } from "@/services/candidateService";
import type { BulkImportResult } from "@/types/candidate";

interface ImportCandidatesDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSuccess: () => void;
}

export default function ImportCandidatesDialog({ open, onOpenChange, onSuccess }: ImportCandidatesDialogProps) {
  const [csvFile, setCsvFile] = useState<File | null>(null);
  const [cvFiles, setCvFiles] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<BulkImportResult | null>(null);
  const [error, setError] = useState("");
  const csvRef = useRef<HTMLInputElement>(null);

  function reset() {
    setCsvFile(null);
    setCvFiles([]);
    setResult(null);
    setError("");
  }

  function handleCsvFile(e: React.ChangeEvent<HTMLInputElement>) {
    setCsvFile(e.target.files?.[0] ?? null);
  }

  function handleCvFiles(e: React.ChangeEvent<HTMLInputElement>) {
    if (e.target.files) {
      setCvFiles((prev) => [...prev, ...Array.from(e.target.files!)]);
    }
  }

  function removeCv(index: number) {
    setCvFiles((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!csvFile) {
      setError("Please select a CSV file.");
      return;
    }
    setLoading(true);
    setError("");
    setResult(null);

    const formData = new FormData();
    formData.append("csv", csvFile);
    cvFiles.forEach((f) => formData.append("cvFiles", f));

    try {
      const data = await candidateService.importCandidates(formData);
      setResult(data);
      if (data.successCount > 0) onSuccess();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      setError(msg ?? "Import failed. Please check your CSV file and try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { if (!loading) { reset(); onOpenChange(v); } }}>
      <DialogContent className="max-h-[90vh] max-w-xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Import Candidates from CSV</DialogTitle>
        </DialogHeader>

        {!result ? (
          <form onSubmit={handleSubmit} className="space-y-5 py-2">
            {/* CSV File */}
            <div className="space-y-2">
              <Label>CSV File <span className="text-destructive">*</span></Label>
              <label className="flex cursor-pointer items-center gap-3 rounded-md border border-dashed border-input bg-muted/30 px-4 py-4 text-sm text-muted-foreground hover:bg-muted/50 transition-colors">
                <FileText className="h-5 w-5 shrink-0" />
                <span className="truncate">{csvFile ? csvFile.name : "Click to select CSV file"}</span>
                <input ref={csvRef} type="file" accept=".csv" className="hidden" onChange={handleCsvFile} />
              </label>

              <div className="rounded-md bg-muted/30 px-4 py-3 text-xs text-muted-foreground space-y-1">
                <p className="font-medium text-foreground">Required CSV columns:</p>
                <code className="block text-xs">fullName, email, phone, currentCompany, source, location, experienceYears, summary, jobId, cvFileName</code>
                <p className="mt-1">• <code>jobId</code>: UUID of existing job (leave blank if none)</p>
                <p>• <code>cvFileName</code>: exact filename of PDF uploaded below</p>
              </div>
            </div>

            {/* PDF CV Files */}
            <div className="space-y-2">
              <Label>CV Files (PDF – optional)</Label>
              <label className="flex cursor-pointer items-center gap-2 rounded-md border border-dashed border-input bg-muted/30 px-4 py-3 text-sm text-muted-foreground hover:bg-muted/50 transition-colors">
                <Upload className="h-4 w-4" />
                Attach PDF files (select multiple)
                <input type="file" multiple accept=".pdf" className="hidden" onChange={handleCvFiles} />
              </label>
              {cvFiles.length > 0 && (
                <ul className="space-y-1 max-h-40 overflow-y-auto">
                  {cvFiles.map((f, i) => (
                    <li key={i} className="flex items-center justify-between rounded-md bg-muted/40 px-3 py-1.5 text-sm">
                      <span className="truncate">{f.name}</span>
                      <button type="button" onClick={() => removeCv(i)} className="ml-2 text-muted-foreground hover:text-destructive">
                        <X className="h-3.5 w-3.5" />
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>

            {error && <p className="text-sm text-destructive">{error}</p>}

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => { reset(); onOpenChange(false); }} disabled={loading}>
                Cancel
              </Button>
              <Button type="submit" disabled={loading || !csvFile}>
                {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                Import
              </Button>
            </DialogFooter>
          </form>
        ) : (
          /* Result screen */
          <div className="space-y-4 py-2">
            {/* Summary cards */}
            <div className="grid grid-cols-3 gap-3">
              <div className="rounded-lg border border-border bg-muted/30 p-3 text-center">
                <p className="text-2xl font-bold">{result.totalRows}</p>
                <p className="text-xs text-muted-foreground mt-0.5">Total rows</p>
              </div>
              <div className="rounded-lg border border-green-500/40 bg-green-500/10 p-3 text-center">
                <p className="text-2xl font-bold text-green-600">{result.successCount}</p>
                <p className="text-xs text-muted-foreground mt-0.5">Imported</p>
              </div>
              <div className={`rounded-lg border p-3 text-center ${result.failCount > 0 ? "border-destructive/40 bg-destructive/10" : "border-border bg-muted/30"}`}>
                <p className={`text-2xl font-bold ${result.failCount > 0 ? "text-destructive" : ""}`}>{result.failCount}</p>
                <p className="text-xs text-muted-foreground mt-0.5">Failed</p>
              </div>
            </div>

            {/* Overall status */}
            {result.failCount === 0 && result.errors.length === 0 ? (
              <div className="flex items-center gap-2 rounded-md bg-green-500/10 px-4 py-3 text-sm text-green-700">
                <CheckCircle2 className="h-4 w-4 shrink-0" />
                All candidates imported successfully!
              </div>
            ) : (
              <div className="flex items-start gap-2 rounded-md bg-amber-500/10 px-4 py-3 text-sm text-amber-700">
                <AlertCircle className="h-4 w-4 mt-0.5 shrink-0" />
                <span>Some rows had issues. Candidates with errors were skipped or created without CV.</span>
              </div>
            )}

            {/* Error list */}
            {result.errors.length > 0 && (
              <div className="space-y-1">
                <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">Details</p>
                <ul className="max-h-48 overflow-y-auto space-y-1">
                  {result.errors.map((err, i) => (
                    <li key={i} className={`rounded px-3 py-1.5 text-xs ${err.includes("[WARNING]") ? "bg-amber-500/10 text-amber-700" : "bg-destructive/10 text-destructive"}`}>
                      {err}
                    </li>
                  ))}
                </ul>
              </div>
            )}

            <DialogFooter>
              <Button onClick={() => { reset(); onOpenChange(false); }}>
                Done
              </Button>
            </DialogFooter>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
