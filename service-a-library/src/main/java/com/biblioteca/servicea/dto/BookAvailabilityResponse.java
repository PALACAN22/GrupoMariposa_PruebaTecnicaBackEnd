package com.biblioteca.servicea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BookAvailabilityResponse {
    
     private Long bookId;

    private boolean exists;

    private boolean available;
    
    private Integer availableCopies;

}
