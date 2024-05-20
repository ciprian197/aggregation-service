package com.example.aggregationservice.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

	INTERNAL_SERVER_ERROR(100, "Internal server error!"),
	BAD_REQUEST(101,"Invalid request. Please check request data!");

	private int code;
	private final String message;

}
