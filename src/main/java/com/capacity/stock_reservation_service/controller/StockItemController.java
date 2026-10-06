package com.capacity.stock_reservation_service.controller;

import com.capacity.stock_reservation_service.dto.*;
import com.capacity.stock_reservation_service.service.StockItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-item")
public class StockItemController {

    private final StockItemService stockItemService;

    @PostMapping
    public ApiResponse<StockItemResponse> create(@RequestBody @Valid CreateStockItemRequest request){
        StockItemResponse response = stockItemService.create(request);
        return new ApiResponse<>(true, "Added Stock Item", response, null);
    }

    @GetMapping("/{sku}")
    public ApiResponse<StockItemResponse> getBySku(@PathVariable String sku){
        StockItemResponse data = stockItemService.getBySku(sku);
        return new ApiResponse<>(true, "Fetched Stock Item", data, null);
    }

    @GetMapping
    public ApiResponse<List<StockItemResponse>> search(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String warehouseLocation,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minQuantityAvailable
            ){
        List<StockItemResponse> data = stockItemService.search(
                category,
                warehouseLocation,
                active,
                minPrice,
                maxPrice,
                minQuantityAvailable
        );

        return new ApiResponse<>(
                true,
                "Search completed",
                data,
                null
        );
    }

    @PutMapping("/{sku}/reserve")
    public ApiResponse<StockItemResponse> reserve(
            @PathVariable String sku,
            @RequestBody ReserveStockRequest request){
        StockItemResponse data = stockItemService.reserve(sku, request);
        return new ApiResponse<>(
                true,
                "Reserved Stock",
                data,
                null
        );
    }

    @PatchMapping("/{sku}")
    public ApiResponse<StockItemResponse> update(
            @PathVariable String sku,
            @RequestBody StockItemPatchRequest request
    ){
        StockItemResponse data = stockItemService.patch(sku, request);

        return new ApiResponse<>(
                true,
                "Updated stock",
                data,
                null
        );
    }
}
