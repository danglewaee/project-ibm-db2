package com.danglewaee.b2bops.inventory.application;

public class StockCountSessionNotFoundException extends RuntimeException {

    public StockCountSessionNotFoundException(String countNumber) {
        super("Stock count session not found: " + countNumber);
    }
}
