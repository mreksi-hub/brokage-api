package com.rasit.brokage.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.CustomerDao;
import com.rasit.brokage.core.data.OrderDao;
import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.core.data.entity.CustomerEntity;
import com.rasit.brokage.core.data.entity.OrderEntity;
import com.rasit.brokage.rest.resource.auth.AuthRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestResponseModel;
import com.rasit.brokage.rest.resource.order.OrderRestRequestModel;
import com.rasit.brokage.rest.security.UserDetailsImpl;
import com.rasit.brokage.utility.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerMvcIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private AssetDao assetDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String BASE_ORDER_URL = ApiPathValues.BASE_V1 + ApiPathValues.ORDER_ENDPOINT;
    private final String LIST_ORDER_URL = BASE_ORDER_URL + ApiPathValues.LIST_ENDPOINT;
    private final String MATCH_ORDER_URL = BASE_ORDER_URL + ApiPathValues.MATCH_ENDPOINT;

    private UUID customer1Id;
    private UUID customer2Id;
    private UUID adminId;
    private UserDetailsImpl userDetailsCustomer1;
    private UserDetailsImpl userDetailsAdmin;
    private String customer1Jwt;
    private String adminJwt;

    private AssetEntity customer1Asset;
    private AssetEntity customer2Asset;

    @BeforeEach
    void setUp() throws Exception {
        customerDao.deleteAll();
        assetDao.deleteAll();
        orderDao.deleteAll();

        CustomerEntity user1 = new CustomerEntity();
        user1.setFirstName("customer1");
        user1.setLastName("customer1");
        user1.setUsername("customer1");
        user1.setPassword(passwordEncoder.encode("pass1"));
        user1.setRole(RoleType.CUSTOMER);
        user1 = customerDao.save(user1);
        customer1Id = user1.getId();

        CustomerEntity user2 = new CustomerEntity();
        user2.setFirstName("customer2");
        user2.setLastName("customer2");
        user2.setUsername("customer2");
        user2.setPassword(passwordEncoder.encode("pass2"));
        user2.setRole(RoleType.CUSTOMER);
        user2 = customerDao.save(user2);
        customer2Id = user2.getId();

        CustomerEntity adminUser = new CustomerEntity();
        adminUser.setFirstName("admin");
        adminUser.setLastName("admin");
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("adminpass"));
        adminUser.setRole(RoleType.ADMIN);
        adminUser = customerDao.save(adminUser);
        adminId = adminUser.getId();


        userDetailsCustomer1 = new UserDetailsImpl(user1.getId(), "customer1", "pass1", Collections.singleton(new SimpleGrantedAuthority("CUSTOMER")));
        userDetailsAdmin = new UserDetailsImpl(adminUser.getId(), "admin", "adminpass", Collections.singleton(new SimpleGrantedAuthority("ADMIN")));

        customer1Jwt = obtainJwtToken("customer1", "pass1");
        adminJwt = obtainJwtToken("admin", "adminpass");

        customer1Asset = new AssetEntity();
        customer1Asset.setCustomerId(customer1Id.toString());
        customer1Asset.setAssetName("GOLD");
        customer1Asset.setSize(BigDecimal.valueOf(100L));
        customer1Asset.setUsableSize(BigDecimal.valueOf(100L));
        assetDao.save(customer1Asset);

        customer2Asset = new AssetEntity();
        customer2Asset.setCustomerId(customer2Id.toString());
        customer2Asset.setAssetName("SILVER");
        customer2Asset.setSize(BigDecimal.valueOf(50L));
        customer2Asset.setUsableSize(BigDecimal.valueOf(50L));
        assetDao.save(customer2Asset);

        AssetEntity customer1Try = new AssetEntity();
        customer1Try.setCustomerId(customer1Id.toString());
        customer1Try.setAssetName("TRY");
        customer1Try.setSize(BigDecimal.valueOf(500000L));
        customer1Try.setUsableSize(BigDecimal.valueOf(500000L));
        assetDao.save(customer1Try);


        AssetEntity customer2Try = new AssetEntity();
        customer2Try.setCustomerId(customer2Id.toString());
        customer2Try.setAssetName("TRY");
        customer2Try.setSize(BigDecimal.valueOf(500000L));
        customer2Try.setUsableSize(BigDecimal.valueOf(500000L));
        assetDao.save(customer2Try);
    }

    private String obtainJwtToken(String email, String password) throws Exception {
        AuthRestRequestModel authRequest = new AuthRestRequestModel(email, password);
        MvcResult result = mockMvc.perform(post(ApiPathValues.BASE_V1 + ApiPathValues.AUTH_ENDPOINT + ApiPathValues.LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthRestResponseModel authResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AuthRestResponseModel.class);
        return authResponse.getToken();
    }

    @Test
    void createOrder_shouldReturnCreatedOrder_forValidRequest() throws Exception {
        OrderRestRequestModel request = new OrderRestRequestModel();
        request.setAssetName("GOLD");
        request.setOrderSide(SideType.BUY);
        request.setSize(BigDecimal.valueOf(10));
        request.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identifier", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customer1Id.toString())))
                .andExpect(jsonPath("$.assetName", is("GOLD")))
                .andExpect(jsonPath("$.orderSide", is("BUY")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void createOrder_shouldReturnNotFound_forNonExistentAsset() throws Exception {
        OrderRestRequestModel request = new OrderRestRequestModel();
        request.setAssetName("NON_EXISTENT_ASSET");
        request.setOrderSide(SideType.BUY);
        request.setSize(BigDecimal.valueOf(10));
        request.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.subErrors[*].subText", notNullValue()));
    }

    @Test
    void createOrder_shouldReturnOwnOrder_whenCustomerTriesToCreateForAnotherCustomer() throws Exception {
        OrderRestRequestModel request = new OrderRestRequestModel();
        request.setAssetName("GOLD");
        request.setOrderSide(SideType.BUY);
        request.setSize(BigDecimal.valueOf(10));
        request.setPrice(BigDecimal.valueOf(100));

        mockMvc.perform(post(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .header(BrokageConstants.X_CUSTOMER_ID, customer2Id) // Customer trying to set another customerId
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId", is(customer1Id.toString())));
    }

    @Test
    void createOrder_shouldAllowAdminToCreateForAnotherCustomer() throws Exception {
        OrderRestRequestModel request = new OrderRestRequestModel();
        request.setAssetName("SILVER");
        request.setOrderSide(SideType.BUY);
        request.setSize(BigDecimal.valueOf(5));
        request.setPrice(BigDecimal.valueOf(50));

        mockMvc.perform(post(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + adminJwt)
                        .with(user(userDetailsAdmin))
                        .header(BrokageConstants.X_CUSTOMER_ID, customer2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identifier", notNullValue()))
                .andExpect(jsonPath("$.customerId", is(customer2Id.toString())))
                .andExpect(jsonPath("$.assetName", is("SILVER")));
    }

    @Test
    void getOrders_shouldReturnOrdersForAuthenticatedCustomer() throws Exception {
        OrderEntity order1 = new OrderEntity();
        order1.setCustomerId(customer1Id.toString());
        order1.setAssetName("GOLD");
        order1.setOrderSide(SideType.BUY);
        order1.setSize(BigDecimal.valueOf(10L));
        order1.setPrice(BigDecimal.valueOf(100));
        order1.setStatus(StatusType.PENDING);
        order1.setCreateDate(ZonedDateTime.now().minusDays(5));
        orderDao.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setCustomerId(customer1Id.toString());
        order2.setAssetName("GOLD");
        order2.setOrderSide(SideType.SELL);
        order2.setSize(BigDecimal.valueOf(5L));
        order2.setPrice(BigDecimal.valueOf(110));
        order2.setStatus(StatusType.PENDING);
        order2.setCreateDate(ZonedDateTime.now().minusDays(1));
        orderDao.save(order2);

        String startDate = ZonedDateTime.now().minusDays(7).format(BrokageUtil.timeFormatter);
        String endDate = ZonedDateTime.now().plusDays(1).format(BrokageUtil.timeFormatter);

        mockMvc.perform(get(LIST_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param(BrokageConstants.PAGE_NUMBER, "0")
                        .param(BrokageConstants.PAGE_SIZE, "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderRestResponseModels", hasSize(2)))
                .andExpect(jsonPath("$.orderRestResponseModels[0].customerId", is(customer1Id.toString())))
                .andExpect(jsonPath("$.orderRestResponseModels[1].customerId", is(customer1Id.toString())));
    }

    @Test
    void getOrders_shouldReturnBadRequest_forInvalidDateFormat() throws Exception {
        String invalidStartDate = "2025-06-01";
        String endDate = ZonedDateTime.now().plusDays(1).format(BrokageUtil.timeFormatter);

        mockMvc.perform(get(LIST_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param("startDate", invalidStartDate)
                        .param("endDate", endDate)
                        .param(BrokageConstants.PAGE_NUMBER, "0")
                        .param(BrokageConstants.PAGE_SIZE, "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[*].subText", hasItem(is("Invalid startDate format. Expected: yyyy-MM-dd'T'HH:mm:ssZ"))));
    }

    @Test
    void getOrders_shouldReturnBadRequest_forPageSizeViolation() throws Exception {
        String startDate = ZonedDateTime.now().minusDays(7).format(BrokageUtil.timeFormatter);
        String endDate = ZonedDateTime.now().plusDays(1).format(BrokageUtil.timeFormatter);

        mockMvc.perform(get(LIST_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .param(BrokageConstants.PAGE_NUMBER, "0")
                        .param(BrokageConstants.PAGE_SIZE, "501")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors[*].subText", hasItem(is(BrokageConstants.PAGE_SIZE_MAX_SIZE_VIOLATION))));
    }

    @Test
    void deleteOrder_shouldReturn_forCanceledOrder() throws Exception {
        OrderEntity orderToCancel = new OrderEntity();
        orderToCancel.setCustomerId(customer1Id.toString());
        orderToCancel.setAssetName("GOLD");
        orderToCancel.setOrderSide(SideType.BUY);
        orderToCancel.setSize(BigDecimal.valueOf(10L));
        orderToCancel.setPrice(BigDecimal.valueOf(100));
        orderToCancel.setStatus(StatusType.PENDING);
        orderToCancel.setCreateDate(ZonedDateTime.now());
        orderToCancel = orderDao.save(orderToCancel);

        mockMvc.perform(delete(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param(ApiPathValues.ORDERID, orderToCancel.getOrderId().toString()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(LIST_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param("startDate", ZonedDateTime.now().minusDays(1).format(BrokageUtil.timeFormatter))
                        .param("endDate", ZonedDateTime.now().plusDays(1).format(BrokageUtil.timeFormatter))
                        .param(BrokageConstants.PAGE_NUMBER, "0")
                        .param(BrokageConstants.PAGE_SIZE, "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderRestResponseModels", hasSize(1)))
                .andExpect(jsonPath("$.orderRestResponseModels[0].status", is("CANCELED")));;
    }

    @Test
    void deleteOrder_shouldReturnNotFound_whenCustomerDeletesAnotherCustomersOrder() throws Exception {
        OrderEntity orderToCancel = new OrderEntity();
        orderToCancel.setCustomerId(customer2Id.toString());
        orderToCancel.setAssetName("SILVER");
        orderToCancel.setOrderSide(SideType.BUY);
        orderToCancel.setSize(BigDecimal.valueOf(1L));
        orderToCancel.setPrice(BigDecimal.valueOf(10));
        orderToCancel.setStatus(StatusType.PENDING);
        orderToCancel.setCreateDate(ZonedDateTime.now());
        orderToCancel = orderDao.save(orderToCancel);

        mockMvc.perform(delete(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param(ApiPathValues.ORDERID, orderToCancel.getOrderId().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrder_shouldAllowAdminToDeleteAnyOrder() throws Exception {
        OrderEntity orderToCancel = new OrderEntity();
        orderToCancel.setCustomerId(customer2Id.toString());
        orderToCancel.setAssetName("SILVER");
        orderToCancel.setOrderSide(SideType.BUY);
        orderToCancel.setSize(BigDecimal.valueOf(1L));
        orderToCancel.setPrice(BigDecimal.valueOf(10));
        orderToCancel.setStatus(StatusType.PENDING);
        orderToCancel.setCreateDate(ZonedDateTime.now());
        orderToCancel = orderDao.save(orderToCancel);

        mockMvc.perform(delete(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + adminJwt)
                        .with(user(userDetailsAdmin))
                        .param(ApiPathValues.ORDERID, String.valueOf(orderToCancel.getOrderId())))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_shouldReturnNoContent_forNonPendingOrder() throws Exception {
        OrderEntity orderToCancel = new OrderEntity();
        orderToCancel.setCustomerId(String.valueOf(customer1Id));
        orderToCancel.setAssetName("GOLD");
        orderToCancel.setOrderSide(SideType.BUY);
        orderToCancel.setSize(BigDecimal.valueOf(10L));
        orderToCancel.setPrice(BigDecimal.valueOf(100));
        orderToCancel.setStatus(StatusType.MATCHED);
        orderToCancel.setCreateDate(ZonedDateTime.now());
        orderToCancel = orderDao.save(orderToCancel);

        mockMvc.perform(delete(BASE_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param(ApiPathValues.ORDERID, orderToCancel.getOrderId().toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void matchOrders_shouldReturnForbidden_whenNotAdmin() throws Exception {
        OrderEntity pendingOrder = new OrderEntity();
        pendingOrder.setCustomerId(customer1Id.toString());
        pendingOrder.setAssetName("GOLD");
        pendingOrder.setOrderSide(SideType.BUY);
        pendingOrder.setSize(BigDecimal.valueOf(10L));
        pendingOrder.setPrice(BigDecimal.valueOf(100));
        pendingOrder.setStatus(StatusType.PENDING);
        pendingOrder.setCreateDate(ZonedDateTime.now());
        pendingOrder = orderDao.save(pendingOrder);

        mockMvc.perform(post(MATCH_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + customer1Jwt)
                        .with(user(userDetailsCustomer1))
                        .param(ApiPathValues.ORDERID, pendingOrder.getOrderId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void matchOrders_shouldReturnNotFound_forNonPendingOrder() throws Exception {
        OrderEntity matchedOrder = new OrderEntity();
        matchedOrder.setCustomerId(customer1Id.toString());
        matchedOrder.setAssetName("GOLD");
        matchedOrder.setOrderSide(SideType.BUY);
        matchedOrder.setSize(BigDecimal.valueOf(10L));
        matchedOrder.setPrice(BigDecimal.valueOf(100));
        matchedOrder.setStatus(StatusType.MATCHED);
        matchedOrder.setCreateDate(ZonedDateTime.now());
        matchedOrder = orderDao.save(matchedOrder);

        mockMvc.perform(post(MATCH_ORDER_URL)
                        .header(BrokageConstants.AUTHORIZATION_HEADER, BrokageConstants.BEARER_TOKEN_PREFIX + adminJwt)
                        .with(user(userDetailsAdmin))
                        .param(ApiPathValues.ORDERID, matchedOrder.getOrderId().toString()))
                .andExpect(status().isNotFound());
    }
}