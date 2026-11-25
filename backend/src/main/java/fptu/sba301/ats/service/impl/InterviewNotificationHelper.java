package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.service.EmailService;

import java.time.LocalDateTime;

class InterviewNotificationHelper {

    public void sendCancellationEmails(EmailService emailService, Interview interview, String reason) {
        String candidateName = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getFullName() : "Candidate";
        String candidateEmail = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getEmail() : null;
        String jobTitle = (interview.getApplication() != null && interview.getApplication().getJob() != null)
                ? interview.getApplication().getJob().getTitle() : "Position";

        if (candidateEmail != null && !candidateEmail.isBlank()) {
            emailService.sendInterviewCancellation(candidateEmail, candidateName, candidateName, jobTitle, interview.getScheduledAt(), reason);
        }
        if (interview.getParticipants() != null) {
            for (InterviewParticipant p : interview.getParticipants()) {
                if (p.getUser() != null && p.getUser().getEmail() != null) {
                    emailService.sendInterviewCancellation(p.getUser().getEmail(), p.getUser().getFullName(), candidateName, jobTitle, interview.getScheduledAt(), reason);
                }
            }
        }
    }

    public void sendRescheduleEmails(EmailService emailService, Interview interview, LocalDateTime newTime) {
        String candidateName = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getFullName() : "Candidate";
        String candidateEmail = (interview.getApplication() != null && interview.getApplication().getCandidate() != null)
                ? interview.getApplication().getCandidate().getEmail() : null;
        String jobTitle = (interview.getApplication() != null && interview.getApplication().getJob() != null)
                ? interview.getApplication().getJob().getTitle() : "Position";

        if (candidateEmail != null && !candidateEmail.isBlank()) {
            emailService.sendInterviewInvitation(candidateEmail, candidateName, candidateName, jobTitle, newTime, interview.getDurationMinutes(), interview.getLocation(), interview.getMeetingLink());
        }
        if (interview.getParticipants() != null) {
            for (InterviewParticipant p : interview.getParticipants()) {
                if (p.getUser() != null && p.getUser().getEmail() != null) {
                    emailService.sendInterviewInvitation(p.getUser().getEmail(), p.getUser().getFullName(), candidateName, jobTitle, newTime, interview.getDurationMinutes(), interview.getLocation(), interview.getMeetingLink());
                }
            }
        }
    }
}
