package fptu.sba301.ats.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "scorecard_criteria")
public class ScorecardCriterion extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ScorecardTemplate template;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "weight", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal weight = new BigDecimal("1.00");
}
