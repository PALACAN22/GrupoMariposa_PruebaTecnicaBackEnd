package com.biblioteca.servicea.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.biblioteca.servicea.dto.LoginRequest;
import com.biblioteca.servicea.dto.UserRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {
 
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endpointProtected_WithoutToken_ResultEspected_401or403() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void CompletedFlow_UserRegister_And_LoginAfter() throws Exception {
        UserRequest registerRequest = new UserRequest();
        registerRequest.setFullName("Pedro Lacan");
        registerRequest.setEmail("pedro.lacan@correo.com");
        registerRequest.setPassword("MyPasswordSecuredTest123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("pedro.lacan@correo.com");
        loginRequest.setPassword("MyPasswordSecuredTest123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void login_deberiaFallarConCredencialesIncorrectas() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("not-exist@correo.com");
        loginRequest.setPassword("PasswordDummy123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

}
