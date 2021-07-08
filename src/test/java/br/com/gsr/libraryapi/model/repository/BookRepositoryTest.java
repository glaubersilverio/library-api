package br.com.gsr.libraryapi.model.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.repository.BookRepository;
import br.com.gsr.libraryapi.service.EmailService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

	@Autowired
	TestEntityManager entityManager;
	
	@Autowired
	BookRepository bookRepository;
	
	@MockBean
	EmailService emailService;
	
	@Test
	@DisplayName("Deve retornar verdadeiro quando existir o livro na base com o isbn informado")
	public void returnTrueWhenIsbnExists() {
		//cenario
		String isbn = "123";
		entityManager.persist(createNewBook(isbn));
		
		//execucao
		boolean exists = bookRepository.existsByIsbn(isbn);
		
		//verificacao
		assertThat(exists).isTrue();
	}
	
	@Test
	@DisplayName("Deve retornar falso quando não existir o livro na base com o isbn informado")
	public void returnFalseWhenIsbnExists() {
		//cenario
		String isbn = "123";
		
		//execucao
		boolean exists = bookRepository.existsByIsbn(isbn);
		
		//verificacao
		assertThat(exists).isFalse();
	}
	
	@Test
	@DisplayName("Deve obter um livro pelo id")
	public void getByIdTest( ) {
		Book book = createNewBook("123");
		entityManager.persist(book);
		
		Optional<Book> foundBook = bookRepository.findById(book.getId());
		
		assertThat( foundBook.isPresent() ).isTrue();
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		Book book = createNewBook("123");
		book = this.bookRepository.save(book);
		
		assertThat( book.getId() ).isNotNull();
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		Book book = createNewBook("123");
		Book savedBook = entityManager.persist(book);
		bookRepository.delete(savedBook);
		
		Book notFoundBook = entityManager.find(Book.class, savedBook.getId());
		
		assertThat( notFoundBook ).isNull();
		
	}
	
	public static Book createNewBook(String isbn) {
		return Book.builder().title("As aventuras").author("Fulano").isbn(isbn).build();
	}
	
}
