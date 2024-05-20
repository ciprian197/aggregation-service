package com.example.aggregationservice.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ErrorResponse handleGeneralException(final Exception exception) {
		log.error("Unknown exception occurred ", exception);
		return new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ErrorResponse handleWebExchangeBindException(final WebExchangeBindException exception) {
		log.error("WebExchangeBindException exception occurred ", exception);
		return new ErrorResponse(ErrorCode.BAD_REQUEST);
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class ErrorResponse {

		private int code;
		private String message;
		public ErrorResponse(final ErrorCode errorCode) {
			this.code = errorCode.getCode();
			this.message = errorCode.getMessage();
		}
	}

}
