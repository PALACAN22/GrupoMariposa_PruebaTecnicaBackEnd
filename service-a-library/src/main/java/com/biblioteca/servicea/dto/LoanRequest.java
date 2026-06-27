package com.biblioteca.servicea.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequest {
    
    @NotNull( message = "The book´s ID is mandatory" )
    private Long bookId;

}
