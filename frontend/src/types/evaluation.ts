export interface InterviewScorecardSummary {
  scorecardId: string;
  interviewerName: string | null;
  overallScore: number | null;
}

export interface InterviewEvaluation {
  interviewId: string;
  scheduledAt: string;
  type: string | null;
  status: string | null;
  averageScore: number;
  interviewerCount: number;
  scorecards: InterviewScorecardSummary[];
}

export interface CandidateEvaluation {
  applicationId: string;
  candidateId: string | null;
  candidateName: string;
  jobTitle: string;
  overallScore: number;
  interviews: InterviewEvaluation[];
}

export interface CandidateRanking {
  rank: number;
  applicationId: string;
  candidateId: string;
  candidateName: string;
  overallScore: number;
  experienceYears: number | null;
  appliedAt: string;
  stage: string;
}

