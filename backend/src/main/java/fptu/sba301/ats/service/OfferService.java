package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateOfferRequest;
import fptu.sba301.ats.dto.request.OfferApprovalRequest;
import fptu.sba301.ats.dto.response.OfferApprovalResponse;
import fptu.sba301.ats.dto.response.OfferResponse;

import java.util.List;
import java.util.UUID;

public interface OfferService {
    OfferResponse createDraft(CreateOfferRequest request);
    OfferResponse updateDraft(UUID id, CreateOfferRequest request);
    OfferResponse getOffer(UUID id);
    List<OfferResponse> getAllOffers();
    OfferResponse submitForApproval(UUID id);
    OfferApprovalResponse approveOrReject(UUID offerId, OfferApprovalRequest request, String userEmail);
    List<OfferApprovalResponse> getApprovalHistory(UUID offerId);
    OfferResponse sendOffer(UUID offerId);
    OfferResponse acceptOffer(UUID offerId);
    OfferResponse declineOffer(UUID offerId, String reason);
    byte[] generateOfferPdf(UUID offerId);
}
