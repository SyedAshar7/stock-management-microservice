package com.capacity.stock_reservation_service.controller;

import com.capacity.stock_reservation_service.dto.ApiResponse;
import com.capacity.stock_reservation_service.exception.DuplicateSkuException;
import com.capacity.stock_reservation_service.exception.StockItemNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class BaseController {

    // 1. DTO Validation Errors (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleInvalidMethodException (MethodArgumentNotValidException e) {
        var errors = e.getAllErrors();

        String errorMsg = !errors.isEmpty()
                ? errors.get(0).getDefaultMessage()
                : "Bad Request";

        return new ApiResponse<>(
                false,
                errorMsg,
                null,
                LocalDateTime.now()
        );
    }

    // 2. To handle creation of a stock that already exists (HTTP 409)
    @ExceptionHandler(DuplicateSkuException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicateException(DuplicateSkuException e) {

        return new ApiResponse<>(
          false,
          e.getMessage(),
          null,
          LocalDateTime.now()
        );
    }

    // 3. To handle exception when stock item already exists (HTTP 404)
    @ExceptionHandler(StockItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFoundException(StockItemNotFoundException e) {

        return new ApiResponse<>(
                false,
                e.getMessage(),
                null,
                LocalDateTime.now()
        );
    }

    // 4. Catch-All for Unexpected Server Errors (HTTP 500)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception e) {
        return new ApiResponse<>(
                false,
                "An unexpected error occurred: " + e.getMessage(),
                null,
                LocalDateTime.now()
        );
    }

}
