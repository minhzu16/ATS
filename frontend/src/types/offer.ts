import type { CandidateStage } from "./candidate";

export type OfferStatus = "DRAFT" | "PENDING_APPROVAL" | "APPROVED" | "REJECTED" | "SENT" | "ACCEPTED" | "DECLINED";
export type ApprovalStatus = "APPROVED" | "REJECTED";

export interface Offer {
  id: string;
  applicationId?: string;
  /** Pipeline stage of the candidate’s active application (driven by offer actions). */
  applicationStage?: CandidateStage;
  candidateName: string;
  jobTitle: string;
  salary: number;
  positionTitle: string;
  startDate: string | null;
  benefits: string | null;
  notes: string | null;
  status: OfferStatus;
  createdAt: string;
}

export interface OfferApproval {
  id: string;
  offerId: string;
  approvedByName: string;
  status: ApprovalStatus;
  comment: string | null;
  createdAt: string;
}

export interface CreateOfferRequest {
  candidateId: string;
  salary: number;
  positionTitle: string;
  startDate: string | null;
  benefits: string | null;
  notes: string | null;
}

export interface OfferApprovalRequest {
  status: ApprovalStatus;
  comment: string;
}
