package fptu.sba301.ats.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class JobPageResponse {
    private List<JobResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
