package com.example.recipeplanner.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpiration", 86400000L);
    }

    private UserDetails user(String username) {
        return new User(username, "pass", Collections.emptyList());
    }

    @Test
    @DisplayName("Generate token - not null")
    void generateToken_notNull() {
        String token = jwtUtils.generateToken(user("alice"));
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Extract username - matches original")
    void extractUsername_matches() {
        String token = jwtUtils.generateToken(user("bob"));
        assertThat(jwtUtils.extractUsername(token)).isEqualTo("bob");
    }

    @Test
    @DisplayName("Validate token - valid returns true")
    void validateToken_valid_returnsTrue() {
        UserDetails ud = user("charlie");
        String token = jwtUtils.generateToken(ud);
        assertThat(jwtUtils.validateToken(token, ud)).isTrue();
    }

    @Test
    @DisplayName("Validate token - wrong user returns false")
    void validateToken_wrongUser_returnsFalse() {
        String token = jwtUtils.generateToken(user("alice"));
        assertThat(jwtUtils.validateToken(token, user("bob"))).isFalse();
    }

    @Test
    @DisplayName("Validate token - garbage token returns false")
    void validateToken_garbageToken_returnsFalse() {
        assertThat(jwtUtils.validateToken("not.a.token", user("alice"))).isFalse();
    }

    @Test
    @DisplayName("Validate token - expired token returns false")
    void validateToken_expiredToken_returnsFalse() {
        JwtUtils expiredUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredUtils, "jwtSecret",
                "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(expiredUtils, "jwtExpiration", -1000L);
        UserDetails ud = user("alice");
        String token = expiredUtils.generateToken(ud);
        assertThat(expiredUtils.validateToken(token, ud)).isFalse();
    }
}
