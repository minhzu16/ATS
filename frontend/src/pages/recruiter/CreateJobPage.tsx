import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { AxiosError } from "axios";
import { AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { createJob } from "@/services/jobs/commandApi";
import { useAuth } from "@/hooks/useAuth";

export default function CreateJobPage() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [headcount, setHeadcount] = useState<number>(1);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const departmentLabel = user?.department?.trim() || "—";

    const handleSubmit = async () => {
        setError(null);
        setSubmitting(true);
        try {
            const created = await createJob({
                title: title.trim(),
                description: description.trim(),
                headcount,
            });
            navigate(`/jobs/${created.jobId}`, { replace: true });
        } catch (e) {
            const ax = e as AxiosError<{ message?: string; errors?: string[] }>;
            const msg =
                ax.response?.data?.errors?.join("; ") ||
                (typeof ax.response?.data === "object" && ax.response?.data && "message" in ax.response.data
                    ? String((ax.response.data as { message?: string }).message)
                    : null) ||
                "Could not create job. Check the form and try again.";
            setError(msg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="mx-auto max-w-5xl space-y-6 p-6">
            <div>
                <h1 className="text-xl font-semibold text-foreground">Create New Job</h1>
                <p className="mt-1 text-sm text-muted-foreground">Submit for approval when you are ready.</p>
            </div>

            <form
                className="rounded-lg border border-border bg-card p-6 shadow-sm sm:p-8"
                onSubmit={(ev) => {
                    ev.preventDefault();
                    void handleSubmit();
                }}
            >
                <div className="space-y-6">
                    <div>
                        <h2 className="text-lg font-medium text-foreground">Job Details</h2>
                        <p className="mt-1 text-sm text-muted-foreground">Basic information about the role.</p>
                    </div>
                    <div className="grid grid-cols-1 gap-6 sm:grid-cols-6">
                        <div className="sm:col-span-4">
                            <Label htmlFor="cj-title">Job Title</Label>
                            <Input
                                id="cj-title"
                                className="mt-1"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                placeholder="e.g. Senior Frontend Developer"
                                required
                            />
                        </div>
                        <div className="sm:col-span-2">
                            <Label htmlFor="cj-dept">Department</Label>
                            <Input
                                id="cj-dept"
                                className="mt-1"
                                value={departmentLabel}
                                readOnly
                                disabled
                                aria-readonly="true"
                            />
                            <p className="mt-1 text-xs text-muted-foreground">Taken from your profile (cannot be changed here).</p>
                        </div>
                        <div className="sm:col-span-2">
                            <Label htmlFor="cj-headcount">Headcount</Label>
                            <Input
                                id="cj-headcount"
                                type="number"
                                min={1}
                                className="mt-1"
                                value={headcount}
                                onChange={(e) => setHeadcount(Math.max(1, Number(e.target.value) || 1))}
                                required
                            />
                        </div>
                        <div className="sm:col-span-6">
                            <Label htmlFor="cj-desc">Description</Label>
                            <Textarea
                                id="cj-desc"
                                rows={5}
                                className="mt-1"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                required
                            />
                            <p className="mt-2 text-sm text-muted-foreground">Write a few sentences about the role.</p>
                        </div>
                    </div>

                    <div
                        className="flex items-start gap-3 rounded-lg border border-border bg-muted/30 p-4 text-sm text-foreground"
                        role="status"
                    >
                        <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" aria-hidden />
                        <div>
                            <span className="font-medium">Note:</span> This posting will be submitted for HR manager approval before it
                            goes live.
                        </div>
                    </div>
                </div>

                {error && (
                    <p className="mt-6 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
                        {error}
                    </p>
                )}

                <div className="mt-8 flex flex-wrap justify-end gap-3 border-t border-border pt-5">
                    <Button type="button" variant="outline" onClick={() => navigate("/jobs")}>
                        Cancel
                    </Button>
                    <Button type="submit" disabled={submitting || !title.trim() || !description.trim()}>
                        {submitting ? "Submitting…" : "Submit for Approval"}
                    </Button>
                </div>
            </form>
        </div>
    );
}
