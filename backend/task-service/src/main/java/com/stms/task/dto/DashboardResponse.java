package com.stms.task.dto;

import com.stms.task.dto.TaskDtos.TaskResponse;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long total, long todo, long inProgress, long done,
        long overdue, long dueToday, long dueThisWeek, long completedLast7Days,
        Map<String, Long> openByPriority,
        List<TaskResponse> topPriorityTasks) {
}
