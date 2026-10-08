package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.OnboardingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingChecklistResponse {
    private UUID id;
    private UUID applicationId;
    private String candidateName;
    private String jobTitle;
    private String title;
    private OnboardingStatus status;
    private int totalTasks;
    private int completedTasks;
    private double progressPercent;
    private LocalDateTime createdAt;
    private List<TaskResponse> tasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResponse {
        private UUID id;
        private String title;
        private String description;
        private boolean completed;
        private Integer sortOrder;
        private LocalDate dueDate;
        private UUID assignedToUserId;
        private String assignedToName;
    }
}
