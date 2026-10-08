import api from "@/lib/api";
import type { 
  InterviewResponse, 
  InterviewDetailResponse, 
  ScorecardTemplateResponse, 
  SubmitFeedbackRequest,
  ApplicationEvaluationResponse
} from "@/types/interview";

export const interviewService = {
  async getAllInterviews(): Promise<InterviewResponse[]> {
    const { data } = await api.get<InterviewResponse[]>("/interviews");
    return data;
  },
  async getInterviewById(id: string): Promise<InterviewDetailResponse> {
    const { data } = await api.get<InterviewDetailResponse>(`/interviews/${id}`);
    return data;
  },
  async getTemplate(interviewId: string): Promise<ScorecardTemplateResponse> {
    const response = await api.get(`/interviews/${interviewId}/template`);
    return response.data;
  },

  async getAllTemplates(): Promise<ScorecardTemplateResponse[]> {
    const response = await api.get('/templates');
    return response.data;
  },

  async submitFeedback(data: SubmitFeedbackRequest): Promise<void> {
    await api.post(`/interviews/feedback`, data);
  },

  async getApplicationEvaluation(applicationId: string): Promise<ApplicationEvaluationResponse> {
    const response = await api.get<ApplicationEvaluationResponse>(`/interviews/applications/${applicationId}/evaluation`);
    return response.data;
  }
};
