package com.capacity.stock_reservation_service.service;

import com.capacity.stock_reservation_service.dto.CreateStockItemRequest;
import com.capacity.stock_reservation_service.dto.ReserveStockRequest;
import com.capacity.stock_reservation_service.dto.StockItemPatchRequest;
import com.capacity.stock_reservation_service.dto.StockItemResponse;
import com.capacity.stock_reservation_service.embeddable.PackageDimensions;
import com.capacity.stock_reservation_service.exception.DuplicateSkuException;
import com.capacity.stock_reservation_service.exception.InsufficientStockException;
import com.capacity.stock_reservation_service.exception.StockItemNotFoundException;
import com.capacity.stock_reservation_service.model.StockItem;
import com.capacity.stock_reservation_service.repository.StockItemRepository;
import com.capacity.stock_reservation_service.specification.StockItemSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockItemServiceImpl implements StockItemService {

    private final StockItemRepository repository;

    @Override
    public StockItemResponse create(CreateStockItemRequest request){

        String requestSku = request.getSku();

        if (repository.existsBySku(requestSku)) {
            throw new DuplicateSkuException(
                    "Cannot create stock, " + requestSku + "sku already exists"
            );
        }

        StockItem stockItem = createRequestToEntity(request);
        StockItem savedStockItem = repository.save(stockItem);

        return toResponse(savedStockItem);
    }

    @Override
    public StockItemResponse getBySku(String sku){
        StockItem stockItem = repository.findBySku(sku);

        if (stockItem == null) {
            throw new StockItemNotFoundException(
                    "Stock item with SKU '" + sku + "' was not found"
            );
        }
        return toResponse(stockItem);
    }

    @Override
    public List<StockItemResponse> search(String category,
                                          String warehouseLocation,
                                          Boolean active,
                                          BigDecimal minPrice,
                                          BigDecimal maxPrice,
                                          Integer minQuantityAvailable) {

        Specification<StockItem> spec = (root, query, cb) ->
                cb.conjunction();

        if (category != null){
            spec = spec.and(
                    StockItemSpecification.hasCategory(
                            category
                    ));
        }

        if (warehouseLocation != null){
            spec = spec.and(
                    StockItemSpecification.hasWarehouseLocation(
                            warehouseLocation
                    ));
        }

        if (active != null){
            spec = spec.and(
                    StockItemSpecification.isActive(
                            active
                    ));
        }

        if (minPrice != null || maxPrice != null){
            spec = spec.and(
                    StockItemSpecification.priceBetween(
                            minPrice
                            , maxPrice));
        }

        if (minQuantityAvailable != null) {
            spec = spec.and(
                    StockItemSpecification.hasMinimumQuantityAvailable(
                            minQuantityAvailable
                    )
            );
        }

        List<StockItem> result = repository.findAll(spec);

        return result
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public StockItemResponse reserve(
            String sku,
            ReserveStockRequest request) {

        StockItem stockItem = repository.findBySku(sku);

        if (stockItem == null) {
            throw new StockItemNotFoundException(
                    "Stock item with SKU '" + sku + "' was not found"
            );
        }

        int quantity = request.getQuantity();
        int availableQuantity =
                        stockItem.getQuantityOnHand()
                                - stockItem.getQuantityReserved();

        if ( quantity > availableQuantity){
            throw new InsufficientStockException(
                    "Quantity not available"
            );
        }

        stockItem.setQuantityReserved(
                stockItem.getQuantityReserved() + quantity
        );

        StockItem updateStockItem = repository.save(stockItem);
        return toResponse(updateStockItem);
    }

    @Override
    public StockItemResponse patch(String sku,
                                   StockItemPatchRequest request) {

        StockItem stockItem = repository.findBySku(sku);

        if (stockItem == null) {
            throw new StockItemNotFoundException(
                    "Stock item with SKU '" + sku + "' was not found"
            );
        }

        if (request.getQuantityOnHand() != null){
            stockItem.setQuantityOnHand(request.getQuantityOnHand());
            stockItem.setLastRestockedDate(LocalDate.now());
        }

        if (request.getUnitPrice() != null){
            stockItem.setUnitPrice(request.getUnitPrice());
        }

        if (request.getWarehouseLocation() != null){
            stockItem.setWarehouseLocation(request.getWarehouseLocation());
        }

        StockItem patchedStockItem = repository.save(stockItem);

        return toResponse(patchedStockItem);
    }

    // ============================================================
    // CREATE REQUEST DTO → ENTITY
    // ============================================================

    private StockItem createRequestToEntity(CreateStockItemRequest createStockItemRequest){
        // Step 1: Initialize
        StockItem stockItem = new StockItem();

        // Step 2: Set Fields
        stockItem.setActive(true);
        stockItem.setCategory(createStockItemRequest.getCategory());
        stockItem.setSku(createStockItemRequest.getSku());
        stockItem.setProductName(createStockItemRequest.getProductName());
        stockItem.setQuantityOnHand(createStockItemRequest.getQuantityOnHand());
        stockItem.setWarehouseLocation(createStockItemRequest.getWarehouseLocation());
        stockItem.setCategory(createStockItemRequest.getCategory());
        stockItem.setUnitPrice(createStockItemRequest.getUnitPrice());

        // Step 3: Handle Data with format difference
        PackageDimensions packageDimensions = new PackageDimensions();
        packageDimensions.setHeightCm(createStockItemRequest.getPackageHeightCm());
        packageDimensions.setLengthCm(createStockItemRequest.getPackageLengthCm());
        packageDimensions.setWidthCm(createStockItemRequest.getPackageWidthCm());
        stockItem.setPackageDimensions(packageDimensions);

        // Step 4: Return Output
        return stockItem;
    }

    // ============================================================
    // ENTITY → RESPONSE DTO
    // ============================================================

    private StockItemResponse toResponse(StockItem stockItem){
        // Initialize
        StockItemResponse response =  new StockItemResponse();

        // Basic Fields Mapping
        response.setActive(stockItem.getActive());
        response.setCategory(stockItem.getCategory());
        response.setSku(stockItem.getSku());
        response.setProductName(stockItem.getProductName());
        response.setWarehouseLocation(stockItem.getWarehouseLocation());

        // Handle Quantities
        Integer onHand = stockItem.getQuantityOnHand();
        Integer reserved = stockItem.getQuantityReserved();
        Integer available = onHand - reserved;

        response.setQuantityOnHand(onHand);
        response.setQuantityReserved(reserved);
        response.setQuantityAvailable(available);

        // Handle Nested Structures
        PackageDimensions dimensions = stockItem.getPackageDimensions();
        response.setPackageHeight(dimensions.getHeightCm());
        response.setPackageLength(dimensions.getLengthCm());
        response.setPackageWidth(dimensions.getWidthCm());

        // Return output
        return response;
    }
}
