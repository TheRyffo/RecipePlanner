package com.example.recipeplanner.service;

import com.example.recipeplanner.dto.AuthDto;
import com.example.recipeplanner.entity.Role;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.exception.DuplicateResourceException;
import com.example.recipeplanner.repository.RoleRepository;
import com.example.recipeplanner.repository.UserRepository;
import com.example.recipeplanner.security.JwtUtils;
import com.example.recipeplanner.service.impl.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Register - success returns token")
    void register_success() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("new@test.com");
        req.setPassword("pass123");

        Role userRole = Role.builder().id(1L).name("ROLE_USER").build();
        User savedUser = User.builder()
                .id(1L).username("newuser").email("new@test.com").password("encoded").build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(savedUser));

        UserDetails mockDetails = mock(UserDetails.class);
        when(mockDetails.getUsername()).thenReturn("newuser");
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(mockDetails);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtils.generateToken(any())).thenReturn("jwt-token");

        AuthDto.AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("Register - duplicate username throws exception")
    void register_duplicateUsername_throws() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("existing");
        req.setEmail("e@test.com");
        req.setPassword("pass123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("existing");
    }

    @Test
    @DisplayName("Register - duplicate email throws exception")
    void register_duplicateEmail_throws() {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setUsername("newuser");
        req.setEmail("dup@test.com");
        req.setPassword("pass123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("dup@test.com");
    }

    @Test
    @DisplayName("Login - success returns token")
    void login_success() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setUsername("alice");
        req.setPassword("pass");

        User user = User.builder().id(1L).username("alice").email("a@test.com").build();

        UserDetails mockDetails = mock(UserDetails.class);
        when(mockDetails.getUsername()).thenReturn("alice");
        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getPrincipal()).thenReturn(mockDetails);
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(jwtUtils.generateToken(any())).thenReturn("token123");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        AuthDto.AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getUsername()).isEqualTo("alice");
    }

    @Test
    @DisplayName("Login - bad credentials throws exception")
    void login_badCredentials_throws() {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}