package com.biblioteca.servicea.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.biblioteca.servicea.dto.LoginRequest;
import com.biblioteca.servicea.dto.LoginResponse;
import com.biblioteca.servicea.dto.UserRequest;
import com.biblioteca.servicea.dto.UserResponse;
import com.biblioteca.servicea.service.AuthService;
import com.biblioteca.servicea.service.UserService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Se manda USER siempre para no auto asignarse ADMIN
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        request.setRole("USER");
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

}
