package com.rasit.brokage.service;

import com.rasit.brokage.rest.converter.AssetConverter;
import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.rest.resource.asset.AssetRestResponseModel;
import com.rasit.brokage.rest.resource.asset.AssetsRestResponseListModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetDao assetDao;

    @Mock
    private AssetConverter assetConverter;

    @InjectMocks
    private AssetService assetService;

    private String testCustomerId;
    private int testPageNumber;
    private int testPageSize;
    private Pageable testPageable;

    @BeforeEach
    void setUp() {
        testCustomerId = "customer123";
        testPageNumber = 0;
        testPageSize = 10;
        testPageable = PageRequest.of(testPageNumber, testPageSize);
    }

    @Test
    void getAssetsByCustomerId_shouldReturnAssets_whenContentExists() {
        // Arrange
        AssetEntity asset1 = new AssetEntity();
        asset1.setCustomerId(testCustomerId);
        asset1.setAssetName("GOLD");
        asset1.setSize(new BigDecimal("10.0"));
        asset1.setUsableSize(new BigDecimal("5.0"));

        AssetEntity asset2 = new AssetEntity();
        asset2.setCustomerId(testCustomerId);
        asset2.setAssetName("TRY");
        asset2.setSize(new BigDecimal("1000.0"));
        asset2.setUsableSize(new BigDecimal("800.0"));

        List<AssetEntity> assetEntities = Arrays.asList(asset1, asset2);
        Page<AssetEntity> pagedResult = new PageImpl<>(assetEntities, testPageable, assetEntities.size());

        AssetRestResponseModel responseModel1 = new AssetRestResponseModel();
        responseModel1.setAssetName("GOLD");
        responseModel1.setSize(new BigDecimal("10.0"));

        AssetRestResponseModel responseModel2 = new AssetRestResponseModel();
        responseModel2.setAssetName("TRY");
        responseModel2.setSize(new BigDecimal("1000.0"));

        List<AssetRestResponseModel> responseModels = Arrays.asList(responseModel1, responseModel2);

        when(assetDao.findAssetsByCustomerId(eq(testCustomerId), eq(testPageable)))
                .thenReturn(pagedResult);
        when(assetConverter.toResourceList(eq(assetEntities)))
                .thenReturn(responseModels);

        // Act
        ResponseEntity<AssetsRestResponseListModel> responseEntity =
                assetService.getAssetsByCustomerId(testCustomerId, testPageNumber, testPageSize);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(responseModels.size(), responseEntity.getBody().getAssetRestResponseModels().size());
        assertEquals(testPageNumber, responseEntity.getBody().getPageNumber());
        assertEquals(testPageSize, responseEntity.getBody().getPageSize());
        assertEquals(pagedResult.getTotalElements(), responseEntity.getBody().getTotalRecords());

        verify(assetDao, times(1)).findAssetsByCustomerId(eq(testCustomerId), eq(testPageable));
        verify(assetConverter, times(1)).toResourceList(eq(assetEntities));
    }

    @Test
    void getAssetsByCustomerId_shouldReturnEmptyList_whenNoContentExists() {
        // Arrange
        Page<AssetEntity> pagedResult = new PageImpl<>(Collections.emptyList(), testPageable, 0);

        when(assetDao.findAssetsByCustomerId(eq(testCustomerId), eq(testPageable)))
                .thenReturn(pagedResult);

        // Act
        ResponseEntity<AssetsRestResponseListModel> responseEntity =
                assetService.getAssetsByCustomerId(testCustomerId, testPageNumber, testPageSize);

        // Assert
        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(testPageNumber, responseEntity.getBody().getPageNumber());
        assertEquals(0, responseEntity.getBody().getPageSize());
        assertEquals(pagedResult.getTotalElements(), responseEntity.getBody().getTotalRecords());
        assertEquals(0, responseEntity.getBody().getAssetRestResponseModels().size());
        assertEquals(0, responseEntity.getBody().getPageCount());

        verify(assetDao, times(1)).findAssetsByCustomerId(eq(testCustomerId), eq(testPageable));
        verify(assetConverter, times(0)).toResourceList(any());
    }

    @Test
    void getAssetsByCustomerId_shouldCallDaoWithCorrectParameters() {
        // Arrange
        // We only care that the DAO is called, not the result for this test
        Page<AssetEntity> emptyPage = new PageImpl<>(Collections.emptyList(), testPageable, 0);
        when(assetDao.findAssetsByCustomerId(any(String.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        assetService.getAssetsByCustomerId(testCustomerId, testPageNumber, testPageSize);

        // Assert
        // Verify that findAssetsByCustomerId was called exactly once with the correct customerId and Pageable
        verify(assetDao, times(1)).findAssetsByCustomerId(eq(testCustomerId), eq(testPageable));
    }
}