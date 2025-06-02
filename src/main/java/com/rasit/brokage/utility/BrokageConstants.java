package com.rasit.brokage.utility;

public class BrokageConstants {

    public static final String TRY_ASSET_NAME = "TRY";
    
    public static final String ORDER_SIDE_VIOLATION = "OrderSide can only be SELL or BUY.";
    public static final String ASSET_NAME_BLANK_VIOLATION = "The asset name must not be blank.";
    public static final String ASSET_NAME_SIZE_VIOLATION = "The asset name must be between 1 and 128 characters.";
    public static final String ORDER_SIZE_MIN_SIZE_VIOLATION = "The order size must not be less than 1.";
    public static final String ORDER_SIZE_MAX_SIZE_VIOLATION = "The order size must not be greater than 1000000.00.";
    public static final String ORDER_SIZE_NULL_VIOLATION = "The order size must not be blank.";
    public static final String ORDER_PRICE_MIN_VIOLATION = "The order price must not be less than 0.01.";
    public static final String ORDER_PRICE_MAX_VIOLATION = "The order price must not be greater than 1000000.00.";
    public static final String ORDER_PRICE_NULL_VIOLATION = "The order price must not be blank.";
    public static final String ORDER_SIDE_NULL_VIOLATION = "The order side must not be blank.";
    public static final String PAGE_NUMBER_MIN_SIZE_VIOLATION = "page_number must not be less than 0";
    public static final String PAGE_NUMBER_MAX_SIZE_VIOLATION = "page_number must be less than or equal to 500";
    public static final String PAGE_SIZE_MIN_SIZE_VIOLATION = "page_size must not be less than 1";
    public static final String PAGE_SIZE_MAX_SIZE_VIOLATION = "page_size must be less than or equal to 500";

    // Rest Request Params
    public static final String PAGE_NUMBER = "page_number";
    public static final String PAGE_SIZE = "page_size";

    // Rest Headers
    public static final String X_CUSTOMER_ID = "x-customer-id";
    public static final String AUTHORIZATION_HEADER = "authorization";
    public static final String BEARER_TOKEN_PREFIX = "Bearer";
}
