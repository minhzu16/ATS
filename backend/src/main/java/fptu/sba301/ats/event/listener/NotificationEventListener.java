package fptu.sba301.ats.event.listener;

import fptu.sba301.ats.entity.Notification;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.event.SystemEvent;
import fptu.sba301.ats.repository.NotificationRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void handleSystemEvent(SystemEvent event) {
        log.info("Received SystemEvent: {} - {}", event.getTitle(), event.getContent());

        // Get current authenticated user to exclude them from notification recipients
        UUID currentUserId = getCurrentUserId();

        List<User> users = userRepository.findByDeletedFalse();

        int sentCount = 0;
        for (User user : users) {
            // Skip the sender — notifications should go to OTHER users only
            if (currentUserId != null && user.getId().equals(currentUserId)) {
                continue;
            }

            Notification notification = Notification.builder()
                    .userId(user.getId())
                    .title(event.getTitle())
                    .message(event.getContent())
                    .type(fptu.sba301.ats.enums.NotificationType.SYSTEM_ALERT)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
            sentCount++;
        }

        log.info("Notifications dispatched to {} other users for SystemEvent", sentCount);
    }

    private UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
                return principal.getId();
            }
        } catch (Exception e) {
            log.debug("Could not determine current user for event filtering: {}", e.getMessage());
        }
        return null;
    }
}
