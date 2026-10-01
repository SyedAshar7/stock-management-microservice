package com.capacity.stock_reservation_service.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

@Embeddable
@Data
public class PackageDimensions {
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
}
