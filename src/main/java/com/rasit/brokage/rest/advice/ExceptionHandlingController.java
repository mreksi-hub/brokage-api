package com.rasit.brokage.rest.advice;

import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.RestErrorResponseModel;
import com.rasit.brokage.rest.resource.SubErrorResponseModel;
import com.rasit.brokage.utility.ErrorMessageType;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rasit.brokage.utility.ErrorMessageType.VIOLATION_ERROR;

@Slf4j
@ControllerAdvice
public class ExceptionHandlingController {

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleNullPointer(final NullPointerException ex) {
        log.error("NullPointerException was uncaught by application: ", ex);
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleInvalidEnum(final HttpMessageConversionException ex) {
        log.error("HttpMessageConversionException was uncaught by application: ", ex);
        String description = ExceptionUtils.getRootCauseMessage(ex);
        if (ExceptionUtils.getRootCause(ex) instanceof IllegalArgumentException) {
            description = ExceptionUtils.getRootCauseMessage(ex).replaceAll(".*?Exception: ", "");
        }
        RestErrorResponseModel response = new RestErrorResponseModel(description);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleIllegalArgument(final IllegalArgumentException ex) {
        log.error("IllegalArgumentException was uncaught by application: ", ex);
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<RestErrorResponseModel> handleEntityNotFound(final EntityNotFoundException ex) {
        log.error("EntityNotFoundException was uncaught by application: ", ex);
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<RestErrorResponseModel> handleDefaultException(final Exception ex) {
        log.error("General exception was uncaught by application; returning 500. Ex: {}, {}", ex, ExceptionUtils.getStackTrace(ex));
        final RestErrorResponseModel response = new RestErrorResponseModel(ErrorMessageType.GENERIC_SERVICE_ERROR.getMessageId(), ErrorMessageType.GENERIC_SERVICE_ERROR.getText(), List.of(new SubErrorResponseModel(HttpStatus.INTERNAL_SERVER_ERROR.toString())));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleInvalidParameterException(final InvalidParameterException ex) {
        log.error("InvalidParameterException was uncaught by application: ", ex);
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleConstraintViolationException(final ConstraintViolationException ex) {
        log.error("ConstraintViolationException was uncaught by application: ", ex);
        List<SubErrorResponseModel> listSubErrorResources = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String message = violation.getMessage();
            if (listSubErrorResources.stream().noneMatch(o -> o.getSubText().equals(message)))
                listSubErrorResources.add(new SubErrorResponseModel(message));
        }
        RestErrorResponseModel response = new RestErrorResponseModel(VIOLATION_ERROR.getMessageId(), VIOLATION_ERROR.getText(), listSubErrorResources);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseStatus()
    public ResponseEntity<RestErrorResponseModel> handleCustomException(final CustomException ex) {
        log.error("CustomException was uncaught by application: ", ex);
        HttpStatus statusCode = ex.getStatusCode();
        String messageId = ex.getErrorMessageType().getMessageId();
        String text = ex.getErrorMessageType().getText();
        List<SubErrorResponseModel> listSubErrorResponseModels = new ArrayList<>();
        Arrays.stream(ex.getVariables()).forEach(variables -> listSubErrorResponseModels.add(new SubErrorResponseModel(variables)));
        final RestErrorResponseModel response = new RestErrorResponseModel(messageId, text, listSubErrorResponseModels);
        return ResponseEntity.status(statusCode).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException was uncaught by application: ", ex);
        List<SubErrorResponseModel> listSubErrorResources = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            String message = fieldError.getField() + ":" + fieldError.getDefaultMessage();
            if (listSubErrorResources.stream().noneMatch(o -> o.getSubText().equals(message)))
                listSubErrorResources.add(new SubErrorResponseModel(message));
        }
        RestErrorResponseModel response = new RestErrorResponseModel(VIOLATION_ERROR.getMessageId(), VIOLATION_ERROR.getText(), listSubErrorResources);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<RestErrorResponseModel> handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException was uncaught by application: ", ex);
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<RestErrorResponseModel> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<RestErrorResponseModel> handleAccessDenied(AccessDeniedException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<RestErrorResponseModel> handleAuthentication(AuthenticationException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<RestErrorResponseModel> handleBadCredentials(BadCredentialsException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel("Invalid credentials.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<RestErrorResponseModel> handleDisabled(DisabledException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel("User account is disabled.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<RestErrorResponseModel> handleInsufficientAuth(InsufficientAuthenticationException ex) {
        RestErrorResponseModel response = new RestErrorResponseModel("Insufficient authentication.");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

}
