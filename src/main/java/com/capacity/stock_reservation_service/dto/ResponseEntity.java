package com.capacity.stock_reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseEntity<T> {

    public Boolean status;
    public String message;
    public T data;
}
