package fptu.sba301.ats.controller;

import fptu.sba301.ats.annotation.LogAudit;
import fptu.sba301.ats.constant.AdminConstants;
import fptu.sba301.ats.constant.AppConstant;
import fptu.sba301.ats.dto.request.DepartmentRequestDTO;
import fptu.sba301.ats.dto.response.DepartmentResponseDTO;
import fptu.sba301.ats.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping(AppConstant.BASE_URL + AdminConstants.DEPARTMENT_URL)
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<Page<DepartmentResponseDTO>> getAllDepartments(Pageable pageable) {
        return ResponseEntity.ok(departmentService.getAllDepartments(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @PostMapping
    @LogAudit(action = "CREATE", resource = "Department")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.ok(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @LogAudit(action = "UPDATE", resource = "Department")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable UUID id,
            @RequestBody DepartmentRequestDTO request) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, request));
    }

    @PatchMapping("/{id}/status")
    @LogAudit(action = "UPDATE_STATUS", resource = "Department")
    public ResponseEntity<Void> toggleDepartmentStatus(@PathVariable UUID id, @RequestParam boolean active) {
        departmentService.toggleDepartmentStatus(id, active);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @LogAudit(action = "DELETE", resource = "Department")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}
