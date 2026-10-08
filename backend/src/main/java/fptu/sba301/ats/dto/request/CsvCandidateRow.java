package fptu.sba301.ats.dto.request;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CsvCandidateRow {

    @CsvBindByName(column = "fullName", required = true)
    private String fullName;

    @CsvBindByName(column = "email")
    private String email;

    @CsvBindByName(column = "phone")
    private String phone;

    @CsvBindByName(column = "currentCompany")
    private String currentCompany;

    @CsvBindByName(column = "source")
    private String source;

    @CsvBindByName(column = "location")
    private String location;

    @CsvBindByName(column = "experienceYears")
    private Integer experienceYears;

    @CsvBindByName(column = "summary")
    private String summary;

    /** UUID string of the job to link this candidate to (optional). */
    @CsvBindByName(column = "jobId")
    private String jobId;

    /**
     * Exact filename of the PDF uploaded alongside the CSV.
     * Used to match this row's candidate to their CV file (e.g. "john_doe_cv.pdf").
     */
    @CsvBindByName(column = "cvFileName")
    private String cvFileName;
}
