package com.rasit.brokage.rest.validation;

import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.utility.ErrorMessageType;
import com.rasit.brokage.utility.SideType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

import static com.rasit.brokage.utility.BrokageConstants.TRY_ASSET_NAME;


/**
 * Validator for asset details,
 */
@Slf4j
@Component
@Validated
public class AssetValidator {
    private final AssetDao assetDao;

    @Autowired
    public AssetValidator(final AssetDao assetDao) {
        this.assetDao = assetDao;
    }

    /**
     * Validates that a specific asset exists for a given customer.
     * Throws a CustomException if the asset is not found for the customer.
     *
     * @param customerId The ID of the customer.
     * @param assetName The name of the asset (e.g., "GOLD", "AAPL").
     * @throws CustomException with ASSET_NOT_FOUND_FOR_CUSTOMER type if the asset does not exist for the customer.
     */
    public void validateCustomerAssetExists(String customerId, String assetName) throws CustomException {
        if (assetDao.isAssetExistsWithCustomerIdAndName(customerId, assetName)) {
            log.debug("Asset exists with the asset name : {}", assetName);
            return;
        }
        throw new CustomException(ErrorMessageType.ASSET_NOT_FOUND,
                new String[]{assetName},
                HttpStatus.NOT_FOUND);
    }

    /**
     * Validates if a customer has enough usable assets for a given order.
     * This method handles both BUY (checking TRY balance) and SELL (checking stock balance) sides.
     *
     * @param customerId The unique ID of the customer.
     * @param orderSize  The quantity of shares/units in the order.
     * @param orderPrice The price per share/unit (relevant for BUY orders).
     * @param orderSide  The side of the order (BUY or SELL).
     * @param assetName  The name of the asset being traded (e.g., "AAPL", "XU100").
     * @throws CustomException if the customer does not have enough usable assets.
     */
    public void checkCustomerHaveEnoughAsset(String customerId, BigDecimal orderSize, BigDecimal orderPrice, SideType orderSide, String assetName) throws CustomException {
        String assetToCheckName;
        BigDecimal requiredAmount;

        if (orderSide.equals(SideType.BUY)) {
            assetToCheckName = TRY_ASSET_NAME;
            requiredAmount = orderSize.multiply(orderPrice);
        } else {
            assetToCheckName = assetName;
            requiredAmount = orderSize;
        }

        AssetEntity asset = assetDao.findAssetByCustomerIdAndName(customerId, assetToCheckName);

        if (requiredAmount.compareTo(asset.getUsableSize()) > 0) {
            log.debug("Customer {} does NOT have enough usable {}. Required: {}, Usable: {}",
                    customerId, assetToCheckName, requiredAmount, asset.getUsableSize());
            throw new CustomException(ErrorMessageType.ASSET_USABLE_SIZE_NOT_ENOUGH,
                    new String[]{assetToCheckName, String.valueOf(asset.getUsableSize())},
                    HttpStatus.BAD_REQUEST);
        }

        log.debug("Customer {} has enough usable {}. Required: {}, Usable: {}",
                customerId, assetToCheckName, requiredAmount, asset.getUsableSize());
    }
}
