package com.capacity.stock_reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ResponseEntity<T> {

    public int status;
    public String message;
    public T data;
}
