package com.capacity.stock_reservation_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockItemResponse {

    private String category;
    private Boolean active;
    private String sku;

    @JsonProperty("name")
    private String productName;

    @JsonProperty("warehouse_name")
    private String warehouseLocation;

    @JsonProperty("quantity_on_hand")
    private Integer quantityOnHand;

    @JsonProperty("quantity_reserved")
    private Integer quantityReserved;

    @JsonProperty("quantity_available")
    private Integer quantityAvailable;

    @JsonProperty("price")
    private BigDecimal unitPrice;

    @JsonProperty("package_length")
    private BigDecimal packageLength;

    @JsonProperty("package_width")
    private BigDecimal packageWidth;

    @JsonProperty("package_height")
    private BigDecimal packageHeight;

    @JsonProperty("last_restocked_date")
    private LocalDate lastRestockedDate;
}
