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

    @Column(name = "sku",
            nullable = false,
            unique = true,
            length = 30)
    private String sku;

    @Column(name = "product_name",
            nullable = false,
            length = 150)
    private String productName;

    @Column(name = "category",
            nullable = false,
            length = 50)
    private String category;

    @Column(name = "quantity_on_hand",
            nullable = false)
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved")
    private Integer quantityReserved = 0;

    @Column(name = "warehouse_location",
            nullable = false,
            length = 50)
    private String warehouseLocation;

    @Column(name = "unit_price",
            nullable = false,
            scale = 2)
    private BigDecimal unitPrice;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lengthCm", column = @Column(
                    name = "package_length_cm",
                    nullable = false,
                    scale = 2
            )),
            @AttributeOverride(name = "widthCm", column = @Column(
                    name = "package_width_cm",
                    nullable = false,
                    scale = 2
            )),
            @AttributeOverride(name = "heightCm", column = @Column(
                    name = "package_height_cm",
                    nullable = false,
                    scale = 2
            ))
    })
    private PackageDimensions packageDimensions;

    @Column(name = "last_restocked_date")
    private LocalDate lastRestockedDate;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
