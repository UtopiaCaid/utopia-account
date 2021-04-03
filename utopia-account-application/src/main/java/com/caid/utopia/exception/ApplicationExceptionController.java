package com.caid.utopia.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApplicationExceptionController extends ResponseEntityExceptionHandler {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		List<String> errorMessages = new ArrayList<String>();
		logger.error("Method Argument Not Valid");
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			logger.error("Field: {}, Field Error Message: {}", fieldError.getField(),fieldError.getDefaultMessage());
			errorMessages.add(fieldError.getField() + fieldError.getDefaultMessage());
		}
		return ResponseEntity.unprocessableEntity().body(errorMessages);
	}

	@ExceptionHandler(value = AccountNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public ResponseEntity<Object> exception(AccountNotFoundException exception, WebRequest request) {
		logger.error("Account does not exist", exception);
		logWebRequestParameters(request);
		return new ResponseEntity<>("Account was unable to be found", HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(value = OversizedValueException.class)
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	public ResponseEntity<Object> exception(OversizedValueException exception, WebRequest request) {
		logger.error("Entity Field value was too large for the DB", exception);
		logWebRequestParameters(request);
		return new ResponseEntity<>("Entity Field value was too large for the DB", HttpStatus.PAYLOAD_TOO_LARGE);
	}
	
	@ExceptionHandler(value = DuplicateUsernameException.class)
	@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
	public ResponseEntity<Object> exception(DuplicateUsernameException exception, WebRequest request) {
		logger.error("The Username provided is already in use please select another", exception);
		logWebRequestParameters(request);
		return new ResponseEntity<>("The Username provided is already in use please select another", HttpStatus.PAYLOAD_TOO_LARGE);
	}
	
	@ExceptionHandler(value = AuthorizationException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public ResponseEntity<Object> exception(AuthorizationException exception, WebRequest request) {
		logger.error("You do not have the level of access required for this resource", exception);
		logWebRequestParameters(request);
		return new ResponseEntity<>("You do not have the level of access required for this resource", HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(RestClientException.class)
	@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
	public ResponseEntity<Object> exception(RestClientException exception, WebRequest request) {
		logger.error("RestClientException: This request required another request to be processed but the subsequent request could not completed.", exception);
		logWebRequestParameters(request);
		return new ResponseEntity<>("This request required another request to be processed but the subsequent request could not completed.", HttpStatus.UNAUTHORIZED);
	}
	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResponseEntity<Object> handleAllUncaughtException(RuntimeException exception, WebRequest request) {
		logger.error("Untracked Error for the request, More info to follow : \n{}", exception.toString());
		logWebRequestParameters(request);
		return new ResponseEntity<>("An unexpected untracked exception has occured please notify an Admin",
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public void logWebRequestParameters(WebRequest request) {
		Map<String, String[]> requestParams = request.getParameterMap();
		requestParams.forEach((key, value) -> logger.error("Request Parameter: {}, Parameter Value: {}", key, value));
	}

}
