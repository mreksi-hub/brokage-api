package com.rasit.brokage.rest.converter;

import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.rest.resource.asset.AssetRestResponseModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class AssetConverter {

    /**
     * Convert to asset rest response model from AssetEntity domain.
     *
     * @param assetEntity AssetEntity domain.
     * @return Asset rest response mode.
     */
    public AssetRestResponseModel toResource(final AssetEntity assetEntity) {
        return convertToResource(assetEntity);
    }

    private AssetRestResponseModel convertToResource(final AssetEntity assetEntity) {
        AssetRestResponseModel orderRestResponseModel = new AssetRestResponseModel();
        orderRestResponseModel.setIdentifier(assetEntity.getAssetId().toString());
        orderRestResponseModel.setCustomerId(assetEntity.getCustomerId());
        orderRestResponseModel.setAssetName(assetEntity.getAssetName());
        orderRestResponseModel.setSize(assetEntity.getSize());
        orderRestResponseModel.setUsableSize(assetEntity.getUsableSize());
        return orderRestResponseModel;
    }

    public List<AssetRestResponseModel> toResourceList(final List<AssetEntity> assetEntity) {
        return assetEntity.stream().filter(Objects::nonNull).map(this::convertToResource).toList();
    }
}
