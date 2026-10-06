package com.capacity.stock_reservation_service.controller;

import com.capacity.stock_reservation_service.dto.ApiResponse;
import com.capacity.stock_reservation_service.exception.ErrorMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.EntityResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class BaseController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handle(MethodArgumentNotValidException e) {
        var errors = e.getAllErrors();

        String errorMsg = (errors != null && !errors.isEmpty())
                ? errors.get(0).getDefaultMessage()
                : "Bad Request";

        ApiResponse<ErrorMessage> errorBody = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                errorMsg,
                new ErrorMessage(
                        400,
                        "Bad Request",
                        LocalDateTime.now()
                )
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorBody);
    }
}
