package com.rasit.brokage.core.data.repository;

import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.core.data.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AssetsRepository extends JpaRepository<AssetEntity, UUID> {

    OrderEntity findByCustomerId(String customerId);

    /**
     * Returns true if an Asset exists with a matching name.
     *
     * @param assetName name of the asset to search for
     * @return true if a matching tenant was found; false otherwise
     */
    @Query("select case when count(a)> 0 then true else false end from AssetEntity a where lower(a.assetName)= :assetName and a.customerId= :customerId")
    boolean existsAssetByNameAndCustomerId(@Param("customerId") String customerId, @Param("assetName") String assetName);

    /**
     * Returns an asset by its name.
     *
     * @param assetName name of asset
     * @return the asset with the given assetName or null if none found
     */
    Optional<AssetEntity> findByAssetName(String assetName);

    /**
     * Returns an asset by its name and customerId.
     *
     * @param assetName  name of asset
     * @param customerId asset of the customer to search for
     * @return the asset with the given assetName and customerId or null if none found
     */
    Optional<AssetEntity> findByCustomerIdAndAssetNameIgnoreCase(String customerId, String assetName);

    Page<AssetEntity> findAllByCustomerId(String customerId, Pageable paging);

    @Modifying
    @Query("UPDATE AssetEntity a SET a.usableSize = a.usableSize - :transactionAmount where lower(a.assetName)= :assetName and a.customerId= :customerId")
    void subtractFromUsableSizeByCustomerIdAndAssetName(@Param("customerId") String customerId, @Param("assetName") String assetName, @Param("transactionAmount") BigDecimal transactionAmount);

    @Modifying
    @Query("UPDATE AssetEntity a SET a.usableSize = a.usableSize + :transactionAmount where lower(a.assetName)= :assetName and a.customerId= :customerId")
    void addToUsableSizeByCustomerIdAndAssetName(@Param("customerId") String customerId, @Param("assetName") String assetName, @Param("transactionAmount") BigDecimal transactionAmount);


    @Modifying
    @Query("UPDATE AssetEntity a SET a.size = a.size - :transactionAmount where lower(a.assetName)= :assetName and a.customerId= :customerId")
    void subtractFromSizeByCustomerIdAndAssetName(@Param("customerId") String customerId, @Param("assetName") String assetName, @Param("transactionAmount") BigDecimal transactionAmount);

    @Modifying
    @Query("UPDATE AssetEntity a SET a.size = a.size + :transactionAmount where lower(a.assetName)= :assetName and a.customerId= :customerId")
    void addToSizeByCustomerIdAndAssetName(@Param("customerId") String customerId, @Param("assetName") String assetName, @Param("transactionAmount") BigDecimal transactionAmount);

}
