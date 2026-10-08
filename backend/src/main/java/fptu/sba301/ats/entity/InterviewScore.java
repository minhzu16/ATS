package fptu.sba301.ats.entity;
import fptu.sba301.ats.entity.BaseEntity;
import fptu.sba301.ats.entity.Interview;
import fptu.sba301.ats.entity.ScorecardCriterion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "interview_scores",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"interview_id", "user_id", "criterion_id"}
        )
)
public class InterviewScore extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User interviewer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private ScorecardCriterion criterion;

    @Column(name = "score")
    private Integer score;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;
}