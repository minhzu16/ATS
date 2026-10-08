import api from "@/lib/api";
import type {
  Offer,
  OfferApproval,
  CreateOfferRequest,
  OfferApprovalRequest,
} from "@/types/offer";

const BASE_URL_API = import.meta.env.VITE_API_BASE_URL || "http://localhost:8386/api/v1";

export const offerService = {
  async createDraft(request: CreateOfferRequest): Promise<Offer> {
    const { data } = await api.post<Offer>("/offers", request);
    return data;
  },

  async updateDraft(id: string, request: CreateOfferRequest): Promise<Offer> {
    const { data } = await api.put<Offer>(`/offers/${id}`, request);
    return data;
  },

  async getOffer(id: string): Promise<Offer> {
    const { data } = await api.get<Offer>(`/offers/${id}`);
    return data;
  },

  async getAllOffers(): Promise<Offer[]> {
    const { data } = await api.get<Offer[]>("/offers");
    return data;
  },

  async submitForApproval(id: string): Promise<Offer> {
    const { data } = await api.patch<Offer>(`/offers/${id}/submit`);
    return data;
  },

  async approveOrReject(id: string, request: OfferApprovalRequest): Promise<void> {
    await api.post(`/offers/${id}/approval`, request);
  },

  async getApprovalHistory(id: string): Promise<OfferApproval[]> {
    const { data } = await api.get<OfferApproval[]>(`/offers/${id}/history`);
    return data;
  },

  getOfferPdfUrl(id: string): string {
    return `${BASE_URL_API}/offers/${id}/pdf`;
  },

  async getOfferPdfBlob(id: string): Promise<Blob> {
    const { data } = await api.get<Blob>(`/offers/${id}/pdf`, {
      responseType: "blob",
    });
    return data;
  },

  async sendOffer(id: string): Promise<Offer> {
    const { data } = await api.post<Offer>(`/offers/${id}/send`);
    return data;
  },

  async acceptOffer(id: string): Promise<Offer> {
    const { data } = await api.post<Offer>(`/offers/${id}/accept`);
    return data;
  },

  async declineOffer(id: string, reason?: string): Promise<Offer> {
    const { data } = await api.post<Offer>(`/offers/${id}/decline`, { reason });
    return data;
  },
};
