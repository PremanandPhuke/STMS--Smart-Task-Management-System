package com.stms.task.config;

import com.stms.task.entity.*;
import com.stms.task.repository.LabelRepository;
import com.stms.task.repository.ProjectRepository;
import com.stms.task.repository.TaskRepository;
import com.stms.task.repository.UserRepository;
import com.stms.task.service.PriorityScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Creates demo accounts and sample data the first time the app starts on an empty database. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final PriorityScoringService scoring;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled || userRepository.count() > 0) return;

        userRepository.save(User.builder().name("Admin").email("admin@stms.com")
                .passwordHash(passwordEncoder.encode("Admin@123")).role(Role.ADMIN).build());
        User demo = userRepository.save(User.builder().name("Demo User").email("demo@stms.com")
                .passwordHash(passwordEncoder.encode("Demo@1234")).role(Role.USER).build());
        User teammate = userRepository.save(User.builder().name("Riya Sharma").email("riya@stms.com")
                .passwordHash(passwordEncoder.encode("Riya@1234")).role(Role.USER).build());

        Project website = projectRepository.save(Project.builder().name("Website relaunch")
                .description("New marketing site and docs").owner(demo).build());
        Project mobile = projectRepository.save(Project.builder().name("Mobile app v2")
                .description("Offline mode and push notifications").owner(demo).build());

        Label bug = labelRepository.save(Label.builder().name("bug").color("#b91c1c").owner(demo).build());
        Label feature = labelRepository.save(Label.builder().name("feature").color("#0f766e").owner(demo).build());
        Label design = labelRepository.save(Label.builder().name("design").color("#7c3aed").owner(demo).build());

        task(website, "Fix broken pricing page link", TaskStatus.TODO, Priority.URGENT, -2, demo, Recurrence.NONE, bug);
        task(website, "Write launch announcement", TaskStatus.IN_PROGRESS, Priority.HIGH, 1, teammate, Recurrence.NONE, feature);
        task(website, "Design new landing hero", TaskStatus.IN_PROGRESS, Priority.MEDIUM, 4, teammate, Recurrence.NONE, design);
        task(website, "Weekly analytics review", TaskStatus.TODO, Priority.LOW, 6, demo, Recurrence.WEEKLY);
        task(website, "Set up CI pipeline", TaskStatus.DONE, Priority.HIGH, -5, demo, Recurrence.NONE, feature);
        task(mobile, "Implement offline sync", TaskStatus.TODO, Priority.HIGH, 0, demo, Recurrence.NONE, feature);
        task(mobile, "Crash on login with old OS", TaskStatus.TODO, Priority.URGENT, 2, teammate, Recurrence.NONE, bug);
        task(mobile, "Update app store screenshots", TaskStatus.TODO, Priority.LOW, 14, null, Recurrence.NONE, design);

        log.info("Seeded demo data. Logins: admin@stms.com/Admin@123, demo@stms.com/Demo@1234, riya@stms.com/Riya@1234");
    }

    private void task(Project project, String title, TaskStatus status, Priority priority, int dueInDays,
                      User assignee, Recurrence recurrence, Label... labels) {
        Task t = new Task();
        t.setProject(project);
        t.setTitle(title);
        t.setStatus(status);
        t.setPriority(priority);
        t.setRecurrence(recurrence);
        t.setDueDate(LocalDate.now().plusDays(dueInDays));
        t.setAssignee(assignee);
        t.getLabels().addAll(java.util.Arrays.asList(labels));
        if (status == TaskStatus.DONE) t.setCompletedAt(LocalDateTime.now().minusDays(1));
        t.setPriorityScore(scoring.calculate(priority, t.getDueDate(), status, LocalDate.now()));
        taskRepository.save(t);
    }
}
