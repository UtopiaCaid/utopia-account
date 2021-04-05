package com.caid.utopia.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(
	value = HttpStatus.PAYLOAD_TOO_LARGE,
	reason = "Entity field value was too large to be stored in the system."
)
public class OversizedValueException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2290939726516481006L;
	
	
	

}
