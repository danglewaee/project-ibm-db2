package com.danglewaee.b2bops.inventory.domain;

public enum StockMovementType {
    RECEIPT,
    RESERVE,
    RELEASE,
    SHIP,
    COUNT_ADJUST_INCREASE,
    COUNT_ADJUST_DECREASE,
    MANUAL_ADJUSTMENT
}
