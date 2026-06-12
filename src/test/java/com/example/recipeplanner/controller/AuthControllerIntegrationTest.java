package com.example.recipeplanner.controller;

import com.example.recipeplanner.dto.AuthDto;
import com.example.recipeplanner.entity.Role;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.repository.RoleRepository;
import com.example.recipeplanner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Ensure roles exist
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_USER").build());
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
        }
    }

    @Test
    @DisplayName("POST /api/auth/register - success returns token")
    void register_success_returnsToken() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register - duplicate username returns 409")
    void register_duplicateUsername_returns409() throws Exception {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();
        userRepository.save(User.builder()
                .username("existing")
                .email("existing@example.com")
                .password(passwordEncoder.encode("pass123"))
                .roles(Set.of(role))
                .build());

        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("existing");
        request.setEmail("other@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/auth/login - success returns token")
    void login_success_returnsToken() throws Exception {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();
        userRepository.save(User.builder()
                .username("loginuser")
                .email("login@example.com")
                .password(passwordEncoder.encode("secret123"))
                .roles(Set.of(role))
                .build());

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setUsername("loginuser");
        request.setPassword("secret123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    @DisplayName("POST /api/auth/login - wrong password returns 401")
    void login_wrongPassword_returns401() throws Exception {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow();
        userRepository.save(User.builder()
                .username("userX")
                .email("userx@example.com")
                .password(passwordEncoder.encode("correctpassword"))
                .roles(Set.of(role))
                .build());

        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setUsername("userX");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register - invalid email returns 400")
    void register_invalidEmail_returns400() throws Exception {
        AuthDto.RegisterRequest request = new AuthDto.RegisterRequest();
        request.setUsername("user1");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
