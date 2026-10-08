package fptu.sba301.ats.entity;

import fptu.sba301.ats.enums.ApplicationStage;
import fptu.sba301.ats.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "candidate_id", "job_id" })
})
public class Application extends BaseEntity {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage")
    @Builder.Default
    private ApplicationStage stage = ApplicationStage.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.ACTIVE;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @PrePersist
    protected void setAppliedAtIfMissing() {
        if (appliedAt == null) {
            appliedAt = LocalDateTime.now(APP_ZONE);
        }
    }

}
