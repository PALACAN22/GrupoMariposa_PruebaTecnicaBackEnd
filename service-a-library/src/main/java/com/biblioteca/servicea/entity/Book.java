package com.biblioteca.servicea.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "books" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column( nullable = false )
    private String title;

    @Column( nullable = false )
    private String author;

    @Column( nullable = false, unique = true )
    private String isbn;

    @Column( name = "publication_year" )
    private Integer publicationYear;

    private String genre;

    @Column( name = "total_copies", nullable = false )
    private Integer totalCopies;

    @Column( name = "available_copies", nullable = false )
    private Integer availableCopies;

    @Column( name = "created_at" )
    private LocalDateTime createdAt;

    @PrePersist
    public void PrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (availableCopies == null) {
            availableCopies = totalCopies;
        }
    }
    
}
