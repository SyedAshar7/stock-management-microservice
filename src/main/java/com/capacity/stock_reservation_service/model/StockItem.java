package com.capacity.stock_reservation_service.model;

import com.capacity.stock_reservation_service.embeddable.PackageDimensions;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "stock_item")
public class StockItem{

    @Id
    @Column(name="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku")
    private String sku;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "category")
    private String category;

    @Column(name = "quantity_on_hand")
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved")
    private Integer quantityReserved = 0;

    @Column(name = "warehouse_location")
    private String warehouseLocation;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lengthCm", column = @Column(name = "package_length_cm")),
            @AttributeOverride(name = "widthCm", column = @Column(name = "package_width_cm")),
            @AttributeOverride(name = "heightCm", column = @Column(name = "package_height_cm"))
    })
    private PackageDimensions packageDimensions;

    @Column(name = "last_restocked_date")
    private LocalDate lastRestockedDate;

    @Column(name = "active")
    private Boolean active;
}
