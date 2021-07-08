package br.com.gsr.libraryapi.api.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.gsr.libraryapi.api.dto.BookDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.exception.BusinessException;
import br.com.gsr.libraryapi.service.BookService;
import br.com.gsr.libraryapi.service.EmailService;
import br.com.gsr.libraryapi.service.LoanService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest {

	static String BOOK_API = "/api/books";
	
	@Autowired
	MockMvc mvc;
	
	@MockBean
	BookService service;
	
	@MockBean
	LoanService loanService;
	
	@MockBean
	EmailService emailService;
	
	@Test
	@DisplayName("Deve criar um livro com sucesso")
	public void createBookTest() throws Exception{
		
		BookDTO dto = BookDTO.builder().author("Artur").isbn("001").title("As aventuras").build();
		Book savedBook = Book.builder().author("Artur").id(1l).isbn("001").title("As aventuras").build();
		
		BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.post(BOOK_API)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.content(json);
		
		mvc
			.perform(request)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("id").value(savedBook.getId()))
			.andExpect(jsonPath("title").value(savedBook.getTitle()))
			.andExpect(jsonPath("author").value(savedBook.getAuthor()))
			.andExpect(jsonPath("isbn").value(savedBook.getIsbn()));
	}
	
	@Test
	@DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro")
	public void createInvalidBookTest() throws Exception {
		
		BookDTO dto = BookDTO.builder().build();
		
		String json = new ObjectMapper().writeValueAsString(dto);
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isBadRequest() )
			.andExpect( jsonPath("errors", hasSize(3)));
		
	}
	
	@Test
	@DisplayName("Deve lançar erro ao tentar cadastrar um livro com ISBN já utilizado por outro.")
	public void createBookWithDuplicatedIsbn() throws Exception {
		
		BookDTO dto = BookDTO.builder().author("Artur").title("As aventuras").isbn("001").build();
		String json = new ObjectMapper().writeValueAsString(dto);
		BDDMockito.given( service.save(Mockito.any(Book.class)) )
			.willThrow(new BusinessException("Isbn já cadastrado."));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.post(BOOK_API)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("errors", hasSize(1)))
			.andExpect(jsonPath("errors[0]").value("Isbn já cadastrado."));
		
		
	}
	
	@Test
	@DisplayName("Deve obter informações de um livro")
	public void getBookDetailsTest() throws Exception{
		
		//cenário
		Long id = 1l;
		Book book = Book.builder().id(id).title("As aventuras").isbn("001").author("Artur").build();
		BDDMockito.given( service.getById(id) ).willReturn(Optional.of(book));
		
		
		//execução
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
	
		mvc
			.perform(request)
			.andExpect(status().isOk())
			.andExpect(jsonPath("id").value(book.getId()))
			.andExpect(jsonPath("title").value(book.getTitle()))
			.andExpect(jsonPath("author").value(book.getAuthor()))
			.andExpect(jsonPath("isbn").value(book.getIsbn()));
	
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando o livro procurado não existir")
	public void bookNotFoundTest() throws Exception{
		//cenário		
		Long id = 1l;
		BDDMockito.given( service.getById(id) ).willReturn(Optional.empty());
		
		//execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		//verificacao
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve deletar um livro")
	public void deleteBookTest() throws Exception{
		Long id = 1l;
		
		BDDMockito.given( service.getById(anyLong()) ).willReturn(Optional.of(Book.builder().id(id).build()));
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isNoContent());
		
	}
	
	@Test
	@DisplayName("Deve retornar resource not found quando não encontrar o livro para deletar")
	public void deleteBookNotFoundTest() throws Exception{
		
		Long id = 1l;
		
		BDDMockito.given( service.getById(anyLong()) ).willReturn(Optional.empty());
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.delete(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect(status().isNotFound());
		
	}
	
	@Test
	@DisplayName("Deve atualizar um livro")
	public void updateBookTest() throws Exception{
		
		//cenário
		Long id = 1L;
		Book book = Book.builder().id(id).author("Artur").title("As Aventuras").isbn("001").build();
		String json = new ObjectMapper().writeValueAsString(book);
		
		Book updatingBook = Book.builder().id(id).author("some author").title("some title").isbn("001").build();
		BDDMockito.given( service.getById(anyLong()) ).willReturn(Optional.of(updatingBook));
		BDDMockito.given( service.update(updatingBook) ).willReturn(updatingBook);
		
		//execucao
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + id))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isOk() )
			.andExpect(jsonPath("id").value(updatingBook.getId()))
			.andExpect(jsonPath("title").value(updatingBook.getTitle()))
			.andExpect(jsonPath("author").value(updatingBook.getAuthor()))
			.andExpect(jsonPath("isbn").value(updatingBook.getIsbn()));
	}
	
	@Test
	@DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
	public void updateInexistentBookTest() throws Exception {
		
		Book updatingBook = Book.builder().author("Artur").title("As Aventuras").isbn("001").build();
		String json = new ObjectMapper().writeValueAsString(updatingBook);
		BDDMockito.given( service.getById( anyLong()) ).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.put(BOOK_API.concat("/" + 1))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect(status().isNotFound());
	}
	
	@Test
	@DisplayName("Deve filtrar livros")
	public void findBooksTest() throws Exception{
		
		Long id = 1l;
		
		Book book = Book.builder()
				.id(id)
				.title(createNewBook().getTitle())
				.author(createNewBook().getAuthor())
				.isbn(createNewBook().getIsbn())
				.build();
		
		List<Book> list = new ArrayList<>();
		list.add(book);
		BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)) )
			.willReturn( new PageImpl<Book>(list, PageRequest.of(0, 100), 1));
		
		String queryString = String.format("?title=%s&author=%s&page=0&size=100", 
				book.getTitle(),
				book.getAuthor());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(BOOK_API.concat(queryString))
				.contentType(MediaType.APPLICATION_JSON);
		
		mvc.perform(request)
			.andExpect( status().isOk() )
			.andExpect( jsonPath("content", Matchers.hasSize(1)))
			.andExpect( jsonPath("totalElements").value(1))
			.andExpect( jsonPath("pageable.pageSize").value(100))
			.andExpect( jsonPath("pageable.pageNumber").value(0))
		;
		
	}
	
	private Book createNewBook() {
		return Book.builder().title("As aventuras").isbn("001").author("Artur").build();
	}
	
}
