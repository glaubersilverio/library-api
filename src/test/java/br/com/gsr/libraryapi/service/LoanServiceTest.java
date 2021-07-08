package br.com.gsr.libraryapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.gsr.libraryapi.api.dto.LoanFilterDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;
import br.com.gsr.libraryapi.api.model.repository.LoanRepository;
import br.com.gsr.libraryapi.exception.BusinessException;
import br.com.gsr.libraryapi.service.impl.LoanServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

	@MockBean
	private LoanRepository repository;
	
	private LoanService service;
	
	@BeforeEach
	public void setUp() {
		this.service = new LoanServiceImpl(repository);
	}

	@Test
	@DisplayName("Deve realizar um empréstimo")
	public void createLoanTest() {
		Book book = Book.builder().id(1l).build();
		Loan savingLoan = Loan.builder()
				.book(book)
				.customer("Fulano")
				.customerEmail("customer@email.com")
				.loanDate(LocalDate.now())
				.build();
		
		Loan savedLoan = Loan.builder()
				.id(1l)
				.book(book)
				.customer("Fulano")
				.loanDate(LocalDate.now())
				.build();
	
		Mockito.when(repository.existsByBookAndNotReturned(Mockito.any(Book.class))).thenReturn(false);
		Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);
		
		Loan loan = service.save(savingLoan);
		
		assertThat(loan.getId()).isEqualTo(savedLoan.getId());
		assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
		assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
		assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());
		
	}
	
	@Test
	@DisplayName("Deve lançar erro de negócio ao salvar um emprestimo com livro ja emprestado")
	public void loanedBookSaveTest() {
		Loan savingLoan = Loan.builder().book(Book.builder().isbn("123").build()).build();
		Mockito.when(repository.existsByBookAndNotReturned(Mockito.any(Book.class))).thenReturn(true);
		
		Throwable exception = catchThrowable(() -> service.save(savingLoan));
		
		assertThat(exception).isInstanceOf(BusinessException.class).hasMessage("Book already loaned.");
		Mockito.verify(repository, Mockito.never()).save(savingLoan);
	}
	
	@Test
	@DisplayName("Deve obter as informações de um empréstimo pelo ID")
	public void getLoanDetailsTest() {
		Long id = 1l;
		Loan loan = Loan.builder()
				.book(Book.builder().id(id).build())
				.customer("Fulano")
				.id(id)
				.loanDate(LocalDate.now())
				.build();
		Mockito.when( repository.findById(id)).thenReturn(Optional.of(loan));
		
		Optional<Loan> foundLoan = service.getById(id);
		
		assertThat(foundLoan).isPresent();
		assertThat(foundLoan.get().getId()).isEqualTo(loan.getId());
		assertThat(foundLoan.get().getBook()).isEqualTo(Book.builder().id(1l).build());
		assertThat(foundLoan.get().getCustomer()).isEqualTo(loan.getCustomer());
		assertThat(foundLoan.get().getLoanDate()).isEqualTo(loan.getLoanDate());
		Mockito.verify(repository).findById(id);
	}
	
	@Test
	@DisplayName("Deve atualizar um empréstimo")
	public void updateBookTest() {
		Long id = 1l;
		Loan loan = Loan.builder()
				.book(Book.builder().id(id).build())
				.customer("Fulano")
				.id(id)
				.loanDate(LocalDate.now())
				.returned(true)
				.build();
		Mockito.when( repository.save(loan) ).thenReturn(loan);
		
		Loan updatedLoan = service.update(loan);
		
		assertThat(updatedLoan.getReturned()).isTrue();
		Mockito.verify(repository).save(loan);
	}
	
	@Test
	@DisplayName("Deve filtrar empréstimos pelas propriedades")
	public void findLoanTest() {
		//cenário
		LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
		
		Long id = 1l;
		Loan loan = Loan.builder()
				.book(Book.builder().id(id).isbn("321").build())
				.customer("Fulano")
				.id(id)
				.loanDate(LocalDate.now())
				.build();

		PageRequest pageRequest = PageRequest.of(0, 100);
		
		List<Loan> list = new ArrayList<>();
		list.add(loan);
		Page<Loan> page = new PageImpl<>(list, pageRequest, list.size());
		
		Mockito.when( repository.findByBookIsbnOrCustomer(
				Mockito.anyString(), 
				Mockito.anyString(), 
				Mockito.any(PageRequest.class)))
			.thenReturn(page);
		
		//execução
		Page<Loan> result = service.find(loanFilterDTO, pageRequest);
		
		
		
		
		
		
	}
	
}
