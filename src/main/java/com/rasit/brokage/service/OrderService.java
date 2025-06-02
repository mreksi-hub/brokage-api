package com.rasit.brokage.service;

import com.rasit.brokage.rest.converter.OrderConverter;
import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.OrderDao;
import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.resource.order.OrderRestResponseModel;
import com.rasit.brokage.rest.resource.order.OrdersRestResponseListModel;
import com.rasit.brokage.rest.validation.AssetValidator;
import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rasit.brokage.utility.BrokageConstants.TRY_ASSET_NAME;

@Service
@Slf4j
public class OrderService {

    private final OrderDao orderDao;
    private final AssetDao assetDao;
    private final OrderConverter orderConverter;

    private final AssetValidator assetValidator;

    @Autowired
    public OrderService(OrderDao orderDao, AssetDao assetDao, OrderConverter orderConverter, AssetValidator assetValidator) {
        this.orderDao = orderDao;
        this.assetDao = assetDao;
        this.orderConverter = orderConverter;
        this.assetValidator = assetValidator;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<OrderRestResponseModel> createOrder(final OrderRestRequestModel order) throws CustomException {

        log.debug("createOrder method starting for new order: {}", order);
        assetValidator.checkCustomerHaveEnoughAsset(order.getCustomerId(), order.getSize(), order.getPrice(), order.getOrderSide(), order.getAssetName());

        OrderEntity orderEntity = orderConverter.toDomain(order);
        String assetToUpdateUsableSize;
        BigDecimal transactionAmount;
        if (order.getOrderSide().equals(SideType.BUY)) {
            assetToUpdateUsableSize = TRY_ASSET_NAME;
            transactionAmount = orderEntity.getSize().multiply(orderEntity.getPrice());
        } else {
            assetToUpdateUsableSize = order.getAssetName();
            transactionAmount = orderEntity.getSize();
        }

        orderDao.saveOrderAndReduceUsableSizeofAsset(orderEntity, assetToUpdateUsableSize, transactionAmount);
        OrderRestResponseModel newOrder = orderConverter.toResource(orderEntity);
        log.debug("Request completed for creating new order : {}", newOrder);
        return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
    }

    public ResponseEntity<OrdersRestResponseListModel> getOrdersByCustomerIdAndDate(String customerId, ZonedDateTime startDate, ZonedDateTime endDate, Integer pageNumber, Integer pageSize) {

        log.debug("HandleGetAllScheduledRooms method starting for pageNumber: {}, pageSize: {}, customerId: {}", pageNumber, pageSize, customerId);

        Pageable paging = PageRequest.of(pageNumber, pageSize);

        Page<OrderEntity> pagedResult = orderDao.getOrdersByCustomerIdAndDate(customerId, startDate, endDate, paging);

        if (pagedResult.hasContent()) {
            List<OrderEntity> orders = pagedResult.getContent();
            List<OrderRestResponseModel> orderRestResponseModels = orderConverter.toResourceList(orders);
            OrdersRestResponseListModel resourceList = new OrdersRestResponseListModel(orderRestResponseModels, pagedResult.getTotalPages(), pageNumber, pageSize, pagedResult.getTotalElements());
            return new ResponseEntity<>(resourceList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new OrdersRestResponseListModel(), HttpStatus.OK);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Object> cancelOrder(String orderId) {
        log.debug("cancelOrder method starting for orderId: {}", orderId);

        OrderEntity order = orderDao.getOrderByOrderIdAndStatus(orderId, StatusType.PENDING);

        if (order == null) return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        String assetToUpdateUsableSize;
        BigDecimal transactionAmount;

        if (order.getOrderSide().equals(SideType.BUY)) {
            assetToUpdateUsableSize = TRY_ASSET_NAME;
            transactionAmount = order.getSize().multiply(order.getPrice());
        } else {
            assetToUpdateUsableSize = order.getAssetName();
            transactionAmount = order.getSize();
        }

        orderDao.cancelOrderAndIncreaseUsableSizeofAsset(orderId, assetToUpdateUsableSize, transactionAmount);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Transactional
    public ResponseEntity<OrdersRestResponseListModel> matchOrders(String orderIdToMatch) {
        log.info("Attempting to match order with ID: {}", orderIdToMatch);
        OrderEntity orderToMatch = orderDao.getOrderByOrderIdAndStatus(orderIdToMatch, StatusType.PENDING);
        if (orderToMatch == null) {
            log.warn("Order with ID {} not found or not in PENDING status for matching.", orderIdToMatch);
            return ResponseEntity.notFound().build();
        }

        SideType orderSide = orderToMatch.getOrderSide();
        SideType counterSide = (orderSide == SideType.BUY) ? SideType.SELL : SideType.BUY;

        List<OrderEntity> potentialCounterOrders = new ArrayList<>(orderDao.getAllByStatusAndAssetNameAndSide(StatusType.PENDING, orderToMatch.getAssetName(), counterSide));

        if (potentialCounterOrders.isEmpty()) {
            log.debug("No pending counter-orders found for asset {} with side {}. Order {} remains PENDING.", orderToMatch.getAssetName(), counterSide, orderToMatch.getOrderId());

            return ResponseEntity.ok(new OrdersRestResponseListModel());
        }

        log.debug("Found {} potential counter-orders for order {}", potentialCounterOrders.size(), orderToMatch.getOrderId());

        Comparator<OrderEntity> comparator;

        if (orderSide == SideType.BUY) {
            // For BUY orders, match with lowest SELL price first
            comparator = Comparator.comparing(OrderEntity::getPrice).thenComparing(OrderEntity::getCreateDate);
        } else {
            // For SELL orders, match with highest BUY price first
            comparator = Comparator.comparing(OrderEntity::getPrice, Comparator.reverseOrder()).thenComparing(OrderEntity::getCreateDate);
        }

        potentialCounterOrders.sort(comparator);

        Iterator<OrderEntity> iterator = potentialCounterOrders.iterator();
        List<OrderRestResponseModel> matchedOrdersList = new ArrayList<>();

        while (orderToMatch.getSize().compareTo(BigDecimal.ZERO) > 0 && iterator.hasNext()) {
            OrderEntity counterOrder = iterator.next();

            if (!isPriceMatch(orderToMatch, counterOrder)) {
                break;
            }

            BigDecimal fillQuantity = orderToMatch.getSize().min(counterOrder.getSize());

            orderToMatch.setSize(orderToMatch.getSize().subtract(fillQuantity));
            counterOrder.setSize(counterOrder.getSize().subtract(fillQuantity));

            log.debug("Matched Order: {} @ {}", fillQuantity, counterOrder.getPrice());

            //opposite order transactions
            BigDecimal transactionAmount = fillQuantity.multiply(orderToMatch.getPrice());
            if (counterOrder.getOrderSide().equals(SideType.SELL)) {
                assetDao.increaseSizeofAsset(counterOrder.getCustomerId(), TRY_ASSET_NAME, transactionAmount);
                assetDao.reduceSizeofAsset(counterOrder.getCustomerId(), orderToMatch.getAssetName(), fillQuantity);
            } else {
                assetDao.increaseSizeofAsset(counterOrder.getCustomerId(), orderToMatch.getAssetName(), fillQuantity);
                assetDao.reduceSizeofAsset(counterOrder.getCustomerId(), TRY_ASSET_NAME, transactionAmount);
            }

            // order transactions
            transactionAmount = fillQuantity.multiply(counterOrder.getPrice());
            if (orderToMatch.getOrderSide().equals(SideType.SELL)) {
                assetDao.increaseSizeofAsset(orderToMatch.getCustomerId(), TRY_ASSET_NAME, transactionAmount);
                assetDao.reduceSizeofAsset(orderToMatch.getCustomerId(), orderToMatch.getAssetName(), fillQuantity);
            } else {
                assetDao.increaseSizeofAsset(orderToMatch.getCustomerId(), orderToMatch.getAssetName(), fillQuantity);
                assetDao.reduceSizeofAsset(orderToMatch.getCustomerId(), TRY_ASSET_NAME, transactionAmount);
            }

            if (counterOrder.getSize().compareTo(BigDecimal.ZERO) == 0) {
                iterator.remove();
                counterOrder.setStatus(StatusType.MATCHED);
                orderDao.updateOrderStatusWithMatched(counterOrder.getOrderId());
                matchedOrdersList.add(orderConverter.toResource(counterOrder));
            } else {
                orderDao.updateOrderSize(counterOrder.getOrderId(), counterOrder.getSize());
            }
        }
        if (orderToMatch.getSize().compareTo(BigDecimal.ZERO) == 0) {
            orderToMatch.setStatus(StatusType.MATCHED);
            orderDao.updateOrderStatusWithMatched(orderToMatch.getOrderId());
            matchedOrdersList.add(orderConverter.toResource(orderToMatch));
        } else {
            orderDao.updateOrderSize(orderToMatch.getOrderId(), orderToMatch.getSize());
        }

        log.debug("Order matching process for order {} completed. {} orders fully or partially matched.", orderIdToMatch, matchedOrdersList.size());

        OrdersRestResponseListModel resourceList = new OrdersRestResponseListModel(matchedOrdersList);
        return new ResponseEntity<>(resourceList, HttpStatus.OK);
    }

    /**
     * Determines if an order's price is a match for an opposite order based on their side and price.
     * <p>
     * For a BUY order, its price must be greater than or equal to the SELL order's price.
     * For a SELL order, its price must be less than or equal to the BUY order's price.
     *
     * @param orderEntity   The primary order to evaluate (e.g., the one being considered for matching).
     * @param oppositeOrder The order on the opposite side being compared against.
     * @return True if the prices match the conditions for a trade, false otherwise.
     */
    private boolean isPriceMatch(final OrderEntity orderEntity, final OrderEntity oppositeOrder) {
        if (orderEntity.getOrderSide() == SideType.BUY) {
            return orderEntity.getPrice().compareTo(oppositeOrder.getPrice()) >= 0;
        } else if (orderEntity.getOrderSide() == SideType.SELL) {
            return orderEntity.getPrice().compareTo(oppositeOrder.getPrice()) <= 0;
        }
        return false;
    }
}
