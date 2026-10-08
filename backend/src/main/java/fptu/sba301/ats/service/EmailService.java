package fptu.sba301.ats.service;

public interface EmailService {
    /**
     * Send an account activation email with a link to set the password.
     * The link will point to: {frontendUrl}/activate?token={token}
     */
    void sendActivationEmail(String to, String fullName, String token);

    /**
     * Send a password reset email with a link.
     * The link will point to: {frontendUrl}/reset-password?token={token}
     */
    void sendPasswordResetEmail(String to, String fullName, String token);

    /**
     * Send interview invitation email with an attached .ics calendar file.
     */
    void sendInterviewInvitation(
            String to,
            String recipientName,
            String candidateName,
            String jobTitle,
            java.time.LocalDateTime scheduledAt,
            Integer durationMinutes,
            String location,
            String meetingLink
    );

    /**
     * Send interview cancellation notification email.
     */
    void sendInterviewCancellation(
            String to,
            String recipientName,
            String candidateName,
            String jobTitle,
            java.time.LocalDateTime scheduledAt,
            String reason
    );
}
