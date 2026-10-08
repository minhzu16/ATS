package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.dto.request.DepartmentRequestDTO;
import fptu.sba301.ats.dto.response.DepartmentResponseDTO;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import fptu.sba301.ats.exception.BusinessException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    @SuppressWarnings("null")
    public Page<DepartmentResponseDTO> getAllDepartments(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Override
    public DepartmentResponseDTO getDepartmentById(UUID id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Department not found", HttpStatus.NOT_FOUND));
        return toDTO(dept);
    }

    @Override
    @LogAudit(action = "CREATE", resource = "Department")
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO req) {
        if (departmentRepository.existsByName(req.getName())) {
            throw new BusinessException("Department name already exists", HttpStatus.BAD_REQUEST);
        }
        Department dept = Department.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();
        return toDTO(departmentRepository.save(dept));
    }

    @Override
    @LogAudit(action = "UPDATE", resource = "Department")
    public DepartmentResponseDTO updateDepartment(UUID id, DepartmentRequestDTO req) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Department not found", HttpStatus.NOT_FOUND));
        
        if (!dept.getName().equals(req.getName()) && departmentRepository.existsByName(req.getName())) {
            throw new BusinessException("Department name already exists", HttpStatus.BAD_REQUEST);
        }
        dept.setName(req.getName());
        dept.setDescription(req.getDescription());
        return toDTO(departmentRepository.save(dept));
    }

    @Override
    @LogAudit(action = "TOGGLE_STATUS", resource = "Department")
    public void toggleDepartmentStatus(UUID id, boolean active) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Department not found", HttpStatus.NOT_FOUND));
        dept.setActive(active);
        departmentRepository.save(dept);
    }

    @Override
    @LogAudit(action = "DELETE", resource = "Department")
    public void deleteDepartment(UUID id) {
        departmentRepository.deleteById(id);
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private DepartmentResponseDTO toDTO(Department dept) {
        long employeeCount = userRepository.countByDepartment_IdAndDeletedFalse(dept.getId());
        long openPositions = jobRepository.countByDepartment_IdAndStatusNot(dept.getId(), JobStatus.CLOSED);

        return DepartmentResponseDTO.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .employeeCount((int) employeeCount)
                .openPositions((int) openPositions)
                .status(dept.isActive())
                .build();
    }
}
