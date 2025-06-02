package com.rasit.brokage.rest.resource.asset;

import com.rasit.brokage.rest.resource.BaseRestModel;
import com.rasit.brokage.utility.BrokageConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.rasit.brokage.utility.BrokageConstants.*;

@Schema(description = "Asset information.")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetRestResponseModel implements BaseRestModel {

    @Schema(description = "Unique identifier of the asset.")
    private String identifier;

    private String customerId;

    @NotBlank(message = ASSET_NAME_BLANK_VIOLATION)
    @Size(min = 1, max = 128, message = ASSET_NAME_SIZE_VIOLATION)
    private String assetName;

    @DecimalMin(value = "0.01", message = BrokageConstants.ORDER_SIZE_MIN_SIZE_VIOLATION)
    @DecimalMax(value = "1000000.00", message = BrokageConstants.ORDER_SIZE_MAX_SIZE_VIOLATION)
    private BigDecimal size;

    @DecimalMin(value = "0.01", message = BrokageConstants.ORDER_SIZE_MIN_SIZE_VIOLATION)
    @DecimalMax(value = "1000000.00", message = BrokageConstants.ORDER_SIZE_MAX_SIZE_VIOLATION)
    private BigDecimal usableSize;
}
