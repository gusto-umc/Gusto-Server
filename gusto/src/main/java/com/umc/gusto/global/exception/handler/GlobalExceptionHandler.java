package com.umc.gusto.global.exception.handler;

import com.umc.gusto.global.exception.Code;
import com.umc.gusto.global.exception.ExceptionResponse;
import com.umc.gusto.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestControllerAdvice(annotations = {RestController.class})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 중 에러 발생"));

        return handleExceptionInternalFalse(e, Code.INVALID_REQUEST, request, errorMessage);
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e,Code.INVALID_REQUEST,request,errors);
    }


    @org.springframework.web.bind.annotation.ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        e.printStackTrace();
        return handleExceptionInternalFalse(e, Code.INTERNAL_SEVER_ERROR, request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> customException(GeneralException generalException, HttpServletRequest request) {
        Code errorCode = generalException.getCode();
        return handleExceptionInternal(generalException,errorCode,null, request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, Code errorCode, HttpHeaders httpHeader,HttpServletRequest request) {

        ExceptionResponse<Object> body = ExceptionResponse.from(errorCode, null);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(e, body, httpHeader, errorCode.getHttpStatus(), webRequest);
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, Code errorCode, WebRequest request, String errorPoint) {
        ExceptionResponse<Object> body = ExceptionResponse.from(errorCode, errorPoint);
        return super.handleExceptionInternal(e, body, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request);
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, Code errorCode, WebRequest request, Map<String, String> errorArgs) {
        ExceptionResponse<Object> body = ExceptionResponse.from(errorCode, errorArgs);
        return super.handleExceptionInternal(e, body, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request);
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, Code errorCode, WebRequest request) {
        ExceptionResponse<Object> body = ExceptionResponse.from(errorCode, null);
        return super.handleExceptionInternal(e, body, HttpHeaders.EMPTY, errorCode.getHttpStatus(), request);
    }
}
