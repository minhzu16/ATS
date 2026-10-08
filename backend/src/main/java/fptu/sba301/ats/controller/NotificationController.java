package fptu.sba301.ats.controller;

import fptu.sba301.ats.entity.Notification;
import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.dto.request.NotificationRequest;
import fptu.sba301.ats.dto.response.NotificationResponseDTO;
import fptu.sba301.ats.enums.NotificationType;
import fptu.sba301.ats.security.UserPrincipal;
import fptu.sba301.ats.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AppConstant.BASE_URL + "/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final fptu.sba301.ats.service.SystemConfigService systemConfigService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Boolean unreadOnly,
            Pageable pageable) {
        Page<Notification> notifications = notificationService.getMyNotifications(principal.getEmail(), pageable);
        Page<NotificationResponseDTO> response = notifications.map(n -> NotificationResponseDTO.builder()
                .id(n.getId().toString())
                .title(n.getTitle())
                .message(n.getMessage())
                .read(n.isRead())
                .type(n.getType() != null ? n.getType().name() : "SYSTEM_ALERT")
                .createdAt(n.getCreatedAt())
                .build());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getEmail()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String id, @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(Long.valueOf(id), principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "BROADCAST_NOTIFICATION", resource = "Notification")
    public ResponseEntity<Void> broadcast(@RequestBody NotificationRequest request) {
        if (!systemConfigService.getBoolean("NOTIFICATIONS_ENABLED", true)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.broadcast(
                request.getType() != null ? request.getType() : NotificationType.ONBOARDING_ASSIGNED,
                request.getTitle(),
                request.getMessage()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-to-role")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "SEND_NOTIFICATION_ROLE", resource = "Notification")
    public ResponseEntity<Void> sendToRole(@RequestBody NotificationRequest request) {
        if (!systemConfigService.getBoolean("NOTIFICATIONS_ENABLED", true)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        notificationService.sendToRole(
                request.getRole(),
                request.getType() != null ? request.getType() : NotificationType.ONBOARDING_ASSIGNED,
                request.getTitle(),
                request.getMessage()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-to-users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "SEND_NOTIFICATION_USERS", resource = "Notification")
    public ResponseEntity<Void> sendToUsers(@RequestBody NotificationRequest request) {
        notificationService.sendToUsers(
                request.getUserIds(),
                request.getType() != null ? request.getType() : NotificationType.ONBOARDING_ASSIGNED,
                request.getTitle(),
                request.getMessage()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAllNotificationsGrouped(pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @LogAudit(action = "DELETE_NOTIFICATION", resource = "Notification")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
