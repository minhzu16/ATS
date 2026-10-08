import api from "@/lib/api";
import type { ScorecardTemplate, CreateScorecardTemplateRequest } from "@/types/scorecard";

export const scorecardService = {
  async getAll(): Promise<ScorecardTemplate[]> {
    const { data } = await api.get<ScorecardTemplate[]>("/scorecard-templates");
    return data;
  },

  async getById(id: string): Promise<ScorecardTemplate> {
    const { data } = await api.get<ScorecardTemplate>(`/scorecard-templates/${id}`);
    return data;
  },

  async getByDepartment(departmentId: string): Promise<ScorecardTemplate[]> {
    const { data } = await api.get<ScorecardTemplate[]>(
      `/scorecard-templates/department/${departmentId}`,
    );
    return data;
  },

  async create(request: CreateScorecardTemplateRequest): Promise<ScorecardTemplate> {
    const { data } = await api.post<ScorecardTemplate>("/scorecard-templates", request);
    return data;
  },

  async update(id: string, request: CreateScorecardTemplateRequest): Promise<ScorecardTemplate> {
    const { data } = await api.put<ScorecardTemplate>(`/scorecard-templates/${id}`, request);
    return data;
  },

  async remove(id: string): Promise<void> {
    await api.delete(`/scorecard-templates/${id}`);
  },
};
