package com.capacity.stock_reservation_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateStockItemRequest {

    private String sku;
    private String productName;
    private String category;
    private Integer quantityOnHand;
    private String warehouseLocation;
    private BigDecimal unitPrice;
    private BigDecimal packageLengthCm;
    private BigDecimal packageWidthCm;
    private BigDecimal packageHeightCm;
}
