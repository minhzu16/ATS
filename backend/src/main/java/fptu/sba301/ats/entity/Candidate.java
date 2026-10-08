package fptu.sba301.ats.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;



@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "candidates")
public class Candidate extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private java.util.UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "current_company")
    private String currentCompany;

    @Column(name = "source")
    private String source;

    @Column(name = "location")
    private String location;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

}
