package com.capacity.stock_reservation_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    public int status;
    public String message;
    public T data;
}
