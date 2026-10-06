package com.capacity.stock_reservation_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateStockItemRequest {

    @NotEmpty(message = "SKU is required")
    private String sku;

    @NotEmpty(message = "Product name is required")
    private String productName;

    @NotEmpty(message = "Category is required")
    private String category;

    @NotNull(message = "Active status is required")
    private boolean active;

    @NotNull(message = "Quantity in hand is required")
    @PositiveOrZero(message = "Quantity in hand must be positive or zero")
    private Integer quantityOnHand;

    @NotEmpty(message = "Warehouse location is required")
    private String warehouseLocation;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "Package length is required")
    @Positive(message = "Package length must be positive")
    private BigDecimal packageLengthCm;

    @NotNull(message = "Package width is required")
    @Positive(message = "Package width must be positive")
    private BigDecimal packageWidthCm;

    @NotNull(message = "Package height is required")
    @Positive(message = "Package height must be positive")
    private BigDecimal packageHeightCm;
}
