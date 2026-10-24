package com.stms.task.service;

import com.stms.task.dto.LabelDtos.LabelResponse;
import com.stms.task.dto.PageResponse;
import com.stms.task.dto.TaskDtos.TaskRequest;
import com.stms.task.dto.TaskDtos.TaskResponse;
import com.stms.task.entity.*;
import com.stms.task.exception.BadRequestException;
import com.stms.task.exception.ResourceNotFoundException;
import com.stms.task.repository.LabelRepository;
import com.stms.task.repository.TaskRepository;
import com.stms.task.repository.TaskSpecs;
import com.stms.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final UserRepository userRepository;
    private final LabelRepository labelRepository;
    private final PriorityScoringService scoring;

    // ------------------------------------------------------------------ queries

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> search(User user, Long projectId, TaskStatus status, Priority priority,
                                             Long assigneeId, String q, Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecs.accessibleTo(user))
                .and(TaskSpecs.hasProject(projectId))
                .and(TaskSpecs.hasStatus(status))
                .and(TaskSpecs.hasPriority(priority))
                .and(TaskSpecs.hasAssignee(assigneeId))
                .and(TaskSpecs.titleContains(q));
        Page<Task> page = taskRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(t -> toResponse(t, user)));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> topPriority(User user, int limit) {
        Specification<Task> spec = TaskSpecs.accessibleTo(user).and(TaskSpecs.isOpen());
        PageRequest page = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "priorityScore"));
        return taskRepository.findAll(spec, page).map(t -> toResponse(t, user)).getContent();
    }

    @Transactional(readOnly = true)
    public TaskResponse get(Long id, User user) {
        return toResponse(getViewable(id, user), user);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> subtasks(Long parentId, User user) {
        getViewable(parentId, user); // permission check
        return taskRepository.findByParentTaskIdOrderByCreatedAtAsc(parentId).stream()
                .map(t -> toResponse(t, user)).toList();
    }

    // ------------------------------------------------------------------ commands

    @Transactional
    public TaskResponse create(TaskRequest req, User user) {
        if (req.projectId() == null) {
            throw new BadRequestException("projectId is required");
        }
        Project project = projectService.getManageable(req.projectId(), user);
        Task task = new Task();
        task.setProject(project);
        applyFields(task, req, project);
        return toResponse(taskRepository.save(task), user);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req, User user) {
        Task task = getViewable(id, user);
        assertCanManage(task, user);
        if (req.parentTaskId() != null && req.parentTaskId().equals(id)) {
            throw new BadRequestException("A task cannot be its own parent");
        }
        applyFields(task, req, task.getProject());
        return toResponse(task, user);
    }

    /** Owners/admins can set any status; an assignee may also move their own task between columns. */
    @Transactional
    public TaskResponse updateStatus(Long id, TaskStatus status, User user) {
        Task task = getViewable(id, user);
        applyStatus(task, status);
        task.setPriorityScore(scoring.calculate(task.getPriority(), task.getDueDate(), task.getStatus(), LocalDate.now()));
        return toResponse(task, user);
    }

    @Transactional
    public void delete(Long id, User user) {
        Task task = getViewable(id, user);
        assertCanManage(task, user);
        taskRepository.delete(task);
    }

    // ------------------------------------------------------------------ access helpers

    /** Loads a task if the user may see it (owner, admin or assignee), otherwise throws. */
    @Transactional(readOnly = true)
    public Task getViewable(Long id, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task " + id + " not found"));
        if (!canView(task, user)) {
            throw new AccessDeniedException("You cannot access this task");
        }
        return task;
    }

    private boolean canManage(Task t, User u) {
        return u.getRole() == Role.ADMIN || t.getProject().getOwner().getId().equals(u.getId());
    }

    private boolean canView(Task t, User u) {
        return canManage(t, u) || (t.getAssignee() != null && t.getAssignee().getId().equals(u.getId()));
    }

    private void assertCanManage(Task t, User u) {
        if (!canManage(t, u)) {
            throw new AccessDeniedException("Only the project owner can do this");
        }
    }

    // ------------------------------------------------------------------ internals

    private void applyFields(Task t, TaskRequest r, Project project) {
        t.setTitle(r.title().trim());
        t.setDescription(r.description());
        if (r.priority() != null) t.setPriority(r.priority());
        if (r.recurrence() != null) t.setRecurrence(r.recurrence());

        if (!Objects.equals(t.getDueDate(), r.dueDate())) {
            t.setDueDate(r.dueDate());
            t.setReminderSent(false); // new date -> allow a new reminder
        }

        t.setAssignee(r.assigneeId() == null ? null : userRepository.findById(r.assigneeId())
                .orElseThrow(() -> new BadRequestException("Assignee " + r.assigneeId() + " does not exist")));

        if (r.parentTaskId() == null) {
            t.setParentTask(null);
        } else {
            Task parent = taskRepository.findById(r.parentTaskId())
                    .orElseThrow(() -> new BadRequestException("Parent task does not exist"));
            if (!parent.getProject().getId().equals(project.getId())) {
                throw new BadRequestException("Parent task belongs to a different project");
            }
            if (parent.getParentTask() != null) {
                throw new BadRequestException("Subtasks can only be one level deep");
            }
            t.setParentTask(parent);
        }

        t.getLabels().clear();
        if (r.labelIds() != null && !r.labelIds().isEmpty()) {
            List<Label> labels = labelRepository.findAllById(r.labelIds());
            if (labels.size() != r.labelIds().size()) {
                throw new BadRequestException("One or more labels do not exist");
            }
            for (Label l : labels) {
                if (!l.getOwner().getId().equals(project.getOwner().getId())) {
                    throw new BadRequestException("Label \"" + l.getName() + "\" belongs to another user");
                }
            }
            t.getLabels().addAll(labels);
        }

        applyStatus(t, r.status() != null ? r.status() : t.getStatus());
        t.setPriorityScore(scoring.calculate(t.getPriority(), t.getDueDate(), t.getStatus(), LocalDate.now()));
    }

    private void applyStatus(Task t, TaskStatus newStatus) {
        TaskStatus old = t.getStatus();
        if (old == newStatus) return;
        t.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            t.setCompletedAt(LocalDateTime.now());
            spawnNextOccurrence(t);
        } else if (old == TaskStatus.DONE) {
            t.setCompletedAt(null);
        }
    }

    /** Recurring tasks: completing one creates the next occurrence automatically. */
    private void spawnNextOccurrence(Task done) {
        if (done.getRecurrence() == Recurrence.NONE) return;
        LocalDate today = LocalDate.now();
        LocalDate next = advance(done.getDueDate() != null ? done.getDueDate() : today, done.getRecurrence());
        while (next.isBefore(today)) {
            next = advance(next, done.getRecurrence());
        }
        Task copy = new Task();
        copy.setTitle(done.getTitle());
        copy.setDescription(done.getDescription());
        copy.setPriority(done.getPriority());
        copy.setRecurrence(done.getRecurrence());
        copy.setProject(done.getProject());
        copy.setAssignee(done.getAssignee());
        copy.setParentTask(done.getParentTask());
        copy.setLabels(new HashSet<>(done.getLabels()));
        copy.setStatus(TaskStatus.TODO);
        copy.setDueDate(next);
        copy.setPriorityScore(scoring.calculate(copy.getPriority(), next, TaskStatus.TODO, today));
        taskRepository.save(copy);
    }

    private static LocalDate advance(LocalDate date, Recurrence r) {
        return switch (r) {
            case DAILY -> date.plusDays(1);
            case WEEKLY -> date.plusWeeks(1);
            case MONTHLY -> date.plusMonths(1);
            case NONE -> date;
        };
    }

    public TaskResponse toResponse(Task t, User viewer) {
        boolean overdue = t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())
                && t.getStatus() != TaskStatus.DONE;
        List<LabelResponse> labels = t.getLabels().stream()
                .map(l -> new LabelResponse(l.getId(), l.getName(), l.getColor()))
                .sorted(Comparator.comparing(LabelResponse::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        User assignee = t.getAssignee();
        return new TaskResponse(
                t.getId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getRecurrence(),
                t.getDueDate(), t.getPriorityScore(), overdue,
                t.getProject().getId(), t.getProject().getName(),
                assignee == null ? null : assignee.getId(),
                assignee == null ? null : assignee.getName(),
                t.getParentTask() == null ? null : t.getParentTask().getId(),
                labels, canManage(t, viewer),
                t.getCreatedAt(), t.getUpdatedAt(), t.getCompletedAt());
    }
}
