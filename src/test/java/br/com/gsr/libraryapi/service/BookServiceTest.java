package br.com.gsr.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.repository.BookRepository;
import br.com.gsr.libraryapi.exception.BusinessException;
import br.com.gsr.libraryapi.service.impl.BookServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

	BookService service;
	
	@MockBean
	BookRepository repository;
	
	@BeforeEach
	public void setUp() {
		this.service = new BookServiceImpl( repository );
	}
	
	@Test
	@DisplayName("Deve salvar um livro")
	public void saveBookTest() {
		//cenario
		Book book = createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
		Mockito.when( repository.save(book) ).thenReturn(
				Book.builder()
					.id(1l)
					.isbn("123")
					.title("As aventuras")
					.author("Fulano")
					.build());
		
		//execucao
		Book savedBook = service.save(book);
		
		//verificacao
		assertThat(savedBook.getId()).isNotNull();
		assertThat(savedBook.getIsbn()).isEqualTo("123");
		assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
		assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
	
	}
	
	@Test
	@DisplayName("Deve lançar erro de negocio ao tentar salvar um livro com isbn duplicado")
	public void shouldNotSaveABookWithDuplicatedISBN() {
		//cenario
		Book book = this.createValidBook();
		Mockito.when( repository.existsByIsbn(Mockito.anyString())).thenReturn(true);
		
		//execucao
		Throwable exception = Assertions.catchThrowable(() -> service.save(book));
		assertThat(exception)
			.isInstanceOf(BusinessException.class)
			.hasMessage("Isbn já cadastrado.");
		Mockito.verify(repository, Mockito.never()).save(book);
	}
	
	@Test
	@DisplayName("Deve obter um livro por id")
	public void getByIdTest() {
		Long id = 1l;
		Book book = createValidBook();
		book.setId(id);
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));
		
		Optional<Book> foundBook = service.getById(id);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(id);
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		
	}
	
	@Test
	@DisplayName("Deve retornar vazio quando um livro por id quando ele não existe")
	public void bookNotFoundByIdTest() {
		Long id = 1l;
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Book> book = service.getById(id);
		
		assertThat(book.isPresent()).isFalse();
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() {
		//cenario
		Book book = Book.builder().id(1l).build();
		
		//execucao
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));
		
		//verificacoes
		Mockito.verify(repository, Mockito.times(1)).delete(book);
		
	}
	
	@Test
	@DisplayName("Deve lançar erro IllegalArgumentException ao tentar deletar um livro nulo")
	public void deleteInexistentBookTest() {		
		//cenario
		Book book = new Book();
		
		//execucao
		Throwable exception = Assertions.catchThrowable( () -> service.delete(book));
		
		//verificacoes
		Mockito.verify(repository, Mockito.never()).delete(book);;
		assertThat(exception).isInstanceOf(IllegalArgumentException.class);
		assertThat(exception).hasMessage("Book id cant be null.");
		
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() {
		Book updatingBook = Book.builder().id(1l).build();
		
		Book updatedBook = createValidBook();
		updatedBook.setId(1l);
		Mockito.when( repository.save(updatingBook) ).thenReturn(updatedBook);
		
		Book book = service.save(updatingBook);
		assertThat(book.getId()).isEqualTo(updatedBook.getId());
		assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
		assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
		assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
		
		
	}
	
	@Test
	@DisplayName("Deve retornar IllegalArgumentException ao tentar atualizar um livro nulo")
	public void updateInexistentBookTest() {
		Book book = new Book();
		
		Throwable exception = Assertions.catchThrowable( () -> service.update(book) );
		
		Mockito.verify(repository, Mockito.never()).save(book);
		assertThat(exception)
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Book id cant be null.");
	}
	
	@Test
	@DisplayName("Deve filtrar livros pelas propriedades")
	public void findBookTest() {
		Book book = createValidBook();
		
		PageRequest pageRequest = PageRequest.of(0, 100);
		
		List<Book> list = new ArrayList<>();
		list.add(book);
		Page<Book> page = new PageImpl<Book>(list, pageRequest, 1);
		
		Mockito.when( repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
			.thenReturn(page);
		
		Page<Book> result = service.find(book, pageRequest);
		
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getContent()).isEqualTo(list);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getPageable().getPageSize()).isEqualTo(100);	
	}
	
	@Test
	@DisplayName("Deve buscar um livro pelo Isbn")
	public void getBookByIsbnTest() {
		String isbn = "123";
		Book book = createValidBook();
		book.setId(1l);
		
		Mockito.when( repository.findByIsbn(isbn) ).thenReturn(Optional.of(book));
		
		Optional<Book> foundBook = service.getBookByIsbn(isbn);
		
		assertThat(foundBook.isPresent()).isTrue();
		assertThat(foundBook.get().getId()).isEqualTo(book.getId());
		assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
		assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
		assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
		
		Mockito.verify(repository, Mockito.times(1)).findByIsbn(isbn);
		
	}
	
	private Book createValidBook() {
		return Book.builder().author("Fulano").isbn("123").title("As aventuras").build();
	}
}
