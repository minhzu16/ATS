package fptu.sba301.ats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitInterviewScoreRequest {

    @NotEmpty(message = "Scores list cannot be empty")
    @Valid
    private List<ScoreEntry> scores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoreEntry {
        @NotNull(message = "Criterion ID is required")
        private UUID criterionId;

        @NotNull(message = "Score is required")
        @Min(value = 1, message = "Score must be at least 1")
        @Max(value = 10, message = "Score must be at most 10")
        private Integer score;

        private String comment;
    }
}
