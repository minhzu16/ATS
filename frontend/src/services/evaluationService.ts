import api from "@/lib/api";
import type { CandidateEvaluation, CandidateRanking } from "@/types/evaluation";

export const evaluationService = {
  async getEvaluationSummary(applicationId: string): Promise<CandidateEvaluation> {
    const { data } = await api.get<CandidateEvaluation>(
      `/applications/${applicationId}/evaluation`,
    );
    return data;
  },

  async getCandidateRanking(jobId: string): Promise<CandidateRanking[]> {
    const { data } = await api.get<CandidateRanking[]>(`/jobs/${jobId}/ranking`);
    return data;
  },
};
