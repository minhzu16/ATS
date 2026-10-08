package fptu.sba301.ats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResponse {

    private int totalRows;
    private int successCount;
    private int failCount;

    /**
     * List of human-readable messages for rows that failed or had warnings:
     * <ul>
     *   <li>"Row 2: fullName is required"</li>
     *   <li>"Row 3: email 'abc@x.com' already exists"</li>
     *   <li>"Row 4: jobId '...' not found"</li>
     *   <li>"Row 5: [WARNING] missing CV file 'john_cv.pdf' – candidate created without CV"</li>
     * </ul>
     */
    private List<String> errors;
}
