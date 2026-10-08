import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { AxiosError } from "axios";
import { AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { getJobForEdit, updateJob } from "@/services/jobs/commandApi";

export default function EditJobPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [headcount, setHeadcount] = useState<number>(1);
    const [departmentName, setDepartmentName] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [dirty, setDirty] = useState(false);

    const load = useCallback(async () => {
        if (!id) {
            setLoading(false);
            setError("Invalid job ID.");
            return;
        }
        setLoading(true);
        setError(null);
        try {
            const job = await getJobForEdit(id);
            setTitle(job.title);
            setDescription(job.description ?? "");
            setHeadcount(job.headcount ?? 1);
            setDepartmentName(job.departmentName);
            setDirty(false);
        } catch (e) {
            const ax = e as AxiosError<{ message?: string }>;
            if (ax.response?.status === 403) {
                setError("You do not have permission to edit this job.");
            } else if (ax.response?.status === 404) {
                setError("Job not found.");
            } else {
                setError("Failed to load job for editing.");
            }
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        void load();
    }, [load]);

    const markDirty = () => setDirty(true);

    const handleSubmit = async () => {
        if (!id) return;
        setError(null);
        setSubmitting(true);
        try {
            await updateJob(id, {
                title: title.trim(),
                description: description.trim(),
                headcount,
            });
            navigate(`/jobs/${id}`, { replace: true });
        } catch (e) {
            const ax = e as AxiosError<{ message?: string; errors?: string[] }>;
            const msg =
                ax.response?.data?.errors?.join("; ") ||
                (typeof ax.response?.data === "object" && ax.response?.data && "message" in ax.response.data
                    ? String((ax.response.data as { message?: string }).message)
                    : null) ||
                "Could not update job.";
            setError(msg);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) {
        return (
            <div className="p-6">
                <p className="text-sm text-muted-foreground">Loading job…</p>
            </div>
        );
    }

    if (error && !title) {
        return (
            <div className="space-y-4 p-6">
                <p className="rounded-lg border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">{error}</p>
                <Button variant="outline" onClick={() => navigate("/jobs")}>
                    Back to Jobs
                </Button>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-5xl space-y-6 p-6">
            {dirty && (
                <div className="rounded-md border border-border bg-muted/50 p-4">
                    <div className="flex gap-3">
                        <AlertCircle className="h-5 w-5 shrink-0 text-muted-foreground" aria-hidden />
                        <div>
                            <h3 className="text-sm font-medium text-foreground">Unsaved changes</h3>
                            <p className="mt-1 text-sm text-muted-foreground">
                                Save your changes before leaving this page.
                            </p>
                        </div>
                    </div>
                </div>
            )}

            <div>
                <h1 className="text-xl font-semibold text-foreground">Edit Job</h1>
                <p className="mt-1 text-sm text-muted-foreground">Update title, description, and headcount.</p>
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
                        <p className="mt-1 text-sm text-muted-foreground">Update basic information about the role.</p>
                    </div>
                    <div className="grid grid-cols-1 gap-6 sm:grid-cols-6">
                        <div className="sm:col-span-4">
                            <Label htmlFor="ej-title">Job Title</Label>
                            <Input
                                id="ej-title"
                                className="mt-1"
                                value={title}
                                onChange={(e) => {
                                    setTitle(e.target.value);
                                    markDirty();
                                }}
                                required
                            />
                        </div>
                        <div className="sm:col-span-2">
                            <Label htmlFor="ej-dept">Department</Label>
                            <Input
                                id="ej-dept"
                                className="mt-1"
                                value={departmentName ?? ""}
                                readOnly
                                disabled
                                aria-readonly="true"
                            />
                            <p className="mt-1 text-xs text-muted-foreground">Cannot be changed here.</p>
                        </div>
                        <div className="sm:col-span-2">
                            <Label htmlFor="ej-headcount">Headcount</Label>
                            <Input
                                id="ej-headcount"
                                type="number"
                                min={1}
                                className="mt-1"
                                value={headcount}
                                onChange={(e) => {
                                    setHeadcount(Math.max(1, Number(e.target.value) || 1));
                                    markDirty();
                                }}
                                required
                            />
                        </div>
                        <div className="sm:col-span-6">
                            <Label htmlFor="ej-desc">Description</Label>
                            <Textarea
                                id="ej-desc"
                                rows={5}
                                className="mt-1"
                                value={description}
                                onChange={(e) => {
                                    setDescription(e.target.value);
                                    markDirty();
                                }}
                                required
                            />
                        </div>
                    </div>
                </div>

                {error && (
                    <p className="mt-6 rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive" role="alert">
                        {error}
                    </p>
                )}

                <div className="mt-8 flex flex-wrap justify-end gap-3 border-t border-border pt-5">
                    <Button type="button" variant="outline" onClick={() => navigate(`/jobs/${id ?? ""}`)}>
                        Cancel
                    </Button>
                    <Button type="submit" disabled={submitting || !title.trim() || !description.trim()}>
                        {submitting ? "Saving…" : "Update Job"}
                    </Button>
                </div>
            </form>
        </div>
    );
}
