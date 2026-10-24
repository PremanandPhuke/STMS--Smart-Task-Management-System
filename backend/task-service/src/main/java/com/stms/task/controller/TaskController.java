package com.stms.task.controller;

import com.stms.task.dto.PageResponse;
import com.stms.task.dto.TaskDtos.StatusUpdateRequest;
import com.stms.task.dto.TaskDtos.TaskRequest;
import com.stms.task.dto.TaskDtos.TaskResponse;
import com.stms.task.entity.Priority;
import com.stms.task.entity.TaskStatus;
import com.stms.task.security.AppUserDetails;
import com.stms.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    /** Only these fields may be used for sorting (prevents sorting by arbitrary/internal columns). */
    private static final Set<String> SORTABLE = Set.of("priorityScore", "dueDate", "createdAt", "updatedAt", "title");

    private final TaskService taskService;

    @GetMapping
    public PageResponse<TaskResponse> list(
            @AuthenticationPrincipal AppUserDetails p,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "priorityScore,desc") String sort) {

        String[] parts = sort.split(",");
        String field = SORTABLE.contains(parts[0]) ? parts[0] : "priorityScore";
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "id")));

        return taskService.search(p.getUser(), projectId, status, priority, assigneeId, q, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        return taskService.get(id, p.getUser());
    }

    @GetMapping("/{id}/subtasks")
    public List<TaskResponse> subtasks(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        return taskService.subtasks(id, p.getUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@Valid @RequestBody TaskRequest req, @AuthenticationPrincipal AppUserDetails p) {
        return taskService.create(req, p.getUser());
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest req,
                               @AuthenticationPrincipal AppUserDetails p) {
        return taskService.update(id, req, p.getUser());
    }

    @PatchMapping("/{id}/status")
    public TaskResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest req,
                                     @AuthenticationPrincipal AppUserDetails p) {
        return taskService.updateStatus(id, req.status(), p.getUser());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails p) {
        taskService.delete(id, p.getUser());
    }
}
