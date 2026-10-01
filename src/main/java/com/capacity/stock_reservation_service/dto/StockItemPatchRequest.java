package com.capacity.stock_reservation_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockItemPatchRequest {

    private Boolean active;

    @JsonProperty("quantity_on_hand")
    private Integer quantityOnHand;

    @JsonProperty("unit_price")
    private BigDecimal unitPrice;

    @JsonProperty("warehouse_location")
    private String warehouseLocation;

    @JsonProperty("last_restocked_date")
    private LocalDate lastRestockedDate;
}
