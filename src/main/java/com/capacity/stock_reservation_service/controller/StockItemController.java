package com.capacity.stock_reservation_service.controller;

import com.capacity.stock_reservation_service.dto.CreateStockItemRequest;
import com.capacity.stock_reservation_service.dto.ReserveStockRequest;
import com.capacity.stock_reservation_service.dto.StockItemPatchRequest;
import com.capacity.stock_reservation_service.dto.StockItemResponse;
import com.capacity.stock_reservation_service.service.StockItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-item")
public class StockItemController {

    private final StockItemService stockItemService;

    @PostMapping
    public StockItemResponse create(@RequestBody CreateStockItemRequest request){
        return stockItemService.create(request);
    }

    @GetMapping("/{sku}")
    public StockItemResponse getBySku(@PathVariable String sku){
        return stockItemService.getBySku(sku);
    }

    @GetMapping
    public List<StockItemResponse> search(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String warehouseLocation,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minQuantityAvailable
            ){
        return stockItemService.search(
                category,
                warehouseLocation,
                active,
                minPrice,
                maxPrice,
                minQuantityAvailable
        );
    }

    @PutMapping("/{sku}/reserve")
    public StockItemResponse reserve(
            @PathVariable String sku,
            @RequestBody ReserveStockRequest request){
        return stockItemService.reserve(sku, request);
    }

    @PatchMapping("/{sku}")
    public StockItemResponse update(
            @PathVariable String sku,
            @RequestBody StockItemPatchRequest request
    ){
        return stockItemService.patch(sku, request);
    }
}
