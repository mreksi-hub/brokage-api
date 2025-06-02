package com.rasit.brokage.service;

import com.rasit.brokage.rest.converter.AssetConverter;
import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.rest.resource.asset.AssetRestResponseModel;
import com.rasit.brokage.rest.resource.asset.AssetsRestResponseListModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AssetService {

    private final AssetDao assetDao;
    private final AssetConverter assetConverter;

    @Autowired
    public AssetService(AssetDao assetDao, AssetConverter assetConverter) {
        this.assetDao = assetDao;
        this.assetConverter = assetConverter;
    }

    public ResponseEntity<AssetsRestResponseListModel> getAssetsByCustomerId(String customerId, Integer pageNumber, Integer pageSize) {

        log.debug("getAssetsByCustomerId method starting for pageNumber: {}, pageSize: {}, customerId: {}",
                pageNumber, pageSize, customerId);

        Pageable paging = PageRequest.of(pageNumber, pageSize);

        Page<AssetEntity> pagedResult = assetDao.findAssetsByCustomerId(customerId, paging);

        if (pagedResult.hasContent()) {
            List<AssetEntity> assets = pagedResult.getContent();
            List<AssetRestResponseModel> assetRestResponseModels = assetConverter.toResourceList(assets);
            AssetsRestResponseListModel resourceList = new AssetsRestResponseListModel(assetRestResponseModels, pagedResult.getTotalPages(), pageNumber,
                    pageSize, pagedResult.getTotalElements());
            return new ResponseEntity<>(resourceList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new AssetsRestResponseListModel(), HttpStatus.OK);
        }
    }
}