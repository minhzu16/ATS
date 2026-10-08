package fptu.sba301.ats.service;

import fptu.sba301.ats.dto.request.DepartmentRequestDTO;
import fptu.sba301.ats.dto.response.DepartmentResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DepartmentService {
    Page<DepartmentResponseDTO> getAllDepartments(Pageable pageable);

    DepartmentResponseDTO getDepartmentById(UUID id);

    DepartmentResponseDTO createDepartment(DepartmentRequestDTO request);

    DepartmentResponseDTO updateDepartment(UUID id, DepartmentRequestDTO request);

    void toggleDepartmentStatus(UUID id, boolean active);

    void deleteDepartment(UUID id);
}
