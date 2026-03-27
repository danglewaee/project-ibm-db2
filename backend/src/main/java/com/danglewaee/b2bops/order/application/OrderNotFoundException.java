package com.danglewaee.b2bops.order.application;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderNumber) {
        super("Sales order not found: " + orderNumber);
    }
}
