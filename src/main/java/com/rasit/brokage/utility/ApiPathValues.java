package com.rasit.brokage.utility;

/**
 * Set of path and header constants for Brokage api calls
 */
public class ApiPathValues {
    /**
     * The part of the request URL after the address and before the call specifics.
     */
    public static final String BASE_V1 = "/v1";

    /**
     * order as path key
     */
    public static final String ORDER_ENDPOINT = "/order";

    /**
     * asset as path key
     */
    public static final String ASSET_ENDPOINT = "/asset";

    /**
     * orderId as path key
     */
    public static final String ORDERID = "{orderId}";

    /**
     * auth as path key
     */
    public static final String AUTH_ENDPOINT = "/auth";

    /**
     * match as path key
     */
    public static final String MATCH_ENDPOINT = "/match";

    /**
     * list as path key
     */
    public static final String LIST_ENDPOINT = "/list";

    /**
     * login as path key
     */
    public static final String LOGIN = "/login";

    /**
     * reload as path key
     */
    public static final String RELOAD = "/reload";

    private ApiPathValues() {
        throw new IllegalStateException("Utility class");
    }

}
