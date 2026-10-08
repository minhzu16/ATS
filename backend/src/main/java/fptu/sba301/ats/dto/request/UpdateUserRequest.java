package fptu.sba301.ats.dto.request;

import fptu.sba301.ats.enums.Role;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateUserRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private Role role;

    private UUID departmentId;
}
