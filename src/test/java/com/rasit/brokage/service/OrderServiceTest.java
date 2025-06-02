package com.rasit.brokage.service;

import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.OrderDao;
import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.rest.converter.OrderConverter;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.resource.order.OrderRestResponseModel;
import com.rasit.brokage.rest.resource.order.OrdersRestResponseListModel;
import com.rasit.brokage.rest.validation.AssetValidator;
import com.rasit.brokage.utility.SideType;
import com.rasit.brokage.utility.StatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.rasit.brokage.utility.BrokageConstants.TRY_ASSET_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private AssetDao assetDao;

    @Mock
    private OrderConverter orderConverter;

    @Mock
    private AssetValidator assetValidator;

    @InjectMocks
    private OrderService orderService;

    private String customerId;
    private String orderId;
    private OrderRestRequestModel buyOrderRequest;
    private OrderRestRequestModel sellOrderRequest;
    private OrderEntity buyOrderEntity;
    private OrderEntity sellOrderEntity;
    private OrderRestResponseModel buyOrderResponse;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID().toString();
        orderId = UUID.randomUUID().toString();

        buyOrderRequest = new OrderRestRequestModel();
        buyOrderRequest.setCustomerId(customerId);
        buyOrderRequest.setAssetName("GOLD");
        buyOrderRequest.setOrderSide(SideType.BUY);
        buyOrderRequest.setSize(BigDecimal.valueOf(10));
        buyOrderRequest.setPrice(BigDecimal.valueOf(100));

        sellOrderRequest = new OrderRestRequestModel();
        sellOrderRequest.setCustomerId(customerId);
        sellOrderRequest.setAssetName("GOLD");
        sellOrderRequest.setOrderSide(SideType.SELL);
        sellOrderRequest.setSize(BigDecimal.valueOf(5));
        sellOrderRequest.setPrice(BigDecimal.valueOf(90));

        buyOrderEntity = new OrderEntity();
        buyOrderEntity.setOrderId(UUID.fromString(orderId));
        buyOrderEntity.setCustomerId(customerId);
        buyOrderEntity.setAssetName("GOLD");
        buyOrderEntity.setOrderSide(SideType.BUY);
        buyOrderEntity.setSize(BigDecimal.valueOf(10));
        buyOrderEntity.setPrice(BigDecimal.valueOf(100));
        buyOrderEntity.setStatus(StatusType.PENDING);

        sellOrderEntity = new OrderEntity();
        sellOrderEntity.setOrderId(UUID.randomUUID());
        sellOrderEntity.setCustomerId(customerId);
        sellOrderEntity.setAssetName("GOLD");
        sellOrderEntity.setOrderSide(SideType.SELL);
        sellOrderEntity.setSize(BigDecimal.valueOf(5));
        sellOrderEntity.setPrice(BigDecimal.valueOf(90));
        sellOrderEntity.setStatus(StatusType.PENDING);


        buyOrderResponse = new OrderRestResponseModel();
        buyOrderResponse.setIdentifier(orderId);
        buyOrderResponse.setCustomerId(customerId);
        buyOrderResponse.setAssetName("GOLD");
        buyOrderResponse.setOrderSide(SideType.BUY);
        buyOrderResponse.setSize(BigDecimal.valueOf(10));
        buyOrderResponse.setPrice(BigDecimal.valueOf(100));
        buyOrderResponse.setStatus("PENDING");
    }

    @Test
    void createOrder_shouldCreateBuyOrderSuccessfully() throws CustomException {
        doNothing().when(assetValidator).checkCustomerHaveEnoughAsset(anyString(), any(BigDecimal.class), any(BigDecimal.class), any(SideType.class), anyString());
        when(orderConverter.toDomain(buyOrderRequest)).thenReturn(buyOrderEntity);
        doNothing().when(orderDao).saveOrderAndReduceUsableSizeofAsset(any(OrderEntity.class), eq(TRY_ASSET_NAME), any(BigDecimal.class));
        when(orderConverter.toResource(buyOrderEntity)).thenReturn(buyOrderResponse);

        ResponseEntity<OrderRestResponseModel> response = orderService.createOrder(buyOrderRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(buyOrderResponse, response.getBody());
        verify(assetValidator, times(1)).checkCustomerHaveEnoughAsset(customerId, BigDecimal.valueOf(10), BigDecimal.valueOf(100), SideType.BUY, "GOLD");
        verify(orderConverter, times(1)).toDomain(buyOrderRequest);

        ArgumentCaptor<BigDecimal> transactionAmountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(orderDao, times(1)).saveOrderAndReduceUsableSizeofAsset(eq(buyOrderEntity), eq(TRY_ASSET_NAME), transactionAmountCaptor.capture());
        assertEquals(BigDecimal.valueOf(1000), transactionAmountCaptor.getValue()); // 10 * 100
        verify(orderConverter, times(1)).toResource(buyOrderEntity);
    }

    @Test
    void createOrder_shouldCreateSellOrderSuccessfully() throws CustomException {
        doNothing().when(assetValidator).checkCustomerHaveEnoughAsset(anyString(), any(BigDecimal.class), any(BigDecimal.class), any(SideType.class), anyString());
        when(orderConverter.toDomain(sellOrderRequest)).thenReturn(sellOrderEntity);
        doNothing().when(orderDao).saveOrderAndReduceUsableSizeofAsset(any(OrderEntity.class), eq("GOLD"), any(BigDecimal.class));
        when(orderConverter.toResource(sellOrderEntity)).thenReturn(buyOrderResponse); // Reusing buyOrderResponse for simplicity

        ResponseEntity<OrderRestResponseModel> response = orderService.createOrder(sellOrderRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(buyOrderResponse, response.getBody());
        verify(assetValidator, times(1)).checkCustomerHaveEnoughAsset(customerId, BigDecimal.valueOf(5), BigDecimal.valueOf(90), SideType.SELL, "GOLD");
        verify(orderConverter, times(1)).toDomain(sellOrderRequest);

        ArgumentCaptor<BigDecimal> transactionAmountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(orderDao, times(1)).saveOrderAndReduceUsableSizeofAsset(eq(sellOrderEntity), eq("GOLD"), transactionAmountCaptor.capture());
        assertEquals(BigDecimal.valueOf(5), transactionAmountCaptor.getValue()); // Just size for SELL
        verify(orderConverter, times(1)).toResource(sellOrderEntity);
    }

    @Test
    void getOrdersByCustomerIdAndDate_shouldReturnOrders_whenContentExists() {
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        int pageNumber = 0;
        int pageSize = 10;
        Pageable paging = PageRequest.of(pageNumber, pageSize);

        List<OrderEntity> orderEntities = Arrays.asList(buyOrderEntity, sellOrderEntity);
        Page<OrderEntity> pagedResult = new PageImpl<>(orderEntities, paging, orderEntities.size());

        List<OrderRestResponseModel> orderRestResponses = Arrays.asList(buyOrderResponse, buyOrderResponse); // Reusing for simplicity

        when(orderDao.getOrdersByCustomerIdAndDate(customerId, startDate, endDate, paging)).thenReturn(pagedResult);
        when(orderConverter.toResourceList(orderEntities)).thenReturn(orderRestResponses);

        ResponseEntity<OrdersRestResponseListModel> response = orderService.getOrdersByCustomerIdAndDate(customerId, startDate, endDate, pageNumber, pageSize);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getOrderRestResponseModels().size());
        assertEquals(1, response.getBody().getPageCount());
        assertEquals(pageNumber, response.getBody().getPageNumber());
        assertEquals(pageSize, response.getBody().getPageSize());
        assertEquals(2, response.getBody().getTotalRecords());
        verify(orderDao, times(1)).getOrdersByCustomerIdAndDate(customerId, startDate, endDate, paging);
        verify(orderConverter, times(1)).toResourceList(orderEntities);
    }

    @Test
    void getOrdersByCustomerIdAndDate_shouldReturnEmptyList_whenNoContentExists() {
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);
        int pageNumber = 0;
        int pageSize = 10;
        Pageable paging = PageRequest.of(pageNumber, pageSize);

        Page<OrderEntity> pagedResult = new PageImpl<>(Collections.emptyList(), paging, 0);

        when(orderDao.getOrdersByCustomerIdAndDate(customerId, startDate, endDate, paging)).thenReturn(pagedResult);

        ResponseEntity<OrdersRestResponseListModel> response = orderService.getOrdersByCustomerIdAndDate(customerId, startDate, endDate, pageNumber, pageSize);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getOrderRestResponseModels().size());
        assertEquals(0, response.getBody().getPageCount());
        assertEquals(pageNumber, response.getBody().getPageNumber());
        assertEquals(0, response.getBody().getPageSize());
        assertEquals(0, response.getBody().getTotalRecords());
        verify(orderDao, times(1)).getOrdersByCustomerIdAndDate(customerId, startDate, endDate, paging);
    }

    @Test
    void cancelOrder_shouldReturnNoContent_whenOrderIsPending() {
        when(orderDao.getOrderByOrderIdAndStatus(orderId, StatusType.PENDING)).thenReturn(buyOrderEntity);
        doNothing().when(orderDao).cancelOrderAndIncreaseUsableSizeofAsset(eq(orderId), eq(TRY_ASSET_NAME), any(BigDecimal.class));

        ResponseEntity<Object> response = orderService.cancelOrder(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        ArgumentCaptor<BigDecimal> transactionAmountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(orderDao, times(1)).cancelOrderAndIncreaseUsableSizeofAsset(eq(orderId), eq(TRY_ASSET_NAME), transactionAmountCaptor.capture());
        assertEquals(BigDecimal.valueOf(1000), transactionAmountCaptor.getValue());
    }

    @Test
    void cancelOrder_shouldReturnNoContent_whenOrderNotFound() {
        when(orderDao.getOrderByOrderIdAndStatus(orderId, StatusType.PENDING)).thenReturn(null);

        ResponseEntity<Object> response = orderService.cancelOrder(orderId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderDao, never()).cancelOrderAndIncreaseUsableSizeofAsset(any(), any(), any());
    }

    @Test
    void matchOrders_shouldReturnOkAndMatchedOrder_whenFullMatchExists() {
        OrderEntity buyOrder = new OrderEntity();
        buyOrder.setOrderId(UUID.randomUUID());
        buyOrder.setCustomerId("custA");
        buyOrder.setAssetName("STOCK");
        buyOrder.setOrderSide(SideType.BUY);
        buyOrder.setSize(BigDecimal.valueOf(10));
        buyOrder.setPrice(BigDecimal.valueOf(100));
        buyOrder.setStatus(StatusType.PENDING);
        buyOrder.setCreateDate(ZonedDateTime.now().minusMinutes(5));

        OrderEntity sellOrder = new OrderEntity();
        sellOrder.setOrderId(UUID.randomUUID());
        sellOrder.setCustomerId("custB");
        sellOrder.setAssetName("STOCK");
        sellOrder.setOrderSide(SideType.SELL);
        sellOrder.setSize(BigDecimal.valueOf(10));
        sellOrder.setPrice(BigDecimal.valueOf(100));
        sellOrder.setStatus(StatusType.PENDING);
        sellOrder.setCreateDate(ZonedDateTime.now().minusMinutes(3));

        OrderRestResponseModel matchedBuyOrderResponse = new OrderRestResponseModel();
        matchedBuyOrderResponse.setIdentifier("buy1");
        matchedBuyOrderResponse.setStatus("MATCHED");

        OrderRestResponseModel matchedSellOrderResponse = new OrderRestResponseModel();
        matchedSellOrderResponse.setIdentifier("sell1");
        matchedSellOrderResponse.setStatus("MATCHED");

        when(orderDao.getOrderByOrderIdAndStatus(buyOrder.getOrderId().toString(), StatusType.PENDING)).thenReturn(buyOrder);
        when(orderDao.getAllByStatusAndAssetNameAndSide(StatusType.PENDING, "STOCK", SideType.SELL))
                .thenReturn(Collections.singletonList(sellOrder));
        when(orderConverter.toResource(buyOrder)).thenReturn(matchedBuyOrderResponse);
        when(orderConverter.toResource(sellOrder)).thenReturn(matchedSellOrderResponse);

        ResponseEntity<OrdersRestResponseListModel> response = orderService.matchOrders(buyOrder.getOrderId().toString());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getOrderRestResponseModels().size());
        assertEquals("MATCHED", response.getBody().getOrderRestResponseModels().get(0).getStatus());
        assertEquals("MATCHED", response.getBody().getOrderRestResponseModels().get(1).getStatus());

        verify(assetDao, times(1)).increaseSizeofAsset(eq("custB"), eq(TRY_ASSET_NAME), eq(BigDecimal.valueOf(1000))); // sell cust gets TRY
        verify(assetDao, times(1)).reduceSizeofAsset(eq("custB"), eq("STOCK"), eq(BigDecimal.valueOf(10))); // sell cust reduces STOCK
        verify(assetDao, times(1)).increaseSizeofAsset(eq("custA"), eq("STOCK"), eq(BigDecimal.valueOf(10))); // buy cust gets STOCK
        verify(assetDao, times(1)).reduceSizeofAsset(eq("custA"), eq(TRY_ASSET_NAME), eq(BigDecimal.valueOf(1000))); // buy cust reduces TRY

        verify(orderDao, times(1)).updateOrderStatusWithMatched(buyOrder.getOrderId());
        verify(orderDao, times(1)).updateOrderStatusWithMatched(sellOrder.getOrderId());
    }

    @Test
    void matchOrders_shouldReturnOkAndPartiallyMatchedOrder_whenPartialMatchExists() {
        OrderEntity buyOrder = new OrderEntity();
        buyOrder.setOrderId(UUID.randomUUID());
        buyOrder.setCustomerId("custA");
        buyOrder.setAssetName("STOCK");
        buyOrder.setOrderSide(SideType.BUY);
        buyOrder.setSize(BigDecimal.valueOf(15));
        buyOrder.setPrice(BigDecimal.valueOf(100));
        buyOrder.setStatus(StatusType.PENDING);
        buyOrder.setCreateDate(ZonedDateTime.now().minusMinutes(5));

        OrderEntity sellOrder = new OrderEntity();
        sellOrder.setOrderId(UUID.randomUUID());
        sellOrder.setCustomerId("custB");
        sellOrder.setAssetName("STOCK");
        sellOrder.setOrderSide(SideType.SELL);
        sellOrder.setSize(BigDecimal.valueOf(10));
        sellOrder.setPrice(BigDecimal.valueOf(100));
        sellOrder.setStatus(StatusType.PENDING);
        sellOrder.setCreateDate(ZonedDateTime.now().minusMinutes(3));

        OrderRestResponseModel matchedSellOrderResponse = new OrderRestResponseModel();
        matchedSellOrderResponse.setIdentifier("sell1");
        matchedSellOrderResponse.setStatus("MATCHED");

        when(orderDao.getOrderByOrderIdAndStatus(buyOrder.getOrderId().toString(), StatusType.PENDING)).thenReturn(buyOrder);
        when(orderDao.getAllByStatusAndAssetNameAndSide(StatusType.PENDING, "STOCK", SideType.SELL))
                .thenReturn(Collections.singletonList(sellOrder));
        when(orderConverter.toResource(sellOrder)).thenReturn(matchedSellOrderResponse);

        ResponseEntity<OrdersRestResponseListModel> response = orderService.matchOrders(buyOrder.getOrderId().toString());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getOrderRestResponseModels().size());

        verify(assetDao, times(1)).increaseSizeofAsset(eq("custB"), eq(TRY_ASSET_NAME), eq(BigDecimal.valueOf(1000)));
        verify(assetDao, times(1)).reduceSizeofAsset(eq("custB"), eq("STOCK"), eq(BigDecimal.valueOf(10)));
        verify(assetDao, times(1)).increaseSizeofAsset(eq("custA"), eq("STOCK"), eq(BigDecimal.valueOf(10)));
        verify(assetDao, times(1)).reduceSizeofAsset(eq("custA"), eq(TRY_ASSET_NAME), eq(BigDecimal.valueOf(1000)));

        verify(orderDao, never()).updateOrderStatusWithMatched(buyOrder.getOrderId()); // Buy order is not fully matched
        verify(orderDao, times(1)).updateOrderStatusWithMatched(sellOrder.getOrderId());
        verify(orderDao, times(1)).updateOrderSize(buyOrder.getOrderId(), BigDecimal.valueOf(5)); // Remaining size
    }


    @Test
    void matchOrders_shouldReturnNotFound_whenOrderToMatchDoesNotExistOrNotPending() {
        when(orderDao.getOrderByOrderIdAndStatus("nonExistentOrder", StatusType.PENDING)).thenReturn(null);

        ResponseEntity<OrdersRestResponseListModel> response = orderService.matchOrders("nonExistentOrder");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(orderDao, never()).getAllByStatusAndAssetNameAndSide(any(), any(), any());
    }

    @Test
    void matchOrders_shouldReturnEmptyList_whenNoCounterOrdersFound() {
        when(orderDao.getOrderByOrderIdAndStatus(buyOrderEntity.getOrderId().toString(), StatusType.PENDING)).thenReturn(buyOrderEntity);
        when(orderDao.getAllByStatusAndAssetNameAndSide(StatusType.PENDING, "GOLD", SideType.SELL))
                .thenReturn(Collections.emptyList());

        ResponseEntity<OrdersRestResponseListModel> response = orderService.matchOrders(buyOrderEntity.getOrderId().toString());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getOrderRestResponseModels().size());
        verify(orderDao, never()).updateOrderStatusWithMatched(any());
        verify(orderDao, never()).updateOrderSize(any(), any());
    }

    @Test
    void matchOrders_shouldStopMatching_whenPriceNoLongerMatches() {
        OrderEntity buyOrder = new OrderEntity();
        buyOrder.setOrderId(UUID.randomUUID());
        buyOrder.setCustomerId("custA");
        buyOrder.setAssetName("STOCK");
        buyOrder.setOrderSide(SideType.BUY);
        buyOrder.setSize(BigDecimal.valueOf(20));
        buyOrder.setPrice(BigDecimal.valueOf(100));
        buyOrder.setStatus(StatusType.PENDING);
        buyOrder.setCreateDate(ZonedDateTime.now().minusMinutes(10));

        OrderEntity sellOrder1 = new OrderEntity();
        sellOrder1.setOrderId(UUID.randomUUID());
        sellOrder1.setCustomerId("custB");
        sellOrder1.setAssetName("STOCK");
        sellOrder1.setOrderSide(SideType.SELL);
        sellOrder1.setSize(BigDecimal.valueOf(5));
        sellOrder1.setPrice(BigDecimal.valueOf(90));
        sellOrder1.setStatus(StatusType.PENDING);
        sellOrder1.setCreateDate(ZonedDateTime.now().minusMinutes(8));

        OrderEntity sellOrder2 = new OrderEntity();
        sellOrder2.setOrderId(UUID.randomUUID());
        sellOrder2.setCustomerId("custC");
        sellOrder2.setAssetName("STOCK");
        sellOrder2.setOrderSide(SideType.SELL);
        sellOrder2.setSize(BigDecimal.valueOf(10));
        sellOrder2.setPrice(BigDecimal.valueOf(105));
        sellOrder2.setStatus(StatusType.PENDING);
        sellOrder2.setCreateDate(ZonedDateTime.now().minusMinutes(7));

        when(orderDao.getOrderByOrderIdAndStatus(buyOrder.getOrderId().toString(), StatusType.PENDING)).thenReturn(buyOrder);
        when(orderDao.getAllByStatusAndAssetNameAndSide(StatusType.PENDING, "STOCK", SideType.SELL))
                .thenReturn(Arrays.asList(sellOrder1, sellOrder2));

        when(orderConverter.toResource(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity arg = invocation.getArgument(0);
            OrderRestResponseModel res = new OrderRestResponseModel();
            res.setIdentifier(String.valueOf(arg.getOrderId()));
            res.setStatus(arg.getStatus().toString());
            return res;
        });

        ResponseEntity<OrdersRestResponseListModel> response = orderService.matchOrders(buyOrder.getOrderId().toString());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getOrderRestResponseModels().size());

        verify(orderDao, times(1)).updateOrderStatusWithMatched(sellOrder1.getOrderId());
        verify(orderDao, never()).updateOrderStatusWithMatched(sellOrder2.getOrderId());

        verify(orderDao, never()).updateOrderStatusWithMatched(buyOrder.getOrderId());
        verify(orderDao, times(1)).updateOrderSize(buyOrder.getOrderId(), BigDecimal.valueOf(15));
    }
}