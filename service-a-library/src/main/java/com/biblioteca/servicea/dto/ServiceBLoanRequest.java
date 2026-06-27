package com.biblioteca.servicea.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBLoanRequest {
    
    private Long userId;
    
    private Long bookId;

}
