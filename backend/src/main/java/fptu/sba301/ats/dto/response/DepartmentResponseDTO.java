package fptu.sba301.ats.dto.response;

import lombok.Data;
import lombok.Builder;
import java.util.UUID;

@Data
@Builder
public class DepartmentResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String head;
    private Integer employeeCount;
    private Integer openPositions;
    private Boolean status;
    private String createdAt;
    private String updatedAt;
}
