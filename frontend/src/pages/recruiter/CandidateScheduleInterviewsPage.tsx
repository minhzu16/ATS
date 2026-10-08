import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Plus, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { candidateService } from "@/services/candidateService";
import type { CandidateDetailItem, InterviewType, InterviewerOptionItem, ScheduleInterviewSlotInput } from "@/types/candidate";

type InterviewSlotForm = {
  scheduledAtLocal: string;
  type: InterviewType;
  location: string;
  meetingLink: string;
  interviewerIds: string[];
};

function createEmptySlot(): InterviewSlotForm {
  return {
    scheduledAtLocal: "",
    type: "ONLINE",
    location: "",
    meetingLink: "",
    interviewerIds: [],
  };
}

function toPayloadSlot(slot: InterviewSlotForm): ScheduleInterviewSlotInput {
  return {
    scheduledAt: slot.scheduledAtLocal,
    type: slot.type,
    location: slot.type === "OFFLINE" ? slot.location || null : null,
    meetingLink: slot.type === "ONLINE" ? slot.meetingLink || null : null,
    interviewerIds: slot.interviewerIds,
  };
}

export default function CandidateScheduleInterviewsPage() {
  const { candidateId } = useParams<{ candidateId: string }>();
  const navigate = useNavigate();

  const [candidate, setCandidate] = useState<CandidateDetailItem | null>(null);
  const [interviewers, setInterviewers] = useState<InterviewerOptionItem[]>([]);
  const [slots, setSlots] = useState<InterviewSlotForm[]>([createEmptySlot()]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!candidateId) {
      setError("Invalid candidate id.");
      setLoading(false);
      return;
    }

    const loadData = async () => {
      setLoading(true);
      setError("");
      try {
        const [candidateDetail, interviewerOptions] = await Promise.all([
          candidateService.getCandidateDetail(candidateId),
          candidateService.getInterviewerOptions(),
        ]);
        setCandidate(candidateDetail);
        setInterviewers(interviewerOptions);
      } catch {
        setError("Failed to load scheduling data.");
      } finally {
        setLoading(false);
      }
    };

    void loadData();
  }, [candidateId]);

  const canSubmit = useMemo(() => !loading && !submitting && !!candidateId && interviewers.length > 0, [loading, submitting, candidateId, interviewers.length]);

  const setSlot = (index: number, updater: (prev: InterviewSlotForm) => InterviewSlotForm) => {
    setSlots((prev) => prev.map((slot, idx) => (idx === index ? updater(slot) : slot)));
  };

  const toggleInterviewer = (index: number, interviewerId: string) => {
    setSlot(index, (prev) => {
      const exists = prev.interviewerIds.includes(interviewerId);
      return {
        ...prev,
        interviewerIds: exists
          ? prev.interviewerIds.filter((id) => id !== interviewerId)
          : [...prev.interviewerIds, interviewerId],
      };
    });
  };

  const validateSlots = (): string | null => {
    for (let index = 0; index < slots.length; index += 1) {
      const slot = slots[index];
      const round = index + 1;

      if (!slot.scheduledAtLocal) {
        return `Round ${round}: scheduled time is required.`;
      }

      if (slot.type === "ONLINE" && !slot.meetingLink.trim()) {
        return `Round ${round}: meeting link is required for ONLINE interview.`;
      }

      if (slot.type === "OFFLINE" && !slot.location.trim()) {
        return `Round ${round}: location is required for OFFLINE interview.`;
      }

      if (slot.interviewerIds.length === 0) {
        return `Round ${round}: choose at least one interviewer.`;
      }
    }

    return null;
  };

  const handleSubmit = async () => {
    if (!candidateId || submitting) return;

    const validationError = validateSlots();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);
    setError("");
    try {
      await candidateService.scheduleCandidateInterviews(candidateId, {
        interviews: slots.map(toPayloadSlot),
      });
      navigate(`/candidates/${candidateId}`);
    } catch {
      setError("Failed to schedule interviews.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p className="text-sm text-muted-foreground">Loading schedule form...</p>;
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center gap-2">
        <Button variant="ghost" size="sm" onClick={() => navigate(`/candidates/${candidateId}`)}>
          <ArrowLeft className="h-4 w-4" />
          Back to Candidate
        </Button>
      </div>

      <section className="rounded-lg border border-border bg-card p-5">
        <h1 className="text-2xl font-semibold tracking-tight">Schedule Interviews</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Candidate: <span className="font-medium text-foreground">{candidate?.fullName ?? "-"}</span>
          {" · "}
          Job: <span className="font-medium text-foreground">{candidate?.jobTitle ?? "-"}</span>
        </p>
      </section>

      {error && <p className="text-sm text-destructive">{error}</p>}

      {slots.map((slot, index) => (
        <section key={`slot-${index}`} className="rounded-lg border border-border bg-card p-5 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">Interview Round {index + 1}</h2>
            {slots.length > 1 && (
              <Button
                variant="outline"
                size="sm"
                onClick={() => setSlots((prev) => prev.filter((_, idx) => idx !== index))}
                disabled={submitting}
              >
                <Trash2 className="h-4 w-4" />
                Remove
              </Button>
            )}
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <Label>Scheduled time</Label>
              <Input
                type="datetime-local"
                value={slot.scheduledAtLocal}
                onChange={(event) => setSlot(index, (prev) => ({ ...prev, scheduledAtLocal: event.target.value }))}
              />
            </div>

            <div className="space-y-2">
              <Label>Type</Label>
              <Select
                value={slot.type}
                onValueChange={(value: InterviewType) =>
                  setSlot(index, (prev) => ({
                    ...prev,
                    type: value,
                    meetingLink: value === "ONLINE" ? prev.meetingLink : "",
                    location: value === "OFFLINE" ? prev.location : "",
                  }))
                }
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select type" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ONLINE">ONLINE</SelectItem>
                  <SelectItem value="OFFLINE">OFFLINE</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {slot.type === "ONLINE" ? (
            <div className="space-y-2">
              <Label>Meeting link</Label>
              <Input
                placeholder="https://meet.google.com/..."
                value={slot.meetingLink}
                onChange={(event) => setSlot(index, (prev) => ({ ...prev, meetingLink: event.target.value }))}
              />
            </div>
          ) : (
            <div className="space-y-2">
              <Label>Location</Label>
              <Input
                placeholder="Office / Meeting room"
                value={slot.location}
                onChange={(event) => setSlot(index, (prev) => ({ ...prev, location: event.target.value }))}
              />
            </div>
          )}

          <div className="space-y-2">
            <Label>Interviewers</Label>
            <div className="grid gap-2 md:grid-cols-2">
              {interviewers.map((interviewer) => {
                const selected = slot.interviewerIds.includes(interviewer.id);
                return (
                  <button
                    type="button"
                    key={interviewer.id}
                    onClick={() => toggleInterviewer(index, interviewer.id)}
                    className={`rounded-md border px-3 py-2 text-left transition ${
                      selected
                        ? "border-primary bg-primary/5 text-foreground"
                        : "border-border bg-background text-muted-foreground hover:bg-muted/30"
                    }`}
                  >
                    <p className="text-sm font-medium">{interviewer.fullName}</p>
                    <p className="text-xs">{interviewer.email}</p>
                  </button>
                );
              })}
            </div>
          </div>
        </section>
      ))}

      <div className="flex flex-wrap items-center gap-2">
        <Button
          variant="outline"
          onClick={() => setSlots((prev) => [...prev, createEmptySlot()])}
          disabled={submitting}
        >
          <Plus className="h-4 w-4" />
          Add Round
        </Button>

        <Button onClick={() => void handleSubmit()} disabled={!canSubmit}>
          {submitting ? "Scheduling..." : "Confirm Schedule"}
        </Button>
      </div>
    </div>
  );
}
