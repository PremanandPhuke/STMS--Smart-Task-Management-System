package com.stms.task.service;

import com.stms.task.dto.DashboardResponse;
import com.stms.task.entity.Priority;
import com.stms.task.entity.Task;
import com.stms.task.entity.TaskStatus;
import com.stms.task.entity.User;
import com.stms.task.repository.TaskRepository;
import com.stms.task.repository.TaskSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public DashboardResponse build(User user) {
        LocalDate today = LocalDate.now();
        Specification<Task> mine = TaskSpecs.accessibleTo(user);

        Map<String, Long> byPriority = new LinkedHashMap<>();
        for (Priority p : Priority.values()) {
            byPriority.put(p.name(), taskRepository.count(mine.and(TaskSpecs.isOpen()).and(TaskSpecs.hasPriority(p))));
        }

        return new DashboardResponse(
                taskRepository.count(mine),
                taskRepository.count(mine.and(TaskSpecs.hasStatus(TaskStatus.TODO))),
                taskRepository.count(mine.and(TaskSpecs.hasStatus(TaskStatus.IN_PROGRESS))),
                taskRepository.count(mine.and(TaskSpecs.hasStatus(TaskStatus.DONE))),
                taskRepository.count(mine.and(TaskSpecs.overdue(today))),
                taskRepository.count(mine.and(TaskSpecs.isOpen()).and(TaskSpecs.dueOn(today))),
                taskRepository.count(mine.and(TaskSpecs.isOpen()).and(TaskSpecs.dueBetween(today, today.plusDays(7)))),
                taskRepository.count(mine.and(TaskSpecs.completedSince(LocalDateTime.now().minusDays(7)))),
                byPriority,
                taskService.topPriority(user, 5));
    }
}
