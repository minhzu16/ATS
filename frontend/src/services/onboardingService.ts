import api from "@/lib/api";
import type { OnboardingChecklist, CreateOnboardingRequest } from "@/types/onboarding";

export const onboardingService = {
  async create(request: CreateOnboardingRequest): Promise<OnboardingChecklist> {
    const { data } = await api.post<OnboardingChecklist>("/onboarding", request);
    return data;
  },

  async getById(id: string): Promise<OnboardingChecklist> {
    const { data } = await api.get<OnboardingChecklist>(`/onboarding/${id}`);
    return data;
  },

  async getByApplicationId(applicationId: string): Promise<OnboardingChecklist> {
    const { data } = await api.get<OnboardingChecklist>(
      `/onboarding/application/${applicationId}`,
    );
    return data;
  },

  async update(id: string, request: CreateOnboardingRequest): Promise<OnboardingChecklist> {
    const { data } = await api.put<OnboardingChecklist>(`/onboarding/${id}`, request);
    return data;
  },

  async toggleTask(checklistId: string, taskId: string): Promise<OnboardingChecklist> {
    const { data } = await api.patch<OnboardingChecklist>(
      `/onboarding/${checklistId}/tasks/${taskId}`,
    );
    return data;
  },

  async getAllChecklists(): Promise<OnboardingChecklist[]> {
    const { data } = await api.get<OnboardingChecklist[]>("/onboarding");
    return data;
  },
};
