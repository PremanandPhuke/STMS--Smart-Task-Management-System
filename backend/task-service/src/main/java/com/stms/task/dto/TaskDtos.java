package com.stms.task.dto;

import com.stms.task.dto.LabelDtos.LabelResponse;
import com.stms.task.entity.Priority;
import com.stms.task.entity.Recurrence;
import com.stms.task.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class TaskDtos {

    private TaskDtos() {
    }

    public record TaskRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 5000) String description,
            TaskStatus status,
            Priority priority,
            Recurrence recurrence,
            LocalDate dueDate,
            Long projectId,       // required when creating, ignored when updating
            Long assigneeId,
            Long parentTaskId,    // set it to create a subtask
            Set<Long> labelIds) {
    }

    public record StatusUpdateRequest(@NotNull TaskStatus status) {
    }

    public record TaskResponse(
            Long id, String title, String description,
            TaskStatus status, Priority priority, Recurrence recurrence,
            LocalDate dueDate, int priorityScore, boolean overdue,
            Long projectId, String projectName,
            Long assigneeId, String assigneeName,
            Long parentTaskId,
            List<LabelResponse> labels,
            boolean canManage,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime completedAt) {
    }
}
