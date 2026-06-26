package com.biblioteca.servicea.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.biblioteca.servicea.entity.Book;
import java.util.List;
import java.util.Optional;


public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);
    boolean existsByIsbn(String isbn);

}
