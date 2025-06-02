package com.rasit.brokage.rest.converter;

import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.resource.order.OrderRestResponseModel;
import com.rasit.brokage.utility.BrokageUtil;
import com.rasit.brokage.utility.StatusType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OrderConverter {

    /**
     * Convert to {@link OrderEntity} domain object from {@link OrderRestRequestModel}.
     *
     * @param orderRestRequestModel {@link OrderRestRequestModel} object that needs to be
     *                              converted.
     * @return {@link OrderEntity} domain object.
     */
    public OrderEntity toDomain(OrderRestRequestModel orderRestRequestModel) {
        return convertToDomain(orderRestRequestModel, null);
    }

    /**
     * Convert to OrderEntity domain from user resource.
     *
     * @param orderRestRequestModel order resource.
     * @param orderEntity           {@link OrderEntity} domain object from which existing
     *                              data is taken and modified attributes updated.
     * @return ExternalUsersEntry domain.
     */
    public OrderEntity convertToDomain(OrderRestRequestModel orderRestRequestModel, OrderEntity orderEntity) {
        if (orderEntity == null) {
            orderEntity = new OrderEntity();
        }
        if (Objects.nonNull(orderRestRequestModel.getCustomerId())) {
            orderEntity.setCustomerId(orderRestRequestModel.getCustomerId());
        }
        if (Objects.nonNull(orderRestRequestModel.getAssetName())) {
            orderEntity.setAssetName(orderRestRequestModel.getAssetName());
        }
        if (Objects.nonNull(orderRestRequestModel.getOrderSide())) {
            orderEntity.setOrderSide(orderRestRequestModel.getOrderSide());
        }
        orderEntity.setStatus(StatusType.PENDING);
        orderEntity.setSize(orderRestRequestModel.getSize());
        orderEntity.setPrice(orderRestRequestModel.getPrice());
        return orderEntity;
    }

    /**
     * Convert to order rest response model from OrderEntity domain.
     *
     * @param orderEntity OrderEntity domain.
     * @return Order rest response mode.
     */
    public OrderRestResponseModel toResource(final OrderEntity orderEntity) {
        return convertToResource(orderEntity);
    }

    private OrderRestResponseModel convertToResource(final OrderEntity orderEntity) {
        OrderRestResponseModel orderRestResponseModel = new OrderRestResponseModel();
        orderRestResponseModel.setIdentifier(orderEntity.getOrderId().toString());
        orderRestResponseModel.setAssetName(orderEntity.getAssetName());
        orderRestResponseModel.setCustomerId(orderEntity.getCustomerId());
        orderRestResponseModel.setOrderSide(orderEntity.getOrderSide());
        orderRestResponseModel.setSize(orderEntity.getSize());
        orderRestResponseModel.setPrice(orderEntity.getPrice());
        orderRestResponseModel.setStatus(orderEntity.getStatus().name());
        orderRestResponseModel.setCreateDate(orderEntity.getCreateDate().format(BrokageUtil.timeFormatter));
        return orderRestResponseModel;
    }

    public List<OrderRestResponseModel> toResourceList(final List<OrderEntity> orderEntity) {
        return orderEntity.stream().filter(Objects::nonNull).map(this::convertToResource).toList();
    }
}
