package fptu.sba301.ats.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import fptu.sba301.ats.dto.request.CreateOfferRequest;
import fptu.sba301.ats.dto.request.OfferApprovalRequest;
import fptu.sba301.ats.dto.response.OfferApprovalResponse;
import fptu.sba301.ats.dto.response.OfferResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Offer;
import fptu.sba301.ats.entity.OfferApproval;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApprovalStatus;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.OfferStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.OfferApprovalRepository;
import fptu.sba301.ats.repository.OfferRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.ApplicationStageTransitionService;
import fptu.sba301.ats.service.OfferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final OfferApprovalRepository approvalRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ApplicationStageTransitionService applicationStageTransitionService;

    @Override
    @Transactional
    public OfferResponse createDraft(CreateOfferRequest request) {
        Application application = applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(
                request.getCandidateId(), ApplicationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Active application not found for this candidate", HttpStatus.BAD_REQUEST));

        if (application.getStage() != ApplicationStage.INTERVIEW) {
            throw new BusinessException(
                    "A new offer can only be created for candidates whose current stage is INTERVIEW.",
                    HttpStatus.BAD_REQUEST);
        }

        Offer offer = Offer.builder()
                .application(application)
                .salary(request.getSalary())
                .positionTitle(request.getPositionTitle())
                .startDate(request.getStartDate())
                .benefits(request.getBenefits())
                .notes(request.getNotes())
                .status(OfferStatus.DRAFT)
                .build();

        offer = offerRepository.save(offer);
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse updateDraft(UUID id, CreateOfferRequest request) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new BusinessException("Can only update offers in DRAFT status", HttpStatus.BAD_REQUEST);
        }

        Application application = offer.getApplication();
        if (application.getStage() != ApplicationStage.INTERVIEW) {
            throw new BusinessException(
                    "The offer can only be edited while the candidate is in INTERVIEW stage",
                    HttpStatus.BAD_REQUEST);
        }

        offer.setSalary(request.getSalary());
        offer.setPositionTitle(request.getPositionTitle());
        offer.setStartDate(request.getStartDate());
        offer.setBenefits(request.getBenefits());
        offer.setNotes(request.getNotes());

        offer = offerRepository.save(offer);
        return toResponse(offer);
    }

    @Override
    public OfferResponse getOffer(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));
        return toResponse(offer);
    }

    @Override
    public List<OfferResponse> getAllOffers() {
        return offerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OfferResponse submitForApproval(UUID id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new BusinessException("Can only submit offers in DRAFT status", HttpStatus.BAD_REQUEST);
        }

        Application application = offer.getApplication();
        if (application.getStage() != ApplicationStage.INTERVIEW) {
            throw new BusinessException(
                    "The offer can only be submitted for approval while the candidate is in INTERVIEW stage",
                    HttpStatus.BAD_REQUEST);
        }

        offer.setStatus(OfferStatus.PENDING_APPROVAL);
        offer = offerRepository.save(offer);
        applicationStageTransitionService.transition(application, ApplicationStage.OFFER);
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferApprovalResponse approveOrReject(UUID offerId, OfferApprovalRequest request, String userEmail) {
        User user = userRepository.findByEmailAndDeletedFalse(userEmail)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.PENDING_APPROVAL) {
            throw new BusinessException("Can only approve/reject offers in PENDING_APPROVAL status", HttpStatus.BAD_REQUEST);
        }

        // Enforce separation of duties: offer creator cannot approve their own offer
        if (offer.getCreatedBy() != null && offer.getCreatedBy().equals(user.getId())) {
            throw new BusinessException(
                    "Offer creator cannot approve or reject their own offer (separation of duties violation)",
                    HttpStatus.FORBIDDEN);
        }

        Application application = offer.getApplication();
        if (application.getStage() != ApplicationStage.OFFER) {
            throw new BusinessException(
                    "Offer approval can only be processed when the candidate is in OFFER stage",
                    HttpStatus.BAD_REQUEST);
        }

        // Update offer status
        if (request.getStatus() == ApprovalStatus.APPROVED) {
            offer.setStatus(OfferStatus.APPROVED);
            // Candidate remains in OFFER stage until offer is SENT and ACCEPTED by candidate
        } else {
            offer.setStatus(OfferStatus.REJECTED);
            applicationStageTransitionService.transition(application, ApplicationStage.REJECTED,
                    request.getComment() != null ? request.getComment() : "Offer rejected by manager");
        }
        offerRepository.save(offer);

        // Create approval record
        OfferApproval approval = OfferApproval.builder()
                .offer(offer)
                .approvedBy(user)
                .status(request.getStatus())
                .comment(request.getComment())
                .build();
        approval = approvalRepository.save(approval);

        return toApprovalResponse(approval);
    }

    @Override
    @Transactional
    public OfferResponse sendOffer(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.APPROVED) {
            throw new BusinessException("Can only send offers that are in APPROVED status", HttpStatus.BAD_REQUEST);
        }

        offer.setStatus(OfferStatus.SENT);
        offer = offerRepository.save(offer);
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse acceptOffer(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.SENT) {
            throw new BusinessException("Can only accept offers that are in SENT status", HttpStatus.BAD_REQUEST);
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer = offerRepository.save(offer);
        applicationStageTransitionService.transition(offer.getApplication(), ApplicationStage.HIRED);
        return toResponse(offer);
    }

    @Override
    @Transactional
    public OfferResponse declineOffer(UUID offerId, String reason) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        if (offer.getStatus() != OfferStatus.SENT) {
            throw new BusinessException("Can only decline offers that are in SENT status", HttpStatus.BAD_REQUEST);
        }

        offer.setStatus(OfferStatus.DECLINED);
        offer = offerRepository.save(offer);
        applicationStageTransitionService.transition(offer.getApplication(), ApplicationStage.REJECTED,
                reason != null ? reason : "Candidate declined offer");
        return toResponse(offer);
    }

    @Override
    public List<OfferApprovalResponse> getApprovalHistory(UUID offerId) {
        return approvalRepository.findByOfferIdOrderByCreatedAtDesc(offerId).stream()
                .map(this::toApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] generateOfferPdf(UUID offerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new BusinessException("Offer not found", HttpStatus.NOT_FOUND));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("OFFER LETTER", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Company header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("ATS - Applicant Tracking System", headerFont));
            document.add(new Paragraph(" "));

            // Date
            String dateStr = offer.getCreatedAt() != null
                    ? offer.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))
                    : "N/A";
            document.add(new Paragraph("Date: " + dateStr, normalFont));
            document.add(new Paragraph(" "));

            // Candidate info
            String candidateName = offer.getApplication() != null && offer.getApplication().getCandidate() != null
                    ? offer.getApplication().getCandidate().getFullName()
                    : "N/A";
            document.add(new Paragraph("Dear " + candidateName + ",", normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "We are pleased to offer you the position of " + offer.getPositionTitle() + ".",
                    normalFont));
            document.add(new Paragraph(" "));

            // Details
            document.add(new Paragraph("Position: " + offer.getPositionTitle(), normalFont));
            document.add(new Paragraph("Salary: $" + (offer.getSalary() != null ? offer.getSalary().toString() : "N/A"), normalFont));
            if (offer.getStartDate() != null) {
                document.add(new Paragraph("Start Date: " + offer.getStartDate().toString(), normalFont));
            }
            document.add(new Paragraph(" "));

            if (offer.getBenefits() != null && !offer.getBenefits().isEmpty()) {
                document.add(new Paragraph("Benefits:", headerFont));
                document.add(new Paragraph(offer.getBenefits(), normalFont));
                document.add(new Paragraph(" "));
            }

            if (offer.getNotes() != null && !offer.getNotes().isEmpty()) {
                document.add(new Paragraph("Additional Notes:", headerFont));
                document.add(new Paragraph(offer.getNotes(), normalFont));
                document.add(new Paragraph(" "));
            }

            // Footer
            document.add(new Paragraph(" "));
            document.add(new Paragraph("We look forward to welcoming you to our team!", normalFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Sincerely,", normalFont));
            document.add(new Paragraph("HR Department", normalFont));

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new BusinessException("Failed to generate PDF: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private OfferResponse toResponse(Offer offer) {
        return OfferResponse.builder()
                .id(offer.getId())
                .applicationId(offer.getApplication() != null ? offer.getApplication().getId() : null)
                .applicationStage(offer.getApplication() != null ? offer.getApplication().getStage() : null)
                .candidateName(offer.getApplication() != null && offer.getApplication().getCandidate() != null
                        ? offer.getApplication().getCandidate().getFullName() : null)
                .jobTitle(offer.getApplication() != null && offer.getApplication().getJob() != null
                        ? offer.getApplication().getJob().getTitle() : null)
                .salary(offer.getSalary())
                .positionTitle(offer.getPositionTitle())
                .startDate(offer.getStartDate())
                .benefits(offer.getBenefits())
                .notes(offer.getNotes())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .build();
    }

    private OfferApprovalResponse toApprovalResponse(OfferApproval approval) {
        return OfferApprovalResponse.builder()
                .id(approval.getId())
                .offerId(approval.getOffer() != null ? approval.getOffer().getId() : null)
                .approvedByName(approval.getApprovedBy() != null ? approval.getApprovedBy().getFullName() : null)
                .status(approval.getStatus())
                .comment(approval.getComment())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}
