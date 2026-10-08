package fptu.sba301.ats.repository;

import fptu.sba301.ats.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(UUID userId, Pageable pageable);
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Notification> findByUserIdAndIsRead(UUID userId, boolean isRead, Pageable pageable);

    List<Notification> findByUserIdAndIsReadFalse(UUID userId);

    long countByUserIdAndIsReadFalse(UUID userId);

    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT MIN(n.id) as id, n.title as title, n.message as message, n.type as type, MAX(n.createdAt) as createdAt, COUNT(n.id) as count " +
           "FROM Notification n " +
           "GROUP BY n.title, n.message, n.type " +
           "ORDER BY MAX(n.createdAt) DESC")
    Page<Object[]> findGroupedNotifications(Pageable pageable);
}