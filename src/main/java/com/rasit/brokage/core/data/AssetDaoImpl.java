package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.core.data.repository.AssetsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
@Slf4j
public class AssetDaoImpl implements AssetDao {
    private final AssetsRepository assetsRepository;

    public AssetDaoImpl(AssetsRepository assetsRepository) {
        this.assetsRepository = assetsRepository;
    }

    @Override
    public boolean isAssetExistsWithCustomerIdAndName(String customerId, String name) {
        return assetsRepository.existsAssetByNameAndCustomerId(customerId, StringUtils.lowerCase(name));
    }

    @Override
    public AssetEntity findAssetByCustomerIdAndName(String customerId, String assetName) {
        Optional<AssetEntity> result = assetsRepository.findByCustomerIdAndAssetNameIgnoreCase(customerId, StringUtils.lowerCase(assetName));

        return result.orElse(null);
    }

    @Override
    public Page<AssetEntity> findAssetsByCustomerId(String customerId, Pageable paging) {
        return assetsRepository.findAllByCustomerId(customerId, paging);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reduceSizeofAsset(String customerId, String assetToUpdateSize, BigDecimal transactionAmount) {
        assetsRepository.subtractFromSizeByCustomerIdAndAssetName(customerId, StringUtils.lowerCase(assetToUpdateSize), transactionAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseSizeofAsset(String customerId, String assetToUpdateSize, BigDecimal transactionAmount) {
        assetsRepository.addToSizeByCustomerIdAndAssetName(customerId, StringUtils.lowerCase(assetToUpdateSize), transactionAmount);
    }

    @Override
    public AssetEntity save(AssetEntity asset) {
        return assetsRepository.save(asset);
    }

    @Override
    public void deleteAll() {
        assetsRepository.deleteAll();
    }
}
