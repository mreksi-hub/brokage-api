package com.rasit.brokage.core.data;

import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.core.data.repository.AssetsRepository;
import com.rasit.brokage.core.data.repository.OrdersRepository;
import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class OrderDaoImpl implements OrderDao {
    private final OrdersRepository ordersRepository;
    private final AssetsRepository assetsRepository;

    public OrderDaoImpl(OrdersRepository ordersRepository, AssetsRepository assetsRepository) {
        this.ordersRepository = ordersRepository;
        this.assetsRepository = assetsRepository;
    }


    @Override
    public Page<OrderEntity> getOrdersByCustomerIdAndDate(String customerId, ZonedDateTime startDate, ZonedDateTime endDate, Pageable paging) {
        return ordersRepository.findAllByCustomerIdAndCreateDateBetween(customerId, startDate, endDate, paging);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderAndReduceUsableSizeofAsset(OrderEntity orderEntity, String assetToUpdateUsableSize, BigDecimal transactionAmount) {
        orderEntity.setCreateDate(ZonedDateTime.now());
        orderEntity = ordersRepository.save(orderEntity);
        assetsRepository.subtractFromUsableSizeByCustomerIdAndAssetName(orderEntity.getCustomerId(), StringUtils.lowerCase(assetToUpdateUsableSize), transactionAmount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrderAndIncreaseUsableSizeofAsset(String orderId, String assetToUpdateUsableSize, BigDecimal transactionAmount) {
        Optional<OrderEntity> orderEntity = ordersRepository.findByOrderIdAndStatus(UUID.fromString(orderId), StatusType.PENDING);
        if (orderEntity.isPresent()) {
            ordersRepository.updateOrderStatus(UUID.fromString(orderId), StatusType.PENDING, StatusType.CANCELED);
            assetsRepository.addToUsableSizeByCustomerIdAndAssetName(orderEntity.get().getCustomerId(), StringUtils.lowerCase(assetToUpdateUsableSize), transactionAmount);
        }
    }

    @Override
    public void updateOrderStatusWithMatched(UUID orderID) {
        ordersRepository.updateOrderStatus(orderID, StatusType.PENDING, StatusType.MATCHED);
    }


    @Override
    public boolean isOrderExistsWithOrderId(String orderId) {
        return ordersRepository.existsById(UUID.fromString(orderId));
    }

    @Override
    public boolean isOrderExistsWithOrderIdAndCustomerId(String orderId, String customerId) {
        return ordersRepository.existsByOrderIdAndCustomerId(UUID.fromString(orderId), customerId);
    }

    @Override
    public OrderEntity getOrderByOrderIdAndStatus(String orderId, StatusType statusType) {
        Optional<OrderEntity> orderEntity = ordersRepository.findByOrderIdAndStatus(UUID.fromString(orderId), statusType);
        return orderEntity.orElse(null);
    }

    @Override
    public List<OrderEntity> getAllByStatusAndAssetNameAndSide(StatusType statusType, String assetName, SideType sideType) {
        return ordersRepository.findAllByStatusAndAssetNameAndOrderSide(statusType, assetName, sideType);
    }

    @Override
    public void updateOrderSize(UUID orderID, BigDecimal size) {
        ordersRepository.updateOrderSizeByOrderId(orderID, size);
    }

    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        return ordersRepository.save(orderEntity);
    }

    @Override
    public void deleteAll() {
        ordersRepository.deleteAll();
    }
}
