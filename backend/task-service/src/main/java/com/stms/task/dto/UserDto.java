package com.stms.task.dto;

import com.stms.task.entity.Role;
import com.stms.task.entity.User;

public record UserDto(Long id, String name, String email, Role role) {
    public static UserDto from(User u) {
        return new UserDto(u.getId(), u.getName(), u.getEmail(), u.getRole());
    }
}
