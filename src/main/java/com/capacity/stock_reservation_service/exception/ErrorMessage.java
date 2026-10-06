package com.capacity.stock_reservation_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrorMessage {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
