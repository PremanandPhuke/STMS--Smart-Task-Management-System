package com.stms.task.security;

import com.stms.task.entity.Role;
import com.stms.task.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-that-is-at-least-32-chars-long";
    private final JwtService jwtService = new JwtService(SECRET, 30, 7);
    private final User user = User.builder().id(1L).email("a@b.com").role(Role.USER).build();

    @Test
    void accessTokenRoundTrip() {
        Claims claims = jwtService.parse(jwtService.generateAccessToken(user));
        assertEquals("a@b.com", claims.getSubject());
        assertEquals(JwtService.TYPE_ACCESS, claims.get("type", String.class));
    }

    @Test
    void refreshTokenHasRefreshType() {
        Claims claims = jwtService.parse(jwtService.generateRefreshToken(user));
        assertEquals(JwtService.TYPE_REFRESH, claims.get("type", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateAccessToken(user) + "x";
        assertThrows(JwtException.class, () -> jwtService.parse(token));
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() {
        JwtService other = new JwtService("another-secret-key-that-is-also-32-chars-long!!", 30, 7);
        String token = other.generateAccessToken(user);
        assertThrows(JwtException.class, () -> jwtService.parse(token));
    }
}
