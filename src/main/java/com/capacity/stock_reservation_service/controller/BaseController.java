package com.capacity.stock_reservation_service.controller;

import com.capacity.stock_reservation_service.dto.ApiResponse;
import com.capacity.stock_reservation_service.exception.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class BaseController {

    // 1. DTO Validation Errors (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MethodArgumentNotValidException e) {
        var errors = e.getAllErrors();

        String errorMsg = (errors != null && !errors.isEmpty())
                ? errors.get(0).getDefaultMessage()
                : "Bad Request";

        ApiResponse<Void> errorBody = new ApiResponse<>(
                false,
                errorMsg,
                null,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST).body(errorBody);
    }



}
