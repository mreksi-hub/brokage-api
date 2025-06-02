package com.rasit.brokage.rest.resource.order;

import com.rasit.brokage.rest.resource.BaseRestModel;
import com.rasit.brokage.utility.BrokageConstants;
import com.rasit.brokage.utility.SideType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Schema(description = "New order request details.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRestRequestModel implements BaseRestModel {
    @Schema(hidden = true)
    private String customerId;

    @Schema(example = "GOLD")
    @NotBlank(message = BrokageConstants.ASSET_NAME_BLANK_VIOLATION)
    @Size(min = 1, max = 128, message = BrokageConstants.ASSET_NAME_SIZE_VIOLATION)
    private String assetName;

    @Schema(example = "BUY", allowableValues = {"BUY", "SELL"})
    @NotNull(message = BrokageConstants.ORDER_SIDE_NULL_VIOLATION)
    private SideType orderSide;

    @Schema(example = "10.00")
    @NotNull(message = BrokageConstants.ORDER_SIZE_NULL_VIOLATION)
    @DecimalMin(value = "0.01", message = BrokageConstants.ORDER_SIZE_MIN_SIZE_VIOLATION)
    @DecimalMax(value = "1000000.00", message = BrokageConstants.ORDER_SIZE_MAX_SIZE_VIOLATION)
    private BigDecimal size;

    @Schema(example = "150.75")
    @NotNull(message = BrokageConstants.ORDER_PRICE_NULL_VIOLATION)
    @DecimalMin(value = "0.01", message = BrokageConstants.ORDER_PRICE_MIN_VIOLATION)
    @DecimalMax(value = "1000000.00", message = BrokageConstants.ORDER_PRICE_MAX_VIOLATION)
    private BigDecimal price;
}
