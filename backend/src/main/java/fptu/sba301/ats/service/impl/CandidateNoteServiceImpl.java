package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateCandidateNoteRequest;
import fptu.sba301.ats.dto.response.CandidateNoteResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.CandidateNote;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateNoteRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.CandidateNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateNoteServiceImpl implements CandidateNoteService {

    private final CandidateNoteRepository candidateNoteRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CandidateNoteResponse> getNotesByCandidateId(UUID candidateId) {
        return candidateNoteRepository.findByApplication_Candidate_IdOrderByCreatedAtDesc(candidateId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CandidateNoteResponse addNote(UUID candidateId, CreateCandidateNoteRequest request, String userEmail) {
        User user = userRepository.findByEmailAndDeletedFalse(userEmail)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        Application application = applicationRepository
                .findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Active application not found for candidate", HttpStatus.NOT_FOUND));

        CandidateNote note = CandidateNote.builder()
                .application(application)
                .content(request.getContent().trim())
                .createdBy(user.getId())
                .build();

        note = candidateNoteRepository.save(note);
        return toResponse(note, user.getFullName());
    }

    @Override
    @Transactional
    public void deleteNote(UUID candidateId, UUID noteId, String userEmail) {
        User user = userRepository.findByEmailAndDeletedFalse(userEmail)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND));

        CandidateNote note = candidateNoteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException("Note not found", HttpStatus.NOT_FOUND));

        if (!note.getApplication().getCandidate().getId().equals(candidateId)) {
            throw new BusinessException("Note does not belong to candidate", HttpStatus.BAD_REQUEST);
        }

        // Only author or HR_MANAGER/SYSTEM_ADMIN can delete note
        boolean isAuthor = note.getCreatedBy() != null && note.getCreatedBy().equals(user.getId());
        boolean isPrivileged = user.getRole() == Role.HR_MANAGER || user.getRole() == Role.SYSTEM_ADMIN;
        if (!isAuthor && !isPrivileged) {
            throw new BusinessException("Not authorized to delete this note", HttpStatus.FORBIDDEN);
        }

        candidateNoteRepository.delete(note);
    }

    private CandidateNoteResponse toResponse(CandidateNote note) {
        String authorName = "System";
        if (note.getCreatedBy() != null) {
            authorName = userRepository.findById(note.getCreatedBy())
                    .map(User::getFullName)
                    .orElse("System");
        }
        return toResponse(note, authorName);
    }

    private CandidateNoteResponse toResponse(CandidateNote note, String authorName) {
        return CandidateNoteResponse.builder()
                .id(note.getId())
                .applicationId(note.getApplication() != null ? note.getApplication().getId() : null)
                .candidateId(note.getApplication() != null && note.getApplication().getCandidate() != null
                        ? note.getApplication().getCandidate().getId() : null)
                .content(note.getContent())
                .createdBy(note.getCreatedBy())
                .authorName(authorName)
                .createdAt(note.getCreatedAt())
                .build();
    }
}
