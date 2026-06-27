package com.biblioteca.servicea.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceBLoanResponse {
    
    private Long id;

    private Long userId;

    private Long bookId;

    private String status;

    private LocalDateTime loanDate;

    private LocalDateTime dueDate;

    private LocalDateTime returnDate;

}
