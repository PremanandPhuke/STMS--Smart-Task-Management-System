package com.stms.task.controller;

import com.stms.task.dto.AuthDtos.AuthResponse;
import com.stms.task.dto.AuthDtos.LoginRequest;
import com.stms.task.dto.AuthDtos.RefreshRequest;
import com.stms.task.dto.AuthDtos.RegisterRequest;
import com.stms.task.dto.UserDto;
import com.stms.task.security.AppUserDetails;
import com.stms.task.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AppUserDetails principal) {
        return UserDto.from(principal.getUser());
    }
}
