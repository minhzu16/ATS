package fptu.sba301.ats.entity;

import fptu.sba301.ats.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offers")
public class Offer extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "salary", precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(name = "position_title")
    private String positionTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private OfferStatus status = OfferStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Version
    @Column(name = "version")
    private Long version;

}

