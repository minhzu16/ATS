package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.CreateCandidateNoteRequest;
import fptu.sba301.ats.dto.response.CandidateNoteResponse;

import java.util.List;
import java.util.UUID;

public interface CandidateNoteService {
    List<CandidateNoteResponse> getNotesByCandidateId(UUID candidateId);
    CandidateNoteResponse addNote(UUID candidateId, CreateCandidateNoteRequest request, String userEmail);
    void deleteNote(UUID candidateId, UUID noteId, String userEmail);
}
