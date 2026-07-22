package com.helpdesk.controller.util;

import com.helpdesk.protoGen.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        val resolvedStatus = HttpStatus.resolve(exception.getStatusCode().value());
        val status = resolvedStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolvedStatus;
        return error(status, exception.getReason(), request);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class
    })
    ResponseEntity<ApiError> handleBadRequest(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NoResourceFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error while handling {} {}", request.getMethod(), request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        val body = ApiError.newBuilder()
                .setStatus(status.value())
                .setError(status.getReasonPhrase())
                .setMessage(message == null || message.isBlank() ? status.getReasonPhrase() : message)
                .setPath(request.getRequestURI())
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        return ResponseEntity.status(status)
                .contentType(errorMediaType(request))
                .body(body);
    }

    private MediaType errorMediaType(HttpServletRequest request) {
        val accept = request.getHeader("Accept");
        val protobuf = MediaType.parseMediaType("application/x-protobuf");
        val acceptedTypes = accept == null ? java.util.List.<MediaType>of() : MediaType.parseMediaTypes(accept);
        val acceptsOnlyProtobuf = !acceptedTypes.isEmpty()
                && acceptedTypes.stream().allMatch(protobuf::isCompatibleWith);

        return acceptsOnlyProtobuf ? protobuf : MediaType.APPLICATION_JSON;
    }
}
