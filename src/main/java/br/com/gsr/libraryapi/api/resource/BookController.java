package br.com.gsr.libraryapi.api.resource;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.gsr.libraryapi.api.dto.BookDTO;
import br.com.gsr.libraryapi.api.dto.LoanDTO;
import br.com.gsr.libraryapi.api.model.entity.Book;
import br.com.gsr.libraryapi.api.model.entity.Loan;
import br.com.gsr.libraryapi.service.BookService;
import br.com.gsr.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/books")
@Api("Book API")
public class BookController {
	
	private BookService bookService;
	private LoanService loanService;
	private ModelMapper mapper;
	
	public BookController(BookService bookService, ModelMapper mapper, LoanService loanService) {
		this.bookService = bookService;
		this.loanService = loanService;
		this.mapper = mapper;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ApiOperation("Creates a book")
	public BookDTO create(@RequestBody @Valid BookDTO dto) {
		Book entity = mapper.map(dto, Book.class);
		entity = bookService.save(entity);
		return mapper.map(entity, BookDTO.class);
	}
	
	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Obtains a book details by id")
	public BookDTO get(@PathVariable Long id) {
		
		return bookService
				.getById(id)
				.map( book -> mapper.map(book, BookDTO.class))
				.orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@ApiOperation("Deletes a book by operation")
	public void delete(@PathVariable Long id) {
		Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
		bookService.delete(book);
	}
	
	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	@ApiOperation("Updates a book")
	public BookDTO update(@PathVariable Long id, @RequestBody @Valid BookDTO dto) {
		return bookService.getById(id).map(book -> {
			book.setAuthor(dto.getAuthor());
			book.setTitle(dto.getTitle());
			book = bookService.update(book);
			return mapper.map(book, BookDTO.class);
		}) .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
	}
	
	@GetMapping
	@ApiOperation("Find book by params")
	public Page<BookDTO> find(BookDTO dto, Pageable pageRequest) {
		Book filter = mapper.map(dto, Book.class);
		Page<Book> result = bookService.find(filter, pageRequest);
		List<BookDTO> list = result.getContent().stream().map( entity -> mapper.map(entity, BookDTO.class)).collect(Collectors.toList());
		return new PageImpl<>(list, pageRequest, result.getTotalElements());
	}
	
	@GetMapping("{id}/loans")
	@ApiOperation("Find loans by book")
	public Page<LoanDTO> loansByBook( @PathVariable Long id, Pageable pageable ) {
		Book book = bookService.getById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND) );
	
		Page<Loan> result = loanService.getLoansByBook(book, pageable);
		List<LoanDTO> list = result.getContent()
			.stream()
			.map( loan -> {
				Book loanBook = loan.getBook();
				BookDTO bookDTO = mapper.map(loanBook, BookDTO.class);
				LoanDTO loanDTO = mapper.map(loan, LoanDTO.class);
				loanDTO.setBook(bookDTO);
				return loanDTO;
			}).collect(Collectors.toList());
		return new PageImpl<LoanDTO>(list, pageable, list.size());
	}
}
