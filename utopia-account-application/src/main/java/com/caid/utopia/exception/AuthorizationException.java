package com.caid.utopia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
		value = HttpStatus.UNAUTHORIZED,
		reason = "You do not have the required level of access to view this resource"
	)
public class AuthorizationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9165066309635666172L;

}
