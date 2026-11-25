package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.entity.Notification;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.NotificationType;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.NotificationRepository;
import fptu.sba301.ats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotificationPersistsRecord() {
        UUID userId = UUID.randomUUID();
        notificationService.createNotification(userId, NotificationType.SYSTEM_ALERT, "Title", "Body", 10L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void broadcastSendsToAllActiveUsers() {
        User u1 = User.builder().id(UUID.randomUUID()).email("a@ats.com").build();
        User u2 = User.builder().id(UUID.randomUUID()).email("b@ats.com").build();
        when(userRepository.findByDeletedFalse()).thenReturn(List.of(u1, u2));

        notificationService.broadcast(NotificationType.SYSTEM_ALERT, "Maintenance", "Tonight");

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    void sendToRoleFiltersUsersByRole() {
        User hr = User.builder().id(UUID.randomUUID()).email("hr@ats.com").role(Role.HR).build();
        when(userRepository.findByRoleAndDeletedFalse(Role.HR)).thenReturn(List.of(hr));

        notificationService.sendToRole(Role.HR, NotificationType.SYSTEM_ALERT, "HR Note", "Review");

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsReadUpdatesOwnedNotification() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("hr@ats.com").build();
        Notification notif = Notification.builder().id(5L).userId(userId).isRead(false).build();

        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(5L, "hr@ats.com");

        assertTrue(notif.isRead());
        verify(notificationRepository).save(notif);
    }

    @Test
    void markAsReadRejectsOtherUsersNotification() {
        User user = User.builder().id(UUID.randomUUID()).email("hr@ats.com").build();
        Notification notif = Notification.builder().id(5L).userId(UUID.randomUUID()).isRead(false).build();

        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notif));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> notificationService.markAsRead(5L, "hr@ats.com"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }

    @Test
    void getUnreadCountAndMarkAllAsReadWorkTogether() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("hr@ats.com").build();
        Notification unread = Notification.builder().id(1L).userId(userId).isRead(false).build();

        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(user));
        when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(1L);
        when(notificationRepository.findByUserIdAndIsReadFalse(userId)).thenReturn(List.of(unread));

        assertEquals(1L, notificationService.getUnreadCount("hr@ats.com"));
        notificationService.markAllAsRead("hr@ats.com");
        assertTrue(unread.isRead());
        verify(notificationRepository).saveAll(List.of(unread));
    }
}
