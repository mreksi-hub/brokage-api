package com.rasit.brokage.rest.exception;

import com.rasit.brokage.utility.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exception for all custom exceptions
 * <p>
 * Contains a list of exception reasons that should be populated when preparing to throw an exception.
 * Each reason consists of the field name causing an issue, and a message describing the problem.
 * The overall message for the exception (ie. new Exception("OVERALL MESSAGE")) is used as a top level description before
 * listing the provided reasons.
 */
public class CustomException extends Exception {
    private static final long serialVersionUID = -461039161422329806L;
    private final ErrorMessageType errorMessageType;
    private final String[] variables;
    private final HttpStatus statusCode;

    public CustomException(ErrorMessageType errorMessageType, String[] variables, HttpStatus statusCode) {
        this.errorMessageType = errorMessageType;
        this.variables = variables;
        this.statusCode = statusCode;
    }

    public CustomException(ErrorMessageType errorMessageType, List<String> variables, HttpStatus statusCode) {
        this.errorMessageType = errorMessageType;
        this.variables = variables.toArray(new String[variables.size()]);
        this.statusCode = statusCode;
    }

    public String[] getVariables() {
        return variables;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public ErrorMessageType getErrorMessageType() {
        return errorMessageType;
    }
}
