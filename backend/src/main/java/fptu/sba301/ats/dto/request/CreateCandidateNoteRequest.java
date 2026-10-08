package fptu.sba301.ats.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCandidateNoteRequest {
    @NotBlank(message = "Note content cannot be empty")
    private String content;
}
