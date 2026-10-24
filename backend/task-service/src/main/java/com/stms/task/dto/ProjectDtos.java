package com.stms.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ProjectDtos {

    private ProjectDtos() {
    }

    public record ProjectRequest(@NotBlank @Size(max = 150) String name, @Size(max = 2000) String description) {
    }

    public record ProjectResponse(Long id, String name, String description, Long ownerId, String ownerName,
                                  LocalDateTime createdAt) {
    }
}
