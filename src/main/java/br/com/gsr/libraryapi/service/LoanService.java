package br.com.gsr.libraryapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.gsr.libraryapi.api.dto.LoanFilterDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;

public interface LoanService {

	Loan save(Loan loan);

	Optional<Loan> getById(Long id);

	Loan update(Loan loan);

	Page<Loan> find(LoanFilterDTO filterDTO, Pageable pageable);

	Page<Loan> getLoansByBook(Book book, Pageable pageable);

	List<Loan> getAllLateLoans();
	
}
