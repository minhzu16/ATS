package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateCandidateNoteRequest;
import fptu.sba301.ats.dto.response.CandidateNoteResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.Candidate;
import fptu.sba301.ats.entity.CandidateNote;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.ApplicationStatus;
import fptu.sba301.ats.enums.Role;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.CandidateNoteRepository;
import fptu.sba301.ats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateNoteServiceImplTest {

    @Mock
    private CandidateNoteRepository candidateNoteRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CandidateNoteServiceImpl candidateNoteService;

    @Test
    void getNotesByCandidateIdResolvesAuthorName() {
        UUID candidateId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Candidate candidate = Candidate.builder().id(candidateId).build();
        Application app = Application.builder().id(UUID.randomUUID()).candidate(candidate).build();
        CandidateNote note = CandidateNote.builder()
                .id(UUID.randomUUID())
                .application(app)
                .content("Strong Java skills")
                .createdBy(authorId)
                .build();

        when(candidateNoteRepository.findByApplication_Candidate_IdOrderByCreatedAtDesc(candidateId))
                .thenReturn(List.of(note));
        when(userRepository.findById(authorId))
                .thenReturn(Optional.of(User.builder().id(authorId).fullName("Alice HR").build()));

        List<CandidateNoteResponse> notes = candidateNoteService.getNotesByCandidateId(candidateId);
        assertEquals(1, notes.size());
        assertEquals("Alice HR", notes.get(0).getAuthorName());
        assertEquals("Strong Java skills", notes.get(0).getContent());
    }

    @Test
    void addNoteSavesTrimmedNoteForActiveApplication() {
        UUID candidateId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("hr@ats.com").fullName("Bob HR").build();
        Candidate candidate = Candidate.builder().id(candidateId).build();
        Application app = Application.builder().id(UUID.randomUUID()).candidate(candidate).build();

        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.of(app));
        when(candidateNoteRepository.save(any(CandidateNote.class))).thenAnswer(inv -> {
            CandidateNote n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        CandidateNoteResponse res = candidateNoteService.addNote(
                candidateId, new CreateCandidateNoteRequest("  Great fit  "), "hr@ats.com");
        assertEquals("Great fit", res.getContent());
        assertEquals("Bob HR", res.getAuthorName());
    }

    @Test
    void addNoteThrowsWhenNoActiveApplication() {
        UUID candidateId = UUID.randomUUID();
        User user = User.builder().id(UUID.randomUUID()).email("hr@ats.com").build();
        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(user));
        when(applicationRepository.findTopByCandidate_IdAndStatusOrderByAppliedAtDesc(candidateId, ApplicationStatus.ACTIVE))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> candidateNoteService.addNote(candidateId, new CreateCandidateNoteRequest("Test"), "hr@ats.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void deleteNoteAllowsAuthor() {
        UUID candidateId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User author = User.builder().id(userId).email("hr@ats.com").role(Role.HR).build();
        Candidate candidate = Candidate.builder().id(candidateId).build();
        Application app = Application.builder().id(UUID.randomUUID()).candidate(candidate).build();
        CandidateNote note = CandidateNote.builder().id(noteId).application(app).createdBy(userId).build();

        when(userRepository.findByEmailAndDeletedFalse("hr@ats.com")).thenReturn(Optional.of(author));
        when(candidateNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

        candidateNoteService.deleteNote(candidateId, noteId, "hr@ats.com");
        verify(candidateNoteRepository).delete(note);
    }

    @Test
    void deleteNoteRejectsNonAuthorNonPrivilegedUser() {
        UUID candidateId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        User interviewer = User.builder().id(UUID.randomUUID()).email("int@ats.com").role(Role.INTERVIEWER).build();
        Candidate candidate = Candidate.builder().id(candidateId).build();
        Application app = Application.builder().id(UUID.randomUUID()).candidate(candidate).build();
        CandidateNote note = CandidateNote.builder().id(noteId).application(app).createdBy(UUID.randomUUID()).build();

        when(userRepository.findByEmailAndDeletedFalse("int@ats.com")).thenReturn(Optional.of(interviewer));
        when(candidateNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> candidateNoteService.deleteNote(candidateId, noteId, "int@ats.com"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
    }
}
