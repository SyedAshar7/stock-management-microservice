package com.capacity.stock_reservation_service.specification;

import com.capacity.stock_reservation_service.model.StockItem;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class StockItemSpecification {

    // Specifications for equals
    public static Specification<StockItem> hasCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    };

    public static Specification<StockItem> hasWarehouseLocation(String warehouseLocation) {
        return (root, query, cb) -> cb.equal(root.get("warehouseLocation"), warehouseLocation);
    }

    public static Specification<StockItem> isActive(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get("active"), active);
    }

    public static Specification<StockItem> priceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> cb.between(root.<BigDecimal>get("unitPrice"), minPrice, maxPrice);
    }

    // TO DO : Fix the filter to fetch this correctly
//    public static Specification<StockItem> hasMinimumQuantityAvailable (int quantity) {
//
//        int available quantity
//        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
//                root.get("quantity_on_hand") - root.get("reservedQuantity"), quantity);
//    }
}