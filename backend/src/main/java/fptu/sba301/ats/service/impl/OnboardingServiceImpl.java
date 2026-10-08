package fptu.sba301.ats.service.impl;

import fptu.sba301.ats.dto.request.CreateOnboardingRequest;
import fptu.sba301.ats.dto.response.OnboardingChecklistResponse;
import fptu.sba301.ats.entity.Application;
import fptu.sba301.ats.entity.OnboardingChecklist;
import fptu.sba301.ats.entity.OnboardingTask;
import fptu.sba301.ats.entity.User;
import fptu.sba301.ats.enums.OnboardingStatus;
import fptu.sba301.ats.exception.BusinessException;
import fptu.sba301.ats.repository.ApplicationRepository;
import fptu.sba301.ats.repository.OnboardingChecklistRepository;
import fptu.sba301.ats.repository.OnboardingTaskRepository;
import fptu.sba301.ats.repository.UserRepository;
import fptu.sba301.ats.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final OnboardingChecklistRepository checklistRepository;
    private final OnboardingTaskRepository taskRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OnboardingChecklistResponse create(CreateOnboardingRequest request) {
        Application application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("Application not found", HttpStatus.NOT_FOUND));

        OnboardingChecklist checklist = OnboardingChecklist.builder()
                .application(application)
                .title(request.getTitle())
                .status(OnboardingStatus.NOT_STARTED)
                .tasks(new ArrayList<>())
                .build();
        checklist = checklistRepository.save(checklist);

        if (request.getTasks() != null && !request.getTasks().isEmpty()) {
            OnboardingChecklist savedChecklist = checklist;
            List<OnboardingTask> tasks = request.getTasks().stream()
                    .map(entry -> {
                        User assignedTo = null;
                        if (entry.getAssignedToUserId() != null) {
                            assignedTo = userRepository.findById(entry.getAssignedToUserId()).orElse(null);
                        }
                        return OnboardingTask.builder()
                                .checklist(savedChecklist)
                                .title(entry.getTitle())
                                .description(entry.getDescription())
                                .sortOrder(entry.getSortOrder() != null ? entry.getSortOrder() : 0)
                                .dueDate(entry.getDueDate())
                                .assignedTo(assignedTo)
                                .completed(false)
                                .build();
                    })
                    .collect(Collectors.toList());
            taskRepository.saveAll(tasks);
            checklist.setTasks(tasks);
        }

        return toResponse(checklist);
    }

    @Override
    public List<OnboardingChecklistResponse> getAll() {
        return checklistRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OnboardingChecklistResponse getById(UUID id) {
        OnboardingChecklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Onboarding checklist not found", HttpStatus.NOT_FOUND));
        return toResponse(checklist);
    }

    @Override
    public OnboardingChecklistResponse getByApplicationId(UUID applicationId) {
        OnboardingChecklist checklist = checklistRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new BusinessException("Onboarding checklist not found for this application", HttpStatus.NOT_FOUND));
        return toResponse(checklist);
    }

    @Override
    @Transactional
    public OnboardingChecklistResponse update(UUID id, CreateOnboardingRequest request) {
        OnboardingChecklist checklist = checklistRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Onboarding checklist not found", HttpStatus.NOT_FOUND));

        checklist.setTitle(request.getTitle());

        // Clear and rebuild tasks
        checklist.getTasks().clear();
        if (request.getTasks() != null) {
            for (CreateOnboardingRequest.TaskEntry entry : request.getTasks()) {
                User assignedTo = null;
                if (entry.getAssignedToUserId() != null) {
                    assignedTo = userRepository.findById(entry.getAssignedToUserId()).orElse(null);
                }
                OnboardingTask task = OnboardingTask.builder()
                        .checklist(checklist)
                        .title(entry.getTitle())
                        .description(entry.getDescription())
                        .sortOrder(entry.getSortOrder() != null ? entry.getSortOrder() : 0)
                        .dueDate(entry.getDueDate())
                        .assignedTo(assignedTo)
                        .completed(false)
                        .build();
                checklist.getTasks().add(task);
            }
        }

        updateChecklistStatus(checklist);
        checklist = checklistRepository.save(checklist);
        return toResponse(checklist);
    }

    @Override
    @Transactional
    public OnboardingChecklistResponse toggleTask(UUID checklistId, UUID taskId) {
        OnboardingChecklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow(() -> new BusinessException("Onboarding checklist not found", HttpStatus.NOT_FOUND));

        OnboardingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("Task not found", HttpStatus.NOT_FOUND));

        if (!task.getChecklist().getId().equals(checklistId)) {
            throw new BusinessException("Task does not belong to this checklist", HttpStatus.BAD_REQUEST);
        }

        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);

        updateChecklistStatus(checklist);
        checklist = checklistRepository.save(checklist);
        return toResponse(checklist);
    }

    private void updateChecklistStatus(OnboardingChecklist checklist) {
        List<OnboardingTask> tasks = checklist.getTasks();
        if (tasks == null || tasks.isEmpty()) {
            checklist.setStatus(OnboardingStatus.NOT_STARTED);
            return;
        }

        long completedCount = tasks.stream().filter(OnboardingTask::isCompleted).count();
        if (completedCount == 0) {
            checklist.setStatus(OnboardingStatus.NOT_STARTED);
        } else if (completedCount == tasks.size()) {
            checklist.setStatus(OnboardingStatus.COMPLETED);
        } else {
            checklist.setStatus(OnboardingStatus.IN_PROGRESS);
        }
    }

    private OnboardingChecklistResponse toResponse(OnboardingChecklist checklist) {
        List<OnboardingTask> tasks = checklist.getTasks() != null ? checklist.getTasks() : List.of();
        int totalTasks = tasks.size();
        int completedTasks = (int) tasks.stream().filter(OnboardingTask::isCompleted).count();
        double progressPercent = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;

        return OnboardingChecklistResponse.builder()
                .id(checklist.getId())
                .applicationId(checklist.getApplication() != null ? checklist.getApplication().getId() : null)
                .candidateName(checklist.getApplication() != null && checklist.getApplication().getCandidate() != null
                        ? checklist.getApplication().getCandidate().getFullName() : null)
                .jobTitle(checklist.getApplication() != null && checklist.getApplication().getJob() != null
                        ? checklist.getApplication().getJob().getTitle() : null)
                .title(checklist.getTitle())
                .status(checklist.getStatus())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercent(progressPercent)
                .createdAt(checklist.getCreatedAt())
                .tasks(tasks.stream()
                        .map(t -> OnboardingChecklistResponse.TaskResponse.builder()
                                .id(t.getId())
                                .title(t.getTitle())
                                .description(t.getDescription())
                                .completed(t.isCompleted())
                                .sortOrder(t.getSortOrder())
                                .dueDate(t.getDueDate())
                                .assignedToUserId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
                                .assignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null)
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
