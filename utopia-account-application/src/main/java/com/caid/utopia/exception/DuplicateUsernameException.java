package com.caid.utopia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.UNPROCESSABLE_ENTITY,
	reason = "The Account Username already exists, please choose another"
)
public class DuplicateUsernameException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6261934336972694209L;

}
