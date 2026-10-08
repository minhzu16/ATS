export interface ScorecardCriterion {
  id: string;
  name: string;
  weight: number;
}

export interface ScorecardTemplate {
  id: string;
  name: string;
  departmentId: string | null;
  departmentName: string | null;
  criteria: ScorecardCriterion[];
  createdAt: string;
}

export interface CriterionRequest {
  name: string;
  weight: number;
}

export interface CreateScorecardTemplateRequest {
  name: string;
  departmentId: string | null;
  criteria: CriterionRequest[];
}
