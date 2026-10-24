package com.stms.task.controller;

import com.stms.task.dto.UserDto;
import com.stms.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /** Used by the UI to pick an assignee. */
    @GetMapping("/api/users")
    public List<UserDto> users() {
        return userRepository.findAll(Sort.by("name")).stream().map(UserDto::from).toList();
    }

    /** Admin-only page. Protected twice: by URL rule in SecurityConfig and by @PreAuthorize. */
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto> adminUsers() {
        return userRepository.findAll(Sort.by("id")).stream().map(UserDto::from).toList();
    }
}
