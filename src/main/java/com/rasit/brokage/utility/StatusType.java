package com.rasit.brokage.utility;

/**
 * Represents an StatusType. Used e.g. to specify the status type for order.
 */
public enum StatusType {
    PENDING("pending"),
    MATCHED("matched"),
    CANCELED("canceled");

    private String value;

    private StatusType(String value) {
        this.value = value;
    }

    public String getName() {
        return value;
    }
}
