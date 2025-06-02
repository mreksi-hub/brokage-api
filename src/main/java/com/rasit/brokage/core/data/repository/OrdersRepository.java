package com.rasit.brokage.core.data.repository;

import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface OrdersRepository extends JpaRepository<OrderEntity, UUID> {
    Page<OrderEntity> findAllByCustomerIdAndCreateDateBetween(String customerId, ZonedDateTime startDate, ZonedDateTime endDate, Pageable paging);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.status=:newStatus where o.orderId= :orderId and o.status= :status")
    void updateOrderStatus(@Param("orderId") UUID orderId, @Param("status") StatusType status, @Param("newStatus") StatusType newStatus);

    Optional<OrderEntity> findByOrderIdAndStatus(UUID orderId, StatusType status);

    List<OrderEntity> findAllByStatusAndAssetNameAndOrderSide(StatusType status, String assetName, SideType sideType);

    boolean existsByOrderIdAndCustomerId(UUID orderId, String customerId);

    @Modifying
    @Query("UPDATE OrderEntity o SET o.size=:size where o.orderId= :orderId")
    void updateOrderSizeByOrderId(@Param("orderId") UUID orderId, @Param("size") BigDecimal size);
}
