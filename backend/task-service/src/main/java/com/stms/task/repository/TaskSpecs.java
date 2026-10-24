package com.stms.task.repository;

import com.stms.task.entity.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reusable query fragments (JPA Criteria API). Methods that receive a null filter return null,
 * which Spring Data simply ignores when specifications are combined with and().
 */
public final class TaskSpecs {

    private TaskSpecs() {
    }

    /** Admins see everything; others see tasks in projects they own or tasks assigned to them. */
    public static Specification<Task> accessibleTo(User user) {
        return (root, query, cb) -> {
            if (user.getRole() == Role.ADMIN) {
                return cb.conjunction();
            }
            Join<Task, Project> project = root.join("project");
            Join<Task, User> assignee = root.join("assignee", JoinType.LEFT);
            return cb.or(
                    cb.equal(project.get("owner").get("id"), user.getId()),
                    cb.equal(assignee.get("id"), user.getId()));
        };
    }

    public static Specification<Task> hasProject(Long projectId) {
        return projectId == null ? null : (root, q, cb) -> cb.equal(root.get("project").get("id"), projectId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return status == null ? null : (root, q, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(Priority priority) {
        return priority == null ? null : (root, q, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> hasAssignee(Long assigneeId) {
        return assigneeId == null ? null : (root, q, cb) -> cb.equal(root.get("assignee").get("id"), assigneeId);
    }

    public static Specification<Task> titleContains(String text) {
        if (text == null || text.isBlank()) return null;
        String pattern = "%" + text.trim().toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    public static Specification<Task> isOpen() {
        return (root, q, cb) -> cb.notEqual(root.get("status"), TaskStatus.DONE);
    }

    public static Specification<Task> dueOn(LocalDate date) {
        return (root, q, cb) -> cb.equal(root.<LocalDate>get("dueDate"), date);
    }

    public static Specification<Task> dueBetween(LocalDate from, LocalDate to) {
        return (root, q, cb) -> cb.between(root.<LocalDate>get("dueDate"), from, to);
    }

    public static Specification<Task> overdue(LocalDate today) {
        return (root, q, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.lessThan(root.<LocalDate>get("dueDate"), today),
                cb.notEqual(root.get("status"), TaskStatus.DONE));
    }

    public static Specification<Task> completedSince(LocalDateTime since) {
        return (root, q, cb) -> cb.and(
                cb.equal(root.get("status"), TaskStatus.DONE),
                cb.greaterThanOrEqualTo(root.<LocalDateTime>get("completedAt"), since));
    }
}
