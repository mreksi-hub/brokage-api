package com.rasit.brokage.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.RestErrorResponseModel;
import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.resource.order.OrderRestResponseModel;
import com.rasit.brokage.rest.resource.order.OrdersRestResponseListModel;
import com.rasit.brokage.rest.validation.AssetValidator;
import com.rasit.brokage.rest.validation.OrderValidator;
import com.rasit.brokage.service.OrderService;
import com.rasit.brokage.rest.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

import static com.rasit.brokage.utility.ApiPathValues.*;
import static com.rasit.brokage.utility.BrokageUtil.*;
import static com.rasit.brokage.utility.BrokageConstants.*;


@SecurityRequirement(name = "BearerAuth")
@SecurityScheme(name = "BearerAuth", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER, scheme = "bearer")
@Validated
@RestController
@RequestMapping(BASE_V1 + ORDER_ENDPOINT)
@Slf4j
public class OrderController {
    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private final OrderService orderService;
    private final AssetValidator assetValidator;
    private final OrderValidator orderValidator;

    @Autowired
    public OrderController(OrderService orderService, AssetValidator assetValidator, OrderValidator orderValidator) {
        this.orderService = orderService;
        this.assetValidator = assetValidator;
        this.orderValidator = orderValidator;
    }

    @Operation(summary = "Make requests to create a order.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Order is created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderRestResponseModel.class), examples = {@ExampleObject(value = "")})}),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. Insufficient permissions.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "Asset not found.", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))})})
    @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class)))
    @PostMapping
    public ResponseEntity<OrderRestResponseModel> createOrder(@AuthenticationPrincipal UserDetailsImpl userDetails, @Parameter(description = "Its allows Admin user to create order for customer.", example = "39aeef68-f97b-4c05-8385-cfe4c0f49b5b") @RequestHeader(value = X_CUSTOMER_ID, required = false) final String customerIdHeader, @Valid @RequestBody OrderRestRequestModel orderRequest) throws JsonProcessingException, CustomException {
        log.debug("Request received for CREATE new order is {}", objectMapper.writeValueAsString(orderRequest));

        String targetCustomerId = resolveCustomerId(userDetails, customerIdHeader);
        orderRequest.setCustomerId(targetCustomerId);
        assetValidator.validateCustomerAssetExists(orderRequest.getCustomerId(), orderRequest.getAssetName());
        return orderService.createOrder(orderRequest);
    }

    @Operation(summary = "Make requests to get all orders by date range.", description = "Allows for query parameters to paginate the results.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Orders are retrieved.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrdersRestResponseListModel.class))})})
    @GetMapping(path = LIST_ENDPOINT)
    public ResponseEntity<OrdersRestResponseListModel> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails, @Parameter(description = "Its allows Admin user to list customer’s orders.", example = "39aeef68-f97b-4c05-8385-cfe4c0f49b5b") @RequestHeader(value = X_CUSTOMER_ID, required = false) final String customerIdHeader,
                                                                 @Parameter(description = "The date the order was created after (ISO 8601 format with timezone, e.g., '2025-06-01T08:24:15+0300'). " +
                                                                         "If no timezone is provided, server's default timezone will be used.", example = "2025-06-01T08:24:15+0300") @RequestParam(name = "startDate") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{4}$", message = "Invalid startDate format. Expected: yyyy-MM-dd'T'HH:mm:ssZ") String startDate,
                                                                 @Parameter(description = "The date the order was created before (ISO 8601 format with timezone, e.g., '2025-06-08T18:00:00+0300')", example = "2025-06-08T18:00:00+0300") @RequestParam(name = "endDate") @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[+-]\\d{4}$", message = "Invalid endDate format. Expected: yyyy-MM-dd'T'HH:mm:ssZ") String endDate,
                                                                 @Parameter(description = "The page number of the current results.") @RequestParam(name = PAGE_NUMBER, defaultValue = "0") @Min(value = 0, message = PAGE_NUMBER_MIN_SIZE_VIOLATION) @Max(value = 500, message = PAGE_NUMBER_MAX_SIZE_VIOLATION) Integer pageNumber, @Parameter(description = "The number of records returned with a single API call.") @RequestParam(name = PAGE_SIZE, defaultValue = "10") @Min(value = 0, message = PAGE_SIZE_MIN_SIZE_VIOLATION) @Max(value = 500, message = PAGE_SIZE_MAX_SIZE_VIOLATION) Integer pageSize) throws CustomException {
        String targetCustomerId = resolveCustomerId(userDetails, customerIdHeader);
        return orderService.getOrdersByCustomerIdAndDate(targetCustomerId, ZonedDateTime.parse(startDate, timeFormatter), ZonedDateTime.parse(endDate, timeFormatter), pageNumber, pageSize);
    }


    @Operation(summary = "Make requests to cancel a order.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Order is canceled."),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or data.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized access.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden. Insufficient permissions.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "Order not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponseModel.class))})})
    @ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = RestErrorResponseModel.class)))
    @DeleteMapping
    public ResponseEntity<Object> deleteOrder(@AuthenticationPrincipal UserDetailsImpl userDetails, @Parameter(description = "Order identifier") @NotBlank @RequestParam(name = ORDERID) final String orderId) throws CustomException {
        log.debug("Request received for cancel order is {}", orderId);

        orderValidator.validateOrderExistenceAndOwnership(orderId, isAdmin(userDetails) ? null : userDetails.getId().toString());
        return orderService.cancelOrder(orderId);
    }


    @Operation(
            summary = "Admin: Match a Pending Order",
            description = "Allows an administrator to manually match a pending stock order. " +
                    "This operation updates the order's status to 'MATCHED' and adjusts " +
                    "the 'size' and 'usableSize' of the associated assets (TRY and the traded asset) " +
                    "for the customer. Only PENDING orders can be matched."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order successfully matched and assets updated.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrdersRestResponseListModel.class), examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "404", description = "Asset not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponseModel.class))})})
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(path = MATCH_ENDPOINT)
    public ResponseEntity<OrdersRestResponseListModel> matchOrders(@Parameter(description = "Order identifier that will be matched") @NotBlank @RequestParam(name = ORDERID) final String orderId) throws CustomException {
        log.debug("Received request to match order with ID: {}", orderId);
        return orderService.matchOrders(orderId);
    }
}