package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.DepartmentRequestDTO;
import fptu.sba301.ats.dto.response.DepartmentResponseDTO;
import fptu.sba301.ats.entity.Department;
import fptu.sba301.ats.enums.JobStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.DepartmentRepository;
import fptu.sba301.ats.repository.JobRepository;
import fptu.sba301.ats.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void getAllDepartmentsMapsCountsAndStatus() {
        UUID id = UUID.randomUUID();
        Department dept = Department.builder().id(id).name("Engineering").description("Core").active(true).build();
        when(departmentRepository.findAll(PageRequest.of(0, 10))).thenReturn(new PageImpl<>(List.of(dept)));
        when(userRepository.countByDepartment_IdAndDeletedFalse(id)).thenReturn(5L);
        when(jobRepository.countByDepartment_IdAndStatusNot(id, JobStatus.CLOSED)).thenReturn(2L);

        Page<DepartmentResponseDTO> page = departmentService.getAllDepartments(PageRequest.of(0, 10));

        assertEquals(1, page.getTotalElements());
        assertEquals("Engineering", page.getContent().get(0).getName());
        assertEquals(5, page.getContent().get(0).getEmployeeCount());
        assertEquals(2, page.getContent().get(0).getOpenPositions());
    }

    @Test
    void getDepartmentByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> departmentService.getDepartmentById(id));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void createDepartmentSavesWhenUnique() {
        DepartmentRequestDTO req = new DepartmentRequestDTO();
        req.setName("Product");
        req.setDescription("PM team");
        UUID id = UUID.randomUUID();

        when(departmentRepository.existsByName("Product")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> {
            Department d = inv.getArgument(0);
            d.setId(id);
            return d;
        });

        DepartmentResponseDTO res = departmentService.createDepartment(req);
        assertEquals(id, res.getId());
        assertEquals("Product", res.getName());
    }

    @Test
    void createDepartmentRejectsDuplicateName() {
        DepartmentRequestDTO req = new DepartmentRequestDTO();
        req.setName("Engineering");
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () -> departmentService.createDepartment(req));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void updateDepartmentUpdatesFields() {
        UUID id = UUID.randomUUID();
        Department existing = Department.builder().id(id).name("Old").description("Old desc").active(true).build();
        DepartmentRequestDTO req = new DepartmentRequestDTO();
        req.setName("New");
        req.setDescription("New desc");

        when(departmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(departmentRepository.existsByName("New")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(inv -> inv.getArgument(0));

        DepartmentResponseDTO res = departmentService.updateDepartment(id, req);
        assertEquals("New", res.getName());
        assertEquals("New desc", res.getDescription());
    }

    @Test
    void toggleDepartmentStatusUpdatesActiveFlag() {
        UUID id = UUID.randomUUID();
        Department dept = Department.builder().id(id).name("Ops").active(true).build();
        when(departmentRepository.findById(id)).thenReturn(Optional.of(dept));

        departmentService.toggleDepartmentStatus(id, false);

        assertFalse(dept.isActive());
        verify(departmentRepository).save(dept);
    }
}
