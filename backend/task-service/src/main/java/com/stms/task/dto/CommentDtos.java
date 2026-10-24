package com.stms.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class CommentDtos {

    private CommentDtos() {
    }

    public record CommentRequest(@NotBlank @Size(max = 2000) String content) {
    }

    public record CommentResponse(Long id, String content, Long authorId, String authorName,
                                  LocalDateTime createdAt) {
    }
}
