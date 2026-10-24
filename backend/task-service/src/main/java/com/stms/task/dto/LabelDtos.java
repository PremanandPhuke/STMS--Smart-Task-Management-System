package com.stms.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class LabelDtos {

    private LabelDtos() {
    }

    public record LabelRequest(
            @NotBlank @Size(max = 50) String name,
            @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "color must look like #0f766e") String color) {
    }

    public record LabelResponse(Long id, String name, String color) {
    }
}
