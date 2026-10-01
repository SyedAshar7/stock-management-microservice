package com.capacity.stock_reservation_service.repository;

import com.capacity.stock_reservation_service.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface StockItemRepository extends JpaRepository<StockItem, Long>, JpaSpecificationExecutor<StockItem> {
    StockItem findBySku(String sku);
}
