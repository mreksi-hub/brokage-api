package com.rasit.brokage.utility;

public enum ErrorMessageType {
    GENERIC_SERVICE_ERROR("SVC1101", "The following service error occurred: %1. Error code is %2"),

    VIOLATION_ERROR("SVC1102", "The following violation error(s) occurred: %1"),

    ASSET_NOT_FOUND("SVC1103", "Asset not found with identifier: %1"),

    ASSET_USABLE_SIZE_NOT_ENOUGH("SVC1104", "%1 asset usable size: %2 is not enough."),

    ORDER_NOT_FOUND("SVC1105", "Order not found with identifier: %1"),

    REFRESH_TOKEN_ERROR("SVC1106", "Refresh token is invalid!"),

    INVALID_CREDENTIAL_ERROR("SVC1107", "Invalid username or password!"),

    X_CUSTOMER_HEADER_NOT_FOUND("SVC1108", "X-Customer-Header is required for ADMIN users.");

    private final String messageId;
    private final String text;

    ErrorMessageType(final String messageId, final String text) {
        this.messageId = messageId;
        this.text = text;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }
}
