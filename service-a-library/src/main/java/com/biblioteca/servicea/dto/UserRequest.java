package com.biblioteca.servicea.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    
    @NotBlank( message = "The full names is mandatory" )
    private String fullName;

    @NotBlank( message = "The email is mandatory" )
    @Email( message = "The email has a invalid format" )
    private String email;

    @NotBlank( message = "The password is mandatory" )
    @Size( min = 8, message = "The password must be at least 8 characters long" )
    private String password;

    private String role;

}
