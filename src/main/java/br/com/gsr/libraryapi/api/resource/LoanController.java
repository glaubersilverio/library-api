package br.com.gsr.libraryapi.api.resource;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.gsr.libraryapi.api.dto.BookDTO;
import br.com.gsr.libraryapi.api.dto.LoanDTO;
import br.com.gsr.libraryapi.api.dto.LoanFilterDTO;
import br.com.gsr.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;
import br.com.gsr.libraryapi.service.BookService;
import br.com.gsr.libraryapi.service.LoanService;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

	private BookService bookService;
	private LoanService loanService;
	private ModelMapper mapper;
	
	public LoanController(BookService bookService, LoanService loanService, ModelMapper mapper) {

		this.bookService = bookService;
		this.loanService = loanService;
		this.mapper = mapper;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Long create(@RequestBody LoanDTO dto) {
		Book book = bookService
				.getBookByIsbn(dto.getIsbn())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for informed isbn"));
		Loan entity = Loan.builder()
				.book(book)
				.customer(dto.getCustomer())
				.loanDate(LocalDate.now())
				.build();
		entity = loanService.save(entity);
		return entity.getId();
	}
	
	@PatchMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
		Loan loan = loanService.getById(id).orElseThrow(() ->  new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
		loan.setReturned(dto.getReturned());
		loanService.update(loan);
	}
	
	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public Page<LoanDTO> find (LoanFilterDTO dto, Pageable pageable) {
		Page<Loan> result = this.loanService.find(dto, pageable);
		List<LoanDTO> loans = result
			.getContent()
			.stream()
			.map( entity -> {
				Book book = entity.getBook();
				BookDTO bookDTO = mapper.map(book, BookDTO.class);
				LoanDTO loanDTO = mapper.map(entity, LoanDTO.class);
				loanDTO.setBook(bookDTO);
				return loanDTO;
			}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
	}
	
}
