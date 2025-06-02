package com.rasit.brokage.utility;

/**
 * Represents an RoleType. Used e.g. to specify the RoleType for customer.
 */
public enum RoleType {
    ADMIN("admin"),
    CUSTOMER("customer");

    public String getValue() {
        return value;
    }

    private final String value;

    private RoleType(String value) {
        this.value = value;
    }


    public static RoleType findByValue(String value) {
        RoleType result = null;
        for (RoleType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                result = type;
                break;
            }
        }
        return result;
    }
}
