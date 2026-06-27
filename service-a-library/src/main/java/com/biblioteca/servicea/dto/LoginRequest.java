package com.biblioteca.servicea.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank( message = "The email is mandatory" )
    private String email;

    @NotBlank( message = "The password is mandatory" )
    private String password;

}
