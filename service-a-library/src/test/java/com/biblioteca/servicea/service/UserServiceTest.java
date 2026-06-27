package com.biblioteca.servicea.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.biblioteca.servicea.dto.UserRequest;
import com.biblioteca.servicea.dto.UserResponse;
import com.biblioteca.servicea.entity.Role;
import com.biblioteca.servicea.entity.User;
import com.biblioteca.servicea.exception.BusinessException;
import com.biblioteca.servicea.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldFail_IfEmailAlreadyExists() {
        UserRequest request = new UserRequest();
        request.setEmail("exists@correo.com");
        request.setFullName("Test-name");
        request.setPassword("password123");

        when(userRepository.existsByEmail("exists@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_shouldAssignDefaultUserRole_IfNotSpecified() {
        UserRequest request = new UserRequest();
        request.setEmail("new@correo.com");
        request.setFullName("New User");
        request.setPassword("password123");
        request.setRole(null);

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.create(request);

        assertThat(response.getRole()).isEqualTo(Role.USER.name());
        assertThat(response.getEmail()).isEqualTo("new@correo.com");
    }

    @Test
    void create_shouldReject_AnInvalidRole() {
        UserRequest request = new UserRequest();
        request.setEmail("test.fail@correo.com");
        request.setFullName("Test Names");
        request.setPassword("password123");
        request.setRole("SUPERADMIN");

        when(userRepository.existsByEmail(any())).thenReturn(false);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid Rol");
    }

}
