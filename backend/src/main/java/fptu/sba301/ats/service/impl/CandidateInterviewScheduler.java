package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.ScheduleCandidateInterviewsRequest;
import fptu.sba301.ats.dto.response.ScheduleCandidateInterviewsResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.ScorecardTemplate;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.ParticipantRole;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.InterviewParticipantRepository;
import fptu.sba301.ats.repository.InterviewRepository;
import fptu.sba301.ats.repository.ScorecardTemplateRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.ApplicationStageTransitionService;
import fptu.sba301.ats.service.EmailService;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class CandidateInterviewScheduler {

    public ScheduleCandidateInterviewsResponse schedule(
            UUID candidateId,
            Application application,
            ScheduleCandidateInterviewsRequest request,
            ApplicationStageTransitionService applicationStageTransitionService,
            UserRepository userRepository,
            InterviewParticipantRepository interviewParticipantRepository,
            ScorecardTemplateRepository scorecardTemplateRepository,
            InterviewRepository interviewRepository,
            EmailService emailService,
            CandidateMapper mapper
    ) {
        if (application.getStage() != ApplicationStage.APPLIED
                && application.getStage() != ApplicationStage.SCREENING
                && application.getStage() != ApplicationStage.INTERVIEW) {
            throw new BusinessException(
                    "Only candidates in APPLIED, SCREENING, or INTERVIEW can be scheduled",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (application.getStage() != ApplicationStage.INTERVIEW) {
            applicationStageTransitionService.transition(application, ApplicationStage.INTERVIEW);
        }

        List<UUID> interviewIds = new ArrayList<>();

        for (ScheduleCandidateInterviewsRequest.InterviewSlot slot : request.getInterviews()) {
            if (slot.getScheduledAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException("Cannot schedule an interview in the past", HttpStatus.BAD_REQUEST);
            }

            Set<UUID> uniqueInterviewerIds = Set.copyOf(slot.getInterviewerIds());
            List<User> interviewers = userRepository.findAllById(uniqueInterviewerIds);

            if (interviewers.size() != uniqueInterviewerIds.size()) {
                throw new BusinessException("One or more interviewers not found", HttpStatus.BAD_REQUEST);
            }

            boolean hasInvalidRole = interviewers.stream()
                    .anyMatch(user -> user.getRole() != Role.INTERVIEWER || user.isDeleted());

            if (hasInvalidRole) {
                throw new BusinessException("All assigned users must be active INTERVIEWER", HttpStatus.BAD_REQUEST);
            }

            // Conflict detection: Check if any interviewer has another interview within 60 mins of slot start
            LocalDateTime slotStart = slot.getScheduledAt();
            LocalDateTime slotEnd = slotStart.plusMinutes(slot.getDurationMinutes() != null ? slot.getDurationMinutes() : 60);

            for (User interviewer : interviewers) {
                List<InterviewParticipant> existing = interviewParticipantRepository.findByUserId(interviewer.getId());
                for (InterviewParticipant ep : existing) {
                    Interview other = ep.getInterview();
                    if (other != null && other.getStatus() == InterviewStatus.SCHEDULED && other.getScheduledAt() != null) {
                        LocalDateTime otherStart = other.getScheduledAt();
                        LocalDateTime otherEnd = otherStart.plusMinutes(other.getDurationMinutes() != null ? other.getDurationMinutes() : 60);
                        if (slotStart.isBefore(otherEnd) && slotEnd.isAfter(otherStart)) {
                            throw new BusinessException(
                                    "Interviewer " + interviewer.getFullName() + " has a scheduling conflict at " + slotStart,
                                    HttpStatus.CONFLICT
                            );
                        }
                    }
                }
            }

            // Resolve scorecard template
            ScorecardTemplate template = resolveScorecardTemplate(slot.getTemplateId(), application, scorecardTemplateRepository);

            Interview interview = interviewRepository.save(Interview.builder()
                    .application(application)
                    .template(template)
                    .scheduledAt(slot.getScheduledAt())
                    .durationMinutes(slot.getDurationMinutes() != null ? slot.getDurationMinutes() : 60)
                    .type(slot.getType())
                    .location(slot.getLocation())
                    .meetingLink(slot.getMeetingLink())
                    .status(InterviewStatus.SCHEDULED)
                    .build());

            interviewIds.add(interview.getId());

            List<InterviewParticipant> participants = interviewers.stream()
                    .map(user -> InterviewParticipant.builder()
                            .id(new InterviewParticipant.InterviewParticipantId(interview.getId(), user.getId()))
                            .interview(interview)
                            .user(user)
                            .role(ParticipantRole.INTERVIEWER)
                            .build())
                    .collect(Collectors.toList());

            interviewParticipantRepository.saveAll(participants);

            // Send calendar invitations with .ics attachments
            sendInvitations(application, interviewers, slot, emailService);
        }

        return mapper.toScheduleResponse(
                candidateId,
                application.getId(),
                application.getStage().name(),
                interviewIds
        );
    }

    private ScorecardTemplate resolveScorecardTemplate(
            UUID templateId,
            Application application,
            ScorecardTemplateRepository scorecardTemplateRepository
    ) {
        ScorecardTemplate template = null;
        if (templateId != null) {
            template = scorecardTemplateRepository.findById(templateId).orElse(null);
        }
        if (template == null && application.getJob() != null && application.getJob().getDepartment() != null) {
            List<ScorecardTemplate> deptTemplates = scorecardTemplateRepository.findByDepartmentId(
                    application.getJob().getDepartment().getId()
            );
            if (!deptTemplates.isEmpty()) {
                template = deptTemplates.get(0);
            }
        }
        if (template == null) {
            List<ScorecardTemplate> allTemplates = scorecardTemplateRepository.findAll();
            if (!allTemplates.isEmpty()) {
                template = allTemplates.get(0);
            }
        }
        return template;
    }

    private void sendInvitations(
            Application application,
            List<User> interviewers,
            ScheduleCandidateInterviewsRequest.InterviewSlot slot,
            EmailService emailService
    ) {
        Candidate candidate = application.getCandidate();
        String jobTitle = (application.getJob() != null) ? application.getJob().getTitle() : "Open Position";
        if (candidate != null && candidate.getEmail() != null && !candidate.getEmail().isBlank()) {
            emailService.sendInterviewInvitation(
                    candidate.getEmail(),
                    candidate.getFullName(),
                    candidate.getFullName(),
                    jobTitle,
                    slot.getScheduledAt(),
                    slot.getDurationMinutes(),
                    slot.getLocation(),
                    slot.getMeetingLink()
            );
        }
        for (User interviewer : interviewers) {
            if (interviewer.getEmail() != null && !interviewer.getEmail().isBlank()) {
                emailService.sendInterviewInvitation(
                        interviewer.getEmail(),
                        interviewer.getFullName(),
                        candidate.getFullName(),
                        jobTitle,
                        slot.getScheduledAt(),
                        slot.getDurationMinutes(),
                        slot.getLocation(),
                        slot.getMeetingLink()
                );
            }
        }
    }
}
