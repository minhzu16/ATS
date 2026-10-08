export type InterviewType = "SCREENING" | "TECHNICAL" | "CULTURE_FIT" | "HR" | "MANAGER";
export type InterviewStatus = "SCHEDULED" | "COMPLETED" | "CANCELLED" | "NO_SHOW";

export interface InterviewResponse {
  id: string;
  scheduledAt: string;
  startedAt: string | null;
  endedAt: string | null;
  location: string;
  meetingLink: string | null;
  type: InterviewType;
  status: InterviewStatus;
  applicationId: string;
  templateId: string;
  candidateName?: string;
  jobTitle?: string;
  participantCount: number;
  scoreCount: number;
}

export interface ParticipantResponse {
  userId: string;
  fullName: string;
  avatarUrl?: string;
  role: string | "ORGANIZER" | "INTERVIEWER";
}

export interface InterviewDetailResponse extends InterviewResponse {
  candidateId: string;
  candidateEmail?: string;
  candidatePhone?: string;
  candidateResumeUrl?: string;
  jobId: string;
  jobDepartment?: string;
  participants: ParticipantResponse[];
}

export interface ScorecardCriterionResponse {
  id: string;
  name: string;
  description?: string;
  weight: number;
}

export interface ScorecardTemplateResponse {
  id: string;
  name: string;
  description?: string;
  criteria: ScorecardCriterionResponse[];
}

export interface CriterionScoreRequest {
  criterionId: string;
  score: number;
}

export interface SubmitFeedbackRequest {
  interviewId: string;
  interviewerId?: string;
  scores: CriterionScoreRequest[];
  overallScore?: number;
  feedback?: string;
}

export interface InterviewStageEvaluationResponse {
  interviewId: string;
  type: string;
  status: string;
  score: number;
  interviewerName: string;
  scheduledAt: string;
  feedbackSnippet: string;
}

export interface ApplicationEvaluationResponse {
  applicationId: string;
  candidateName: string;
  jobTitle: string;
  overallScore: number;
  recommendation: string;
  interviewsCompleted: number;
  totalInterviews: number;
  stages: InterviewStageEvaluationResponse[];
}

export interface CandidateEvaluationResponse {
  interviewId: string;
  candidateName: string;
  overallScore: number;
  recommendation: string;
  interviewerFeedbacks: any[];
}
