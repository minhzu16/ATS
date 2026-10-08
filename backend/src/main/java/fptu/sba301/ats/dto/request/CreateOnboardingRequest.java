package fptu.sba301.ats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOnboardingRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    @NotBlank(message = "Title is required")
    private String title;

    @Valid
    private List<TaskEntry> tasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskEntry {
        @NotBlank(message = "Task title is required")
        private String title;
        private String description;
        private Integer sortOrder;
        private LocalDate dueDate;
        private UUID assignedToUserId;
    }
}
