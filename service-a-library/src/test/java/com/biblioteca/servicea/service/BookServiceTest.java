package com.biblioteca.servicea.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.biblioteca.servicea.dto.BookRequest;
import com.biblioteca.servicea.dto.BookResponse;
import com.biblioteca.servicea.entity.Book;
import com.biblioteca.servicea.exception.BusinessException;
import com.biblioteca.servicea.exception.ResourceNotFoundException;
import com.biblioteca.servicea.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;

     @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
                .id(1L)
                .title("Don Quijote de la mancha")
                .author("Miguel de Servantes")
                .isbn("9780307474728")
                .genre("Novela")
                .totalCopies(5)
                .availableCopies(2)
                .build();
    }

    @Test
    void create_FailedExpected_ISBNExists() {
        BookRequest request = new BookRequest();
        request.setIsbn("9780307474728");
        request.setTitle("Test-ISBN");
        request.setAuthor("Test-ISBN");
        request.setTotalCopies(3);

        when(bookRepository.existsByIsbn("9780307474728")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISBN");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveTheBook_WithAvailableCopies_EqualToTheTotal() {
        BookRequest request = new BookRequest();
        request.setIsbn("111");
        request.setTitle("New Title");
        request.setAuthor("New Author");
        request.setTotalCopies(4);

        when(bookRepository.existsByIsbn("111")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setId(99L);
            return b;
        });

        BookResponse response = bookService.create(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getAvailableCopies()).isEqualTo(4);
        assertThat(response.getTotalCopies()).isEqualTo(4);
    }

    @Test
    void decrementAvailableCopies_shouldSubtractOneCopy() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.decrementAvailableCopies(1L);

        assertThat(sampleBook.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void decrementAvailableCopies_shouldFail_IfNoCopiesAreAvailable() {
        sampleBook.setAvailableCopies(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        assertThatThrownBy(() -> bookService.decrementAvailableCopies(1L))
                .isInstanceOf(BusinessException.class);

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getById_shouldThrowNotFound_IfDoesNotExist() {
        when(bookRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getById(123L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
