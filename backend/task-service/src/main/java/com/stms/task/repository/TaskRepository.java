package com.stms.task.repository;

import com.stms.task.entity.Task;
import com.stms.task.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /** Dynamic filtering + paging; project and assignee are fetched in the same query (no N+1). */
    @Override
    @EntityGraph(attributePaths = {"project", "assignee"})
    Page<Task> findAll(Specification<Task> spec, Pageable pageable);

    List<Task> findByParentTaskIdOrderByCreatedAtAsc(Long parentTaskId);

    List<Task> findByStatusNot(TaskStatus status);

    /** Tasks that still need a reminder (used by the scheduler). */
    @Query("""
            select t from Task t
              join fetch t.project p
              join fetch p.owner
              left join fetch t.assignee
            where t.status <> :done
              and t.reminderSent = false
              and t.dueDate is not null
              and t.dueDate <= :limit
            """)
    List<Task> findDueForReminder(@Param("done") TaskStatus done, @Param("limit") LocalDate limit);
}
