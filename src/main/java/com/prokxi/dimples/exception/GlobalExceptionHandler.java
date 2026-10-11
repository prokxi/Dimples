package com.prokxi.dimples.exception;

import java.util.Comparator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * The single place that turns exceptions into RFC 9457 {@link ProblemDetail} responses.
 * Spring MVC's own exceptions (unreadable body, unsupported method, unknown path, …) are
 * mapped by {@link ResponseEntityExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	ProblemDetail handleNotFound(NotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	ProblemDetail handleAccessDenied(AccessDeniedException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail handleConflict(ConflictException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(TooManyRequestsException.class)
	ProblemDetail handleTooManyRequests(TooManyRequestsException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
	}

	@ExceptionHandler(InvalidFieldException.class)
	ProblemDetail handleInvalidField(InvalidFieldException ex) {
		return invalidFields(List.of(new InvalidField(ex.getField(), ex.getMessage())));
	}

	/**
	 * Spring Security's exceptions are rethrown so that its filter chain answers them
	 * with 401 or 403 instead of the catch-all below turning them into 500.
	 */
	@ExceptionHandler({ AuthenticationException.class,
			org.springframework.security.access.AccessDeniedException.class })
	void rethrowSecurityException(RuntimeException ex) {
		throw ex;
	}

	/**
	 * Hides the cause from the client: messages and stack traces may contain internals or
	 * personal data.
	 */
	@ExceptionHandler(Exception.class)
	ProblemDetail handleUnexpected(Exception ex) {
		log.error("Unexpected error", ex);
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<InvalidField> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(InvalidField::from)
			.sorted(Comparator.comparing(InvalidField::field).thenComparing(InvalidField::message))
			.toList();
		return handleExceptionInternal(ex, invalidFields(errors), headers, status, request);
	}

	private static ProblemDetail invalidFields(List<InvalidField> errors) {
		ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"The request contains invalid fields.");
		body.setProperty("errors", errors);
		return body;
	}

	/**
	 * One invalid field of a request body. The rejected value is left out on purpose, as
	 * it may be a password or other personal data.
	 */
	record InvalidField(String field, String message) {

		static InvalidField from(FieldError error) {
			return new InvalidField(error.getField(), error.getDefaultMessage());
		}

	}

}
