package com.capacity.stock_reservation_service.exception;

public class StockItemNotFoundException extends RuntimeException{

    public StockItemNotFoundException(String message){
        super(message);
    }
}
