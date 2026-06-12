package com.example.recipeplanner.security;

import com.example.recipeplanner.entity.Role;
import com.example.recipeplanner.entity.User;
import com.example.recipeplanner.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername - found returns UserDetails")
    void loadUser_found() {
        Role role = Role.builder().id(1L).name("ROLE_USER").build();
        User user = User.builder().id(1L).username("alice").password("enc")
                .enabled(true).roles(Set.of(role)).build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails ud = userDetailsService.loadUserByUsername("alice");

        assertThat(ud.getUsername()).isEqualTo("alice");
        assertThat(ud.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("loadUserByUsername - not found throws UsernameNotFoundException")
    void loadUser_notFound_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
