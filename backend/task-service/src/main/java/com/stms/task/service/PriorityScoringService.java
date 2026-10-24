package com.stms.task.service;

import com.stms.task.entity.Priority;
import com.stms.task.entity.TaskStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The "smart" part: turns priority + due date into one 0-100 urgency score.
 * Base points come from the priority, extra points from how close (or late) the due date is.
 */
@Service
public class PriorityScoringService {

    public int calculate(Priority priority, LocalDate dueDate, TaskStatus status, LocalDate today) {
        if (status == TaskStatus.DONE) {
            return 0;
        }
        int score = switch (priority) {
            case LOW -> 10;
            case MEDIUM -> 30;
            case HIGH -> 50;
            case URGENT -> 70;
        };
        if (dueDate != null) {
            long days = ChronoUnit.DAYS.between(today, dueDate);
            if (days < 0) {
                score += 40 + (int) Math.min(-days, 10L) * 2; // overdue: 42..60 extra points
            } else if (days == 0) {
                score += 35;
            } else if (days == 1) {
                score += 30;
            } else if (days <= 3) {
                score += 20;
            } else if (days <= 7) {
                score += 10;
            }
        }
        return Math.min(score, 100);
    }
}
