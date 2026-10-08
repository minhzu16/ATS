package fptu.sba301.ats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String email;
    private String phone;
    private String currentCompany;
    private String source;
    private String location;
    private Integer experienceYears;
    private String summary;

    private UUID jobId;
}
