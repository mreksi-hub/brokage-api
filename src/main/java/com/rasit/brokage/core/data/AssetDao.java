package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.AssetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

public interface AssetDao {
    boolean isAssetExistsWithCustomerIdAndName(String customerId, String name);

    AssetEntity findAssetByCustomerIdAndName(String customerId, String assetName);

    Page<AssetEntity> findAssetsByCustomerId(String customerId, Pageable paging);

    void reduceSizeofAsset(String customerId, String assetToUpdateSize, BigDecimal transactionAmount);

    void increaseSizeofAsset(String customerId, String assetToUpdateSize, BigDecimal transactionAmount);

    @Transactional(rollbackFor = Exception.class)
    void increaseUsableSizeofAsset(String customerId, String assetToUpdateSize, BigDecimal transactionAmount);

    AssetEntity save(AssetEntity asset);

    void deleteAll();
}
