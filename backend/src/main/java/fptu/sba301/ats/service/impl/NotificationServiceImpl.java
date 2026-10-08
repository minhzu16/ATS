package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.entity.Notification;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.NotificationType;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.NotificationRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(UUID userId, NotificationType type, String title, String message, Long referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
        log.info("Created notification for user {}, type: {} - {}", userId, type, title);
    }

    @Override
    @Transactional
    public void broadcast(NotificationType type, String title, String message) {
        java.util.List<User> users = userRepository.findByDeletedFalse();
        users.forEach(user -> createNotification(user.getId(), type, title, message, null));
        log.info("Broadcasted notification to {} users", users.size());
    }

    @Override
    @Transactional
    public void sendToRole(Role role, NotificationType type, String title, String message) {
        java.util.List<User> users = userRepository.findByRoleAndDeletedFalse(role);
        users.forEach(user -> createNotification(user.getId(), type, title, message, null));
        log.info("Sent notification to role {} ({} users)", role, users.size());
    }

    @Override
    @Transactional
    public void sendToUsers(java.util.List<java.util.UUID> userIds, NotificationType type, String title, String message) {
        userIds.forEach(userId -> createNotification(userId, type, title, message, null));
        log.info("Sent notification to {} specific users", userIds.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(String email, Pageable pageable) {
        User user = findUserOrThrow(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, String email) {
        User user = findUserOrThrow(email);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("Notification not found", HttpStatus.NOT_FOUND));

        if (!notification.getUserId().equals(user.getId())) {
            throw new BusinessException("Not authorized to read this notification", HttpStatus.FORBIDDEN);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Notification> getAllMyNotifications(String email, Boolean unreadOnly) {
        User user = findUserOrThrow(email);
        if (Boolean.TRUE.equals(unreadOnly)) {
            return notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        }
        return notificationRepository.findByUserId(user.getId(), Pageable.unpaged()).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = findUserOrThrow(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = findUserOrThrow(email);
        java.util.List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<fptu.sba301.ats.dto.response.NotificationResponseDTO> getAllNotificationsGrouped(Pageable pageable) {
        return notificationRepository.findGroupedNotifications(pageable).map(row -> {
            fptu.sba301.ats.enums.NotificationType type = (fptu.sba301.ats.enums.NotificationType) row[3];
            long count = ((Number) row[5]).longValue();
            return fptu.sba301.ats.dto.response.NotificationResponseDTO.builder()
                    .id(row[0].toString())
                    .title(row[1].toString() + " (Sent to " + count + " users)")
                    .message(row[2].toString())
                    .type(type != null ? type.name() : "SYSTEM_ALERT")
                    .read(true) // System log is already read inherently
                    .createdAt((java.time.Instant) row[4])
                    .build();
        });
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    private User findUserOrThrow(String email) {
        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new BusinessException("User not found with email: " + email, HttpStatus.NOT_FOUND));
    }


}
