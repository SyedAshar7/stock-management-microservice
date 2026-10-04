package com.capacity.stock_reservation_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReserveStockRequest {

    @NotNull(message = "Quantity to reserve is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
