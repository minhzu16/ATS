import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  CheckSquare,
  Square,
  Plus,
  Trash2,
  ClipboardList,
  Calendar,
  User as UserIcon,
} from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { onboardingService } from "@/services/onboardingService";
import { userService } from "@/services/userService";
import type {
  OnboardingChecklist,
  OnboardingStatus,
  CreateOnboardingRequest,
  TaskEntry,
} from "@/types/onboarding";
import type { UserRecord } from "@/types/user";

const STATUS_STYLES: Record<OnboardingStatus, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-700 ring-1 ring-gray-300",
  IN_PROGRESS: "bg-blue-50 text-blue-700 ring-1 ring-blue-700/10",
  COMPLETED: "bg-green-50 text-green-700 ring-1 ring-green-700/10",
};

export default function OnboardingPage() {
  const { id, applicationId } = useParams<{ id: string; applicationId: string }>();
  const [checklist, setChecklist] = useState<OnboardingChecklist | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [createOpen, setCreateOpen] = useState(false);

  const loadChecklist = async () => {
    setLoading(true);
    setError("");
    try {
      if (id) {
        const data = await onboardingService.getById(id);
        setChecklist(data);
      } else if (applicationId) {
        const data = await onboardingService.getByApplicationId(applicationId);
        setChecklist(data);
      } else {
        setError("No checklist ID or application ID provided.");
      }
    } catch {
      setChecklist(null);
      if (applicationId) {
        setCreateOpen(true);
      } else {
        setError("Onboarding checklist not found.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadChecklist();
  }, [id, applicationId]);

  const handleToggle = async (taskId: string) => {
    if (!checklist) return;
    try {
      const updated = await onboardingService.toggleTask(checklist.id, taskId);
      setChecklist(updated);
    } catch {
      alert("Failed to toggle task.");
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading onboarding checklist...
      </div>
    );
  }

  if (createOpen && !checklist) {
    return (
      <CreateChecklistForm
        applicationId={applicationId ?? ""}
        onCreated={(c) => {
          setChecklist(c);
          setCreateOpen(false);
        }}
      />
    );
  }

  if (error || !checklist) {
    return <p className="py-8 text-center text-sm text-destructive">{error || "Not found."}</p>;
  }

  const progressPercent = Math.round(checklist.progressPercent);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-semibold tracking-tight">{checklist.title}</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            {checklist.candidateName && `${checklist.candidateName} · `}
            {checklist.jobTitle ?? "Onboarding Checklist"}
          </p>
        </div>
        <Badge variant="outline" className={STATUS_STYLES[checklist.status]}>
          {checklist.status.replace(/_/g, " ")}
        </Badge>
      </div>

      {/* Progress Bar */}
      <section className="rounded-lg border border-border bg-card p-6 shadow-sm">
        <div className="mb-2 flex items-center justify-between text-sm">
          <span className="font-medium">Progress</span>
          <span className="text-muted-foreground">
            {checklist.completedTasks}/{checklist.totalTasks} tasks ({progressPercent}%)
          </span>
        </div>
        <div className="h-3 w-full overflow-hidden rounded-full bg-muted">
          <div
            className="h-full rounded-full bg-primary transition-all duration-500"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </section>

      {/* Task List */}
      <section className="rounded-lg border border-border bg-card p-6 shadow-sm">
        <h3 className="mb-4 text-lg font-semibold text-card-foreground">
          <ClipboardList className="mb-0.5 mr-2 inline-block h-5 w-5" />
          Tasks
        </h3>
        {checklist.tasks.length === 0 ? (
          <p className="text-sm text-muted-foreground">No tasks in this checklist.</p>
        ) : (
          <div className="space-y-2">
            {checklist.tasks.map((task) => (
              <div
                key={task.id}
                className={`flex items-start gap-3 rounded-lg border p-4 transition-colors ${
                  task.completed
                    ? "border-green-200 bg-green-50/50"
                    : "border-border bg-background hover:bg-muted/20"
                }`}
              >
                <button
                  onClick={() => handleToggle(task.id)}
                  className="mt-0.5 shrink-0"
                >
                  {task.completed ? (
                    <CheckSquare className="h-5 w-5 text-green-600" />
                  ) : (
                    <Square className="h-5 w-5 text-muted-foreground" />
                  )}
                </button>
                <div className="flex-1">
                  <p
                    className={`font-medium ${
                      task.completed ? "text-muted-foreground line-through" : ""
                    }`}
                  >
                    {task.title}
                  </p>
                  {task.description && (
                    <p className="mt-1 text-sm text-muted-foreground">{task.description}</p>
                  )}
                  <div className="mt-2 flex flex-wrap gap-3 text-xs text-muted-foreground">
                    {task.dueDate && (
                      <span className="flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        Due: {task.dueDate}
                      </span>
                    )}
                    {task.assignedToName && (
                      <span className="flex items-center gap-1">
                        <UserIcon className="h-3 w-3" />
                        {task.assignedToName}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

/* ──────────────────────── Create Checklist Form ──────────────────────── */

function CreateChecklistForm({
  applicationId,
  onCreated,
}: {
  applicationId: string;
  onCreated: (c: OnboardingChecklist) => void;
}) {
  const [title, setTitle] = useState("Onboarding Checklist");
  const [tasks, setTasks] = useState<TaskEntry[]>([
    { title: "", description: "", sortOrder: 0, dueDate: null, assignedToUserId: null },
  ]);
  const [saving, setSaving] = useState(false);
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(true);

  useEffect(() => {
    const loadUsers = async () => {
      try {
        const data = await userService.getUsers(0, 100);
        setUsers((data.content || []).filter((u) => u.active));
      } catch {
        // Ignore error
      } finally {
        setLoadingUsers(false);
      }
    };
    void loadUsers();
  }, []);

  const addTask = () => {
    setTasks([
      ...tasks,
      { title: "", description: "", sortOrder: tasks.length, dueDate: null, assignedToUserId: null },
    ]);
  };

  const removeTask = (index: number) => {
    setTasks(tasks.filter((_, i) => i !== index));
  };

  const updateTask = (index: number, field: keyof TaskEntry, value: string | number | null) => {
    const updated = [...tasks];
    updated[index] = { ...updated[index], [field]: value };
    setTasks(updated);
  };

  const handleCreate = async () => {
    if (!title.trim()) {
      alert("Title is required.");
      return;
    }
    if (tasks.some((t) => !t.title.trim())) {
      alert("All tasks must have a title.");
      return;
    }
    setSaving(true);
    try {
      const request: CreateOnboardingRequest = {
        applicationId,
        title: title.trim(),
        tasks: tasks.map((t, i) => ({ ...t, sortOrder: i })),
      };
      const result = await onboardingService.create(request);
      onCreated(result);
    } catch {
      alert("Failed to create checklist.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Create Onboarding Checklist</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Set up tasks for onboarding the new hire
        </p>
      </div>

      <section className="mx-auto max-w-2xl rounded-lg border border-border bg-card p-6 shadow-sm">
        <div className="space-y-4">
          <div>
            <label className="mb-1 block text-sm font-medium">Checklist Title</label>
            <Input value={title} onChange={(e) => setTitle(e.target.value)} />
          </div>

          <div>
            <div className="mb-2 flex items-center justify-between">
              <label className="text-sm font-medium">Tasks</label>
              <Button size="sm" variant="outline" onClick={addTask}>
                <Plus className="h-3.5 w-3.5" /> Add Task
              </Button>
            </div>
            <div className="space-y-3">
              {tasks.map((task, i) => (
                <div key={i} className="rounded-lg border border-border p-4">
                  <div className="flex items-center gap-2">
                    <Input
                      value={task.title}
                      onChange={(e) => updateTask(i, "title", e.target.value)}
                      placeholder="Task title"
                      className="flex-1"
                    />
                    {tasks.length > 1 && (
                      <Button
                        size="sm"
                        variant="outline"
                        className="text-destructive"
                        onClick={() => removeTask(i)}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    )}
                  </div>
                  <Input
                    value={task.description}
                    onChange={(e) => updateTask(i, "description", e.target.value)}
                    placeholder="Description (optional)"
                    className="mt-2"
                  />
                  <div className="mt-2 flex gap-2">
                    <Input
                      type="date"
                      value={task.dueDate ?? ""}
                      onChange={(e) => updateTask(i, "dueDate", e.target.value || null)}
                      className="flex-1"
                      placeholder="Due date"
                    />
                    <select
                      value={task.assignedToUserId ?? ""}
                      onChange={(e) => updateTask(i, "assignedToUserId", e.target.value || null)}
                      disabled={loadingUsers}
                      className="flex-1 rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                    >
                      <option value="">Assign to user</option>
                      {users.map((user) => (
                        <option key={user.id} value={user.id}>
                          {user.fullName}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-2">
          <Button onClick={handleCreate} disabled={saving}>
            {saving ? "Creating..." : "Create Checklist"}
          </Button>
        </div>
      </section>
    </div>
  );
}
