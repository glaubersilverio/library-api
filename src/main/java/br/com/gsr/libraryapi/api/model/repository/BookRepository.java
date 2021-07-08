package br.com.gsr.libraryapi.api.model.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.gsr.libraryapi.api.model.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long>{

	boolean existsByIsbn(String isbn);

	Optional<Book> findByIsbn(String isbn);

}
