package fptu.sba301.ats.service;

import fptu.sba301.ats.entity.Notification;
import fptu.sba301.ats.enums.NotificationType;
import fptu.sba301.ats.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void createNotification(java.util.UUID userId, NotificationType type, String title, String message, Long referenceId);
    void broadcast(NotificationType type, String title, String message);
    void sendToRole(Role role, NotificationType type, String title, String message);
    void sendToUsers(java.util.List<java.util.UUID> userIds, NotificationType type, String title, String message);

    Page<Notification> getMyNotifications(String email, Pageable pageable);
    List<Notification> getAllMyNotifications(String email, Boolean unreadOnly);
    long getUnreadCount(String email);
    void markAsRead(Long notificationId, String email);
    void markAllAsRead(String email);
    
    // CRUD for admin
    Page<Notification> getAllNotifications(Pageable pageable);
    Page<fptu.sba301.ats.dto.response.NotificationResponseDTO> getAllNotificationsGrouped(Pageable pageable);
    void deleteNotification(Long id);
}
