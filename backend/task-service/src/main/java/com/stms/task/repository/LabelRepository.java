package com.stms.task.repository;

import com.stms.task.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByOwnerIdOrderByNameAsc(Long ownerId);

    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
}
