export type OnboardingStatus = "NOT_STARTED" | "IN_PROGRESS" | "COMPLETED";

export interface OnboardingTask {
  id: string;
  title: string;
  description: string | null;
  completed: boolean;
  sortOrder: number;
  dueDate: string | null;
  assignedToUserId: string | null;
  assignedToName: string | null;
}

export interface OnboardingChecklist {
  id: string;
  applicationId: string;
  candidateName: string | null;
  jobTitle: string | null;
  title: string;
  status: OnboardingStatus;
  totalTasks: number;
  completedTasks: number;
  progressPercent: number;
  createdAt: string;
  tasks: OnboardingTask[];
}

export interface TaskEntry {
  title: string;
  description: string;
  sortOrder: number;
  dueDate: string | null;
  assignedToUserId: string | null;
}

export interface CreateOnboardingRequest {
  applicationId: string;
  title: string;
  tasks: TaskEntry[];
}
