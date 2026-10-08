package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.CreateScorecardTemplateRequest;
import fptu.sba301.ats.dto.response.ScorecardTemplateResponse;
import fptu.sba301.ats.service.ScorecardTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/scorecard-templates")
@RequiredArgsConstructor
public class ScorecardTemplateController {

    private final ScorecardTemplateService scorecardTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<ScorecardTemplateResponse> create(
            @Valid @RequestBody CreateScorecardTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(scorecardTemplateService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER')")
    public ResponseEntity<List<ScorecardTemplateResponse>> getAll() {
        return ResponseEntity.ok(scorecardTemplateService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER')")
    public ResponseEntity<ScorecardTemplateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(scorecardTemplateService.getById(id));
    }

    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<ScorecardTemplateResponse>> getByDepartment(
            @PathVariable UUID departmentId) {
        return ResponseEntity.ok(scorecardTemplateService.getByDepartment(departmentId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<ScorecardTemplateResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateScorecardTemplateRequest request) {
        return ResponseEntity.ok(scorecardTemplateService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        scorecardTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
