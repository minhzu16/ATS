package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.dto.request.CreateCandidateNoteRequest;
import fptu.sba301.ats.dto.response.CandidateNoteResponse;
import fptu.sba301.ats.service.CandidateNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static fptu.sba301.ats.constant.AppConstant.BASE_URL;

@RestController
@RequestMapping(BASE_URL + "/candidates/{candidateId}/notes")
@RequiredArgsConstructor
public class CandidateNoteController {

    private final CandidateNoteService candidateNoteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER')")
    public ResponseEntity<List<CandidateNoteResponse>> getNotes(@PathVariable UUID candidateId) {
        return ResponseEntity.ok(candidateNoteService.getNotesByCandidateId(candidateId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER', 'INTERVIEWER')")
    @LogAudit(action = "CREATE_CANDIDATE_NOTE", resource = "CANDIDATE_NOTE")
    public ResponseEntity<CandidateNoteResponse> addNote(
            @PathVariable UUID candidateId,
            @Valid @RequestBody CreateCandidateNoteRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(candidateNoteService.addNote(candidateId, request, authentication.getName()));
    }

    @DeleteMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('HR', 'HR_MANAGER')")
    @LogAudit(action = "DELETE_CANDIDATE_NOTE", resource = "CANDIDATE_NOTE")
    public ResponseEntity<Void> deleteNote(
            @PathVariable UUID candidateId,
            @PathVariable UUID noteId,
            Authentication authentication) {
        candidateNoteService.deleteNote(candidateId, noteId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
