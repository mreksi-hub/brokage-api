package com.rasit.brokage.utility;

/**
 * Represents an SideType. Used e.g. to specify the side type for order.
 */
public enum SideType {
    BUY("buy"),
    SELL("sell");

    public String getValue() {
        return value;
    }

    private final String value;

    private SideType(String value) {
        this.value = value;
    }


    public static SideType findByValue(String value) {
        SideType result = null;
        for (SideType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                result = type;
                break;
            }
        }
        return result;
    }
}
