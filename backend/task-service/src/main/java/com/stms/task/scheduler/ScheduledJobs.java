package com.stms.task.scheduler;

import com.stms.task.entity.Task;
import com.stms.task.entity.TaskStatus;
import com.stms.task.entity.User;
import com.stms.task.grpc.NotificationClient;
import com.stms.task.repository.TaskRepository;
import com.stms.task.service.PriorityScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobs {

    private final TaskRepository taskRepository;
    private final NotificationClient notificationClient;
    private final PriorityScoringService scoring;

    @Value("${app.reminders.enabled:true}")
    private boolean remindersEnabled;

    @Value("${app.reminders.lookahead-days:1}")
    private int lookaheadDays;

    /** Sends a reminder (via gRPC) for every open task that is due soon or overdue, once per due date. */
    @Scheduled(cron = "${app.reminders.cron}")
    public void sendDueReminders() {
        if (!remindersEnabled) return;
        LocalDate limit = LocalDate.now().plusDays(lookaheadDays);
        List<Task> tasks = taskRepository.findDueForReminder(TaskStatus.DONE, limit);
        int sent = 0;
        for (Task task : tasks) {
            User recipient = task.getAssignee() != null ? task.getAssignee() : task.getProject().getOwner();
            boolean delivered = notificationClient.sendReminder(recipient.getEmail(), recipient.getName(),
                    task.getTitle(), task.getProject().getName(), task.getDueDate().toString());
            if (delivered) {
                task.setReminderSent(true);
                taskRepository.save(task);
                sent++;
            }
        }
        if (!tasks.isEmpty()) {
            log.info("Reminder job: {} due, {} delivered", tasks.size(), sent);
        }
    }

    /** Scores change as days pass (a task becomes more urgent), so recalculate every night. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void refreshPriorityScores() {
        LocalDate today = LocalDate.now();
        List<Task> open = taskRepository.findByStatusNot(TaskStatus.DONE);
        for (Task t : open) {
            int score = scoring.calculate(t.getPriority(), t.getDueDate(), t.getStatus(), today);
            if (score != t.getPriorityScore()) {
                t.setPriorityScore(score);
            }
        }
        taskRepository.saveAll(open);
        log.info("Priority scores refreshed for {} open tasks", open.size());
    }
}
