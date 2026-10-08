package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.CreateOfferRequest;
import fptu.sba301.ats.dto.request.OfferApprovalRequest;
import fptu.sba301.ats.dto.response.OfferApprovalResponse;
import fptu.sba301.ats.dto.response.OfferResponse;
import fptu.sba301.ats.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "CREATE_OFFER", resource = "OFFER")
    public ResponseEntity<OfferResponse> createDraft(
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(offerService.createDraft(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "UPDATE_OFFER", resource = "OFFER")
    public ResponseEntity<OfferResponse> updateDraft(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOfferRequest request) {
        return ResponseEntity.ok(offerService.updateDraft(id, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<OfferResponse>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OfferResponse> getOffer(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.getOffer(id));
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "SUBMIT_OFFER_APPROVAL", resource = "OFFER")
    public ResponseEntity<OfferResponse> submitForApproval(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.submitForApproval(id));
    }

    @PostMapping("/{id}/approval")
    @PreAuthorize("hasRole('HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "APPROVE_REJECT_OFFER", resource = "OFFER")
    public ResponseEntity<OfferApprovalResponse> approveOrReject(
            @PathVariable UUID id,
            @Valid @RequestBody OfferApprovalRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(offerService.approveOrReject(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "SEND_OFFER", resource = "OFFER")
    public ResponseEntity<OfferResponse> sendOffer(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.sendOffer(id));
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "ACCEPT_OFFER", resource = "OFFER")
    public ResponseEntity<OfferResponse> acceptOffer(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.acceptOffer(id));
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @fptu.sba301.ats.annotation.LogAudit(action = "DECLINE_OFFER", resource = "OFFER")
    public ResponseEntity<OfferResponse> declineOffer(
            @PathVariable UUID id,
            @RequestBody(required = false) java.util.Map<String, String> body) {
        String reason = (body != null) ? body.get("reason") : null;
        return ResponseEntity.ok(offerService.declineOffer(id, reason));
    }

    @GetMapping({"/{id}/approvals", "/{id}/history"})
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<OfferApprovalResponse>> getApprovalHistory(@PathVariable UUID id) {
        return ResponseEntity.ok(offerService.getApprovalHistory(id));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<byte[]> getOfferPdf(@PathVariable UUID id) {
        byte[] pdfBytes = offerService.generateOfferPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "offer-" + id + ".pdf");
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
