package fptu.sba301.ats.entity;

import fptu.sba301.ats.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jobs")
public class Job extends BaseEntity {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hiring_manager_id")
    private User hiringManager;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;

    @Column(name = "headcount")
    @Builder.Default
    private Integer headcount = 1;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

}
