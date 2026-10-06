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

    public static Specification<StockItem> priceGreaterThanOrEqualTo (BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.<BigDecimal>get("unitPrice"), minPrice);
    }

    public static Specification<StockItem> priceLessThanOrEqualTo (BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.<BigDecimal>get("unitPrice"), maxPrice);
    }

    public static Specification<StockItem> hasMinimumQuantityAvailable(int quantity) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(
                        cb.diff(root.get("quantityOnHand"),
                                root.get("quantityReserved")
                        ),
                        quantity);
    }
}