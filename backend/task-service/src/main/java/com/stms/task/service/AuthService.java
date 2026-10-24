package com.stms.task.service;

import com.stms.task.dto.AuthDtos.AuthResponse;
import com.stms.task.dto.AuthDtos.LoginRequest;
import com.stms.task.dto.AuthDtos.RegisterRequest;
import com.stms.task.dto.UserDto;
import com.stms.task.entity.Role;
import com.stms.task.entity.User;
import com.stms.task.exception.ConflictException;
import com.stms.task.repository.UserRepository;
import com.stms.task.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = userRepository.save(User.builder()
                .name(req.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.USER) // nobody can self-register as ADMIN
                .build());
        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();
        // Throws BadCredentialsException when email/password are wrong
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, req.password()));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid"));
        return tokensFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        try {
            Claims claims = jwtService.parse(refreshToken);
            if (!JwtService.TYPE_REFRESH.equals(claims.get("type", String.class))) {
                throw new BadCredentialsException("Not a refresh token");
            }
            User user = userRepository.findByEmail(claims.getSubject())
                    .orElseThrow(() -> new BadCredentialsException("Unknown user"));
            return tokensFor(user);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private AuthResponse tokensFor(User user) {
        return new AuthResponse(jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user),
                UserDto.from(user));
    }
}
