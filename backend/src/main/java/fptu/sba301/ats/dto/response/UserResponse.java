package fptu.sba301.ats.dto.response;

import fptu.sba301.ats.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private Role role;
    private boolean active;
    private boolean accountLocked;
    private String department;
    private UUID departmentId;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
