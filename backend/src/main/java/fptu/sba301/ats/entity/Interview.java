package fptu.sba301.ats.entity;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.BaseEntity;
import fptu.sba301.ats.entity.InterviewParticipant;
import fptu.sba301.ats.entity.InterviewScore;
import fptu.sba301.ats.enums.InterviewStatus;
import fptu.sba301.ats.enums.InterviewType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interviews")
public class Interview extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ScorecardTemplate template;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "location")
    private String location;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "started_at")
    private Instant startedAt; 

    @Column(name = "ended_at")
    private Instant endedAt; 
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private InterviewType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private InterviewStatus status = InterviewStatus.SCHEDULED;


    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewParticipant> participants;

    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewScore> scores;
}