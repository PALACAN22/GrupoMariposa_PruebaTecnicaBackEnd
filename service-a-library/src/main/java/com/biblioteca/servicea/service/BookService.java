package com.biblioteca.servicea.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.servicea.dto.BookAvailabilityResponse;
import com.biblioteca.servicea.dto.BookRequest;
import com.biblioteca.servicea.dto.BookResponse;
import com.biblioteca.servicea.entity.Book;
import com.biblioteca.servicea.exception.BusinessException;
import com.biblioteca.servicea.exception.ResourceNotFoundException;
import com.biblioteca.servicea.repository.BookRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookService {
    
    private final BookRepository bookRepository;

    public Page<BookResponse> list(String author, String genre, Boolean available, Pageable pageable) {

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (author != null && !author.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%"));
            }
            if (genre != null && !genre.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("genre")), "%" + genre.toLowerCase() + "%"));
            }
            if (available != null && available) {
                predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookRepository.findAll(spec, pageable).map(this::toResponse);
        
    }

    public BookResponse getById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException("A book with that ISBN already exists " + request.getIsbn());
        }
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationYear(request.getPublicationYear())
                .genre(request.getGenre())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .build();
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = findEntityById(id);
        int prestadas = book.getTotalCopies() - book.getAvailableCopies();
        int nuevasDisponibles = request.getTotalCopies() - prestadas;
        if (nuevasDisponibles < 0) {
            throw new BusinessException("The total number of copies cannot be reduced below the number currently on loan (" + prestadas + ")");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setGenre(request.getGenre());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(nuevasDisponibles);

        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public void delete(Long id) {
        Book book = findEntityById(id);
        bookRepository.delete(book);
    }
    
    public BookAvailabilityResponse checkAvailability(Long id) {
        return bookRepository.findById(id)
                .map(b -> BookAvailabilityResponse.builder()
                        .bookId(b.getId())
                        .exists(true)
                        .available(b.getAvailableCopies() > 0)
                        .availableCopies(b.getAvailableCopies())
                        .build())
                .orElse(BookAvailabilityResponse.builder()
                        .bookId(id)
                        .exists(false)
                        .available(false)
                        .availableCopies(0)
                        .build());
    }

    @Transactional
    public void decrementAvailableCopies(Long bookId) {
        Book book = findEntityById(bookId);
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessException("There are no copies of the book available");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
    }

    @Transactional
    public void incrementAvailableCopies(Long bookId) {
        Book book = findEntityById(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            bookRepository.save(book);
        }
    }

    private Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    private BookResponse toResponse(Book b) {
        return BookResponse.builder()
                .id(b.getId())
                .title(b.getTitle())
                .author(b.getAuthor())
                .isbn(b.getIsbn())
                .publicationYear(b.getPublicationYear())
                .genre(b.getGenre())
                .totalCopies(b.getTotalCopies())
                .availableCopies(b.getAvailableCopies())
                .build();
    }

}

