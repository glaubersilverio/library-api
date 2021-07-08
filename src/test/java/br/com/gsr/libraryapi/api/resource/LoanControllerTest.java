package br.com.gsr.libraryapi.api.resource;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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

import br.com.gsr.libraryapi.api.dto.LoanDTO;
import br.com.gsr.libraryapi.api.dto.LoanFilterDTO;
import br.com.gsr.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;
import br.com.gsr.libraryapi.exception.BusinessException;
import br.com.gsr.libraryapi.service.BookService;
import br.com.gsr.libraryapi.service.EmailService;
import br.com.gsr.libraryapi.service.LoanService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WebMvcTest(controllers = LoanController.class)
public class LoanControllerTest {

	public static final String LOAN_API = "/api/loans";

	@Autowired
	MockMvc mvc;

	@MockBean
	private LoanService loanService;

	@MockBean
	private BookService bookService;
	
	@MockBean
	private EmailService emailService;

	@Test
	@DisplayName("Deve realizar um empréstimo")
	public void createLoanTest() throws Exception {

		LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1l).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

		Loan loan = Loan.builder().id(1l).customer("Fulano").book(book).loanDate(LocalDate.now()).build();
		BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isCreated()).andExpect(content().string("1"));
	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer emprestimo de um livro inexistente")
	public void invalidIsbnLoanTest() throws Exception {
		LoanDTO dto = LoanDTO.builder().customer("Fulano").isbn("123").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.empty());

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book not found for informed isbn"));

	}

	@Test
	@DisplayName("Deve retornar erro ao tentar fazer o emprestimo de um livro já emprestado")
	public void loanedBookErrorOnCreateLoanTest() throws Exception {
		LoanDTO dto = LoanDTO.builder().customer("Fulano").isbn("123").build();
		String json = new ObjectMapper().writeValueAsString(dto);

		Book book = Book.builder().id(1l).isbn("123").build();
		BDDMockito.given(bookService.getBookByIsbn("123")).willReturn(Optional.of(book));

		BDDMockito.given(loanService.save(Mockito.any(Loan.class)))
				.willThrow(new BusinessException("Book already loaned"));

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(LOAN_API).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON).content(json);

		mvc.perform(request).andExpect(status().isBadRequest()).andExpect(jsonPath("errors", Matchers.hasSize(1)))
				.andExpect(jsonPath("errors[0]").value("Book already loaned"));

	}
	
	@Test
	@DisplayName("Deve devolver um livro")
	public void returnBookTest() throws Exception{
		//cenario { returned: true }
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		Loan loan = Loan.builder().id(1l).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		BDDMockito.given( loanService.getById(Mockito.anyLong()) )
			.willReturn(Optional.of(loan));
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.patch(LOAN_API.concat("/1"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isOk() );
		Mockito.verify(loanService, Mockito.times(1)).update(loan);
	}
	
	@Test
	@DisplayName("Deve retornar 404 quanto tentar devolver um livro que não está alugado")
	public void returnedInexistentLoanTest() throws Exception{
		ReturnedLoanDTO dto = ReturnedLoanDTO.builder().returned(true).build();
		String json = new ObjectMapper().writeValueAsString(dto);
		
		BDDMockito.given( loanService.getById(Mockito.anyLong()) ).willReturn(Optional.empty());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.patch(LOAN_API.concat("/1"))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json);
		
		mvc.perform(request)
			.andExpect( status().isNotFound() );
		
	}
	
	@Test
	@DisplayName("Deve filtrar loans")
	public void filterLoanTest() throws Exception{
		//cenário
		Long id = 1l;
		
		Loan loan = Loan.builder()
				.book(Book.builder().id(id).isbn("321").build())
				.customer("Fulano")
				.id(id)
				.loanDate(LocalDate.now())
				.build();
		
		List<Loan> list = new ArrayList<>();
		list.add(loan);
		BDDMockito.given( loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class) ))
			.willReturn( new PageImpl<Loan>(list, PageRequest.of(0, 100), 1));
		
		String queryString = String.format("?isbn=%s&customer=%s&page=0&size=100", loan.getBook().getIsbn(), loan.getCustomer());
		
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
				.get(LOAN_API.concat(queryString))
				.accept(MediaType.APPLICATION_JSON);
	
		mvc.perform(request)
			.andExpect( status().isOk() )
			.andExpect( jsonPath("content", Matchers.hasSize(1)))
			.andExpect( jsonPath("totalElements").value(1))
			.andExpect( jsonPath("pageable.pageSize").value(100))
			.andExpect( jsonPath("pageable.pageNumber").value(0))
			;
	}
}
