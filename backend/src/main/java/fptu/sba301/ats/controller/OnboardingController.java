package fptu.sba301.ats.controller;

import fptu.sba301.ats.dto.request.CreateOnboardingRequest;
import fptu.sba301.ats.dto.response.OnboardingChecklistResponse;
import fptu.sba301.ats.service.OnboardingService;
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
@RequestMapping(BASE_URL + "/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OnboardingChecklistResponse> create(
            @Valid @RequestBody CreateOnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(onboardingService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<List<OnboardingChecklistResponse>> getAll() {
        return ResponseEntity.ok(onboardingService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OnboardingChecklistResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(onboardingService.getById(id));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OnboardingChecklistResponse> getByApplicationId(
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(onboardingService.getByApplicationId(applicationId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OnboardingChecklistResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOnboardingRequest request) {
        return ResponseEntity.ok(onboardingService.update(id, request));
    }

    @PatchMapping("/{id}/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    public ResponseEntity<OnboardingChecklistResponse> toggleTask(
            @PathVariable UUID id,
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(onboardingService.toggleTask(id, taskId));
    }
}

