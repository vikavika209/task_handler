package org.example.service;

import org.example.entity.Role;
import org.example.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.example.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret = "dGhpcyBpcyBhIHNlY3VyZSBrZXkgZm9yIGh0dHBzIHNpZ25pbmcgdGhpcyBrZXk=";
    private long expiration = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        jwtService.secretKey = secret;
        jwtService.jwtExpirationMs = expiration;
    }

    @Test
    void generateAndValidateTokenTest() {
        UserDetails user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtService.extractUsername(token);
        assertEquals(user.getUsername(), username);

        boolean isValid = jwtService.isTokenValid(token, user);
        assertTrue(isValid);
    }

    @Test
    void tokenShouldExpireProperly() throws InterruptedException {

        jwtService.jwtExpirationMs = 1000;
        UserDetails user = User.builder()
                .id(1L)
                .email("expired@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);

        Thread.sleep(2000);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractAllClaims(token));
    }

    @Test
    void claimsShouldContainSubject() {
        UserDetails user = User.builder()
                .id(1L)
                .email("claims@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        String token = jwtService.generateToken(user);

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals(user.getUsername(), claims.getSubject());
    }
}
