package com.capacity.stock_reservation_service.service;

import com.capacity.stock_reservation_service.dto.CreateStockItemRequest;
import com.capacity.stock_reservation_service.dto.ReserveStockRequest;
import com.capacity.stock_reservation_service.dto.StockItemPatchRequest;
import com.capacity.stock_reservation_service.dto.StockItemResponse;

import java.math.BigDecimal;
import java.util.List;

public interface StockItemService {

    StockItemResponse create(
            CreateStockItemRequest createStockItemRequest);

    StockItemResponse getBySku(String sku);

    List<StockItemResponse> search(String category,
                                   String warehouseLocation,
                                   Boolean active,
                                   BigDecimal minPrice,
                                   BigDecimal maxPrice,
                                   Integer minQuantityAvailable);

    StockItemResponse reserve(String sku, ReserveStockRequest request);

    StockItemResponse patch(String sku, StockItemPatchRequest request);
}
