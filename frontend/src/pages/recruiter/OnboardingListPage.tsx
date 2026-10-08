import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ClipboardCheck, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { onboardingService } from "@/services/onboardingService";
import type { OnboardingChecklist } from "@/types/onboarding";

const STATUS_STYLES: Record<string, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-700 ring-1 ring-gray-300",
  IN_PROGRESS: "bg-blue-50 text-blue-700 ring-1 ring-blue-700/10",
  COMPLETED: "bg-green-50 text-green-700 ring-1 ring-green-700/10",
};

export default function OnboardingListPage() {
  const [checklists, setChecklists] = useState<OnboardingChecklist[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadChecklists = async () => {
      try {
        const data = await onboardingService.getAllChecklists();
        setChecklists(data);
      } catch {
        setError("Failed to load onboarding checklists.");
      } finally {
        setLoading(false);
      }
    };
    void loadChecklists();
  }, []);

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center text-muted-foreground">
        Loading...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-semibold tracking-tight">Onboarding</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Manage onboarding checklists for new hires
        </p>
      </div>

      {error && (
        <p className="text-sm text-destructive">{error}</p>
      )}

      {checklists.length === 0 ? (
        <div className="rounded-lg border border-border bg-card p-8 text-center">
          <ClipboardCheck className="mx-auto mb-4 h-12 w-12 text-muted-foreground" />
          <p className="text-muted-foreground">No onboarding checklists yet.</p>
        </div>
      ) : (
        <div className="rounded-lg border border-border bg-card">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b bg-muted/50 text-left">
                  <th className="px-4 py-3 text-sm font-medium">Title</th>
                  <th className="px-4 py-3 text-sm font-medium">Candidate</th>
                  <th className="px-4 py-3 text-sm font-medium">Job</th>
                  <th className="px-4 py-3 text-sm font-medium">Progress</th>
                  <th className="px-4 py-3 text-sm font-medium">Status</th>
                  <th className="px-4 py-3 text-sm font-medium"></th>
                </tr>
              </thead>
              <tbody>
                {checklists.map((checklist) => (
                  <tr key={checklist.id} className="border-b last:border-0">
                    <td className="px-4 py-3">
                      <span className="font-medium">{checklist.title}</span>
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {checklist.candidateName || "—"}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {checklist.jobTitle || "—"}
                    </td>
                    <td className="px-4 py-3 text-sm">
                      {checklist.completedTasks}/{checklist.totalTasks} tasks
                    </td>
                    <td className="px-4 py-3">
                      <Badge variant="outline" className={STATUS_STYLES[checklist.status]}>
                        {checklist.status.replace(/_/g, " ")}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <Button variant="ghost" size="sm" asChild>
                        <Link to={`/onboarding/${checklist.id}`}>
                          View <ChevronRight className="ml-1 h-4 w-4" />
                        </Link>
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}