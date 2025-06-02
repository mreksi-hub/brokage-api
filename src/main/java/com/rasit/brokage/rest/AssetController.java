package com.rasit.brokage.rest;

import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.asset.AssetsRestResponseListModel;
import com.rasit.brokage.service.AssetService;
import com.rasit.brokage.rest.security.UserDetailsImpl;
import com.rasit.brokage.utility.ApiPathValues;
import com.rasit.brokage.utility.BrokageConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.rasit.brokage.utility.ApiPathValues.LIST_ENDPOINT;
import static com.rasit.brokage.utility.BrokageConstants.*;
import static com.rasit.brokage.utility.BrokageUtil.resolveCustomerId;

@SecurityRequirement(name = "BearerAuth")
@Validated
@RestController
@RequestMapping(ApiPathValues.BASE_V1 + ApiPathValues.ASSET_ENDPOINT)
@Slf4j
public class AssetController {

    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "Make requests to get all assets.", description = "Allows for query parameters to paginate the results.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Assets are retrieved.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AssetsRestResponseListModel.class))})})
    @GetMapping(path = LIST_ENDPOINT)
    public ResponseEntity<AssetsRestResponseListModel> getAssets(@AuthenticationPrincipal UserDetailsImpl userDetails, @Parameter(description = "Its allows Admin user to create order for customer.", example = "39aeef68-f97b-4c05-8385-cfe4c0f49b5b") @RequestHeader(value = X_CUSTOMER_ID, required = false) final String customerIdHeader, @Parameter(description = "The page number of the current results.") @RequestParam(name = BrokageConstants.PAGE_NUMBER, defaultValue = "0") @Min(value = 0, message = PAGE_NUMBER_MIN_SIZE_VIOLATION) @Max(value = 500, message = BrokageConstants.PAGE_NUMBER_MAX_SIZE_VIOLATION) Integer pageNumber, @Parameter(description = "The number of records returned with a single API call.") @RequestParam(name = BrokageConstants.PAGE_SIZE, defaultValue = "10") @Min(value = 0, message = PAGE_SIZE_MIN_SIZE_VIOLATION) @Max(value = 500, message = BrokageConstants.PAGE_SIZE_MAX_SIZE_VIOLATION) Integer pageSize) throws CustomException {
        String targetCustomerId = resolveCustomerId(userDetails, customerIdHeader);
        return assetService.getAssetsByCustomerId(targetCustomerId, pageNumber, pageSize);
    }
}