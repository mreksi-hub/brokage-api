package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import java.util.List;
import java.util.UUID;

public interface OrderDao {
    Page<OrderEntity> getOrdersByCustomerIdAndDate(String customerId, ZonedDateTime startDate, ZonedDateTime endDate, Pageable paging);

    void saveOrderAndReduceUsableSizeofAsset(OrderEntity orderEntity, String assetToUpdateUsableSize, BigDecimal transactionAmount);

    void cancelOrderAndIncreaseUsableSizeofAsset(String orderId, String assetToUpdateUsableSize, BigDecimal transactionAmount);

    boolean isOrderExistsWithOrderId(String orderId);

    boolean isOrderExistsWithOrderIdAndCustomerId(String orderId, String customerId);

    OrderEntity getOrderByOrderIdAndStatus(String orderId, StatusType statusType);

    List<OrderEntity> getAllByStatusAndAssetNameAndSide(StatusType statusType, String assetName, SideType sideType);

    void updateOrderStatusWithMatched(UUID orderID);

    void updateOrderSize(UUID orderID, BigDecimal size);

    OrderEntity save(OrderEntity orderEntity);

    void deleteAll();
}
