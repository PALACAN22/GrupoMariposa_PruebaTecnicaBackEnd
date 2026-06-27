package com.biblioteca.servicea.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookRequest {
    
    @NotBlank( message = "The title is mandatory" )
    private String title;

    @NotBlank( message = "The author is mandatory" )
    private String author;
    
    @NotBlank( message =  "The ISBN is mandatory" )
    @Pattern( regexp = "^[0-9\\-Xx]{10,17}$", message = "The ISBN has a invalid format" )
    private String isbn;

    @Min(value = 1800, message = "The publication year is invalid")
    private Integer publicationYear;

    private String genre;

    @NotNull( message = "The total copies is mandatory" )
    @Min( value = 0, message = "The number of copies cannot be negative" )
    private Integer totalCopies;
    
}
