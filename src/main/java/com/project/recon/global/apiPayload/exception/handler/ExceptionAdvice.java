package com.project.recon.global.apiPayload.exception.handler;

import com.project.recon.global.apiPayload.code.BaseErrorCode;
import com.project.recon.global.apiPayload.code.GeneralErrorCode;
import com.project.recon.global.apiPayload.exception.GeneralException;
import com.project.recon.global.apiPayload.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(GeneralException e) {
        BaseErrorCode code = e.getErrorCode();

        log.warn("CustomException: {}", e.getErrorCode().getMessage());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fld -> String.format("[%s] %s", fld.getField(), fld.getDefaultMessage()))
                .toList();

        log.warn("Validation Exception: {}", errors);

        BaseErrorCode code = GeneralErrorCode.INVALID_PARAMETER;

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = String.format("%s 필드의 타입이 잘못되었습니다.", e.getName());

        log.warn("TypeMismatchException: {}", e.getMessage());

        BaseErrorCode code = GeneralErrorCode.INVALID_PARAMETER;

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        BaseErrorCode code = GeneralErrorCode.INVALID_BODY_TYPE;

        log.warn("HttpMessageNotReadableException: {}", e.getMessage());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, code.getMessage()));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponse<Object>> handlePropertyReferenceException(PropertyReferenceException e) {
        BaseErrorCode code = GeneralErrorCode.INVALID_PARAMETER;

        log.warn("PropertyReferenceException: {}", e.getMessage());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, code.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        BaseErrorCode code = GeneralErrorCode.INTERNAL_SERVER_ERROR;

        log.warn("Exception: {}", e.getMessage());

        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.onFailure(code, code.getMessage()));
    }
}
