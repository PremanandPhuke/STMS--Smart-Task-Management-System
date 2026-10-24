package com.stms.task.service;

import com.stms.task.entity.Priority;
import com.stms.task.entity.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PriorityScoringServiceTest {

    private final PriorityScoringService service = new PriorityScoringService();
    private final LocalDate today = LocalDate.of(2026, 10, 23);

    @Test
    void doneTasksScoreZero() {
        assertEquals(0, service.calculate(Priority.URGENT, today, TaskStatus.DONE, today));
    }

    @Test
    void noDueDateUsesBasePriorityOnly() {
        assertEquals(10, service.calculate(Priority.LOW, null, TaskStatus.TODO, today));
        assertEquals(70, service.calculate(Priority.URGENT, null, TaskStatus.TODO, today));
    }

    @Test
    void dueTodayAddsPoints() {
        assertEquals(65, service.calculate(Priority.MEDIUM, today, TaskStatus.TODO, today));
    }

    @Test
    void overdueScoresHigherThanUpcoming() {
        int overdue = service.calculate(Priority.MEDIUM, today.minusDays(3), TaskStatus.TODO, today);
        int upcoming = service.calculate(Priority.MEDIUM, today.plusDays(3), TaskStatus.TODO, today);
        assertTrue(overdue > upcoming);
    }

    @Test
    void scoreIsCappedAt100() {
        assertEquals(100, service.calculate(Priority.URGENT, today.minusDays(30), TaskStatus.TODO, today));
    }
}
