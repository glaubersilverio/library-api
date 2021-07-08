package br.com.gsr.libraryapi.model.repository;

import static br.com.gsr.libraryapi.model.repository.BookRepositoryTest.createNewBook;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;
import br.com.gsr.libraryapi.api.model.repository.LoanRepository;
import br.com.gsr.libraryapi.service.EmailService;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class LoanRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private LoanRepository repository;
	
	@MockBean
	EmailService emailService;
	

	@Test
	@DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
	public void existsByBookAndNotReturnedTest() {
		Loan loan = createAndPersistLoan(LocalDate.now());
		Book book = loan.getBook();

		// execucao
		boolean exists = repository.existsByBookAndNotReturned(book);

		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Deve buscar um empréstimo pelo isbn do livro ou customer")
	public void findByBookIsbnOrCustomerTest() {
		createAndPersistLoan(LocalDate.now());
		
		Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 100));
		
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getPageable().getPageSize()).isEqualTo(100);
		assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
		
	}
	
	@Test
	@DisplayName("Deve obter empréstimos cuja data empréstimo for menor ou igual a três dias atrás e não retornados")
	public void findByLoanDateLessThanAndNotReturnedTest() {
		Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));		
		List<Loan> lateLoans = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		assertThat( lateLoans ).hasSize(1).contains(loan);
		
	}
	
	@Test
	@DisplayName("Deve retornar uma lista vazia ao não encontrar livros com entrega atrasada")
	public void notFindByLoanDateLessThanAndNotReturnedTest() {
		createAndPersistLoan(LocalDate.now());		
		List<Loan> lateLoans = repository.findByLoansDateLessThanAndNotReturned(LocalDate.now().minusDays(4));
		
		assertThat( lateLoans ).isEmpty();
		
	}
	
	public Loan createAndPersistLoan(LocalDate loanDate) {
		Book book = createNewBook("123");
		entityManager.persist(book);

		Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
		entityManager.persist(loan);
		
		return loan;
	}
}
