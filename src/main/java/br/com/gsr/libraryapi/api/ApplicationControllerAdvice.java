package br.com.gsr.libraryapi.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import br.com.gsr.libraryapi.api.exception.ApiErrors;
import br.com.gsr.libraryapi.exception.BusinessException;

@RestControllerAdvice
public class ApplicationControllerAdvice {
	

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleValidationExceptions(MethodArgumentNotValidException ex) {
		BindingResult bindingResult = ex.getBindingResult();
		return new ApiErrors(bindingResult);
	}
	
	@ExceptionHandler(BusinessException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiErrors handleBusinessException(BusinessException ex) {
		return new ApiErrors(ex);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrors> handleResponseStatusException(ResponseStatusException ex) {
		return new ResponseEntity<ApiErrors>(new ApiErrors(ex), ex.getStatus());
	}
	

}
