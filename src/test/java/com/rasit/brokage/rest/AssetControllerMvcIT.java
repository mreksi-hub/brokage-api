package com.rasit.brokage.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasit.brokage.core.data.AssetDao;
import com.rasit.brokage.core.data.CustomerDao;
import com.rasit.brokage.core.data.entity.AssetEntity;
import com.rasit.brokage.core.data.entity.CustomerEntity;
import com.rasit.brokage.rest.security.UserDetailsImpl;
import com.rasit.brokage.utility.ApiPathValues;
import com.rasit.brokage.utility.BrokageConstants;
import com.rasit.brokage.utility.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static com.rasit.brokage.utility.ErrorMessageType.X_CUSTOMER_HEADER_NOT_FOUND;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AssetControllerMvcIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AssetDao assetDao;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String BASE_URL = ApiPathValues.BASE_V1 + ApiPathValues.ASSET_ENDPOINT + ApiPathValues.LIST_ENDPOINT;

    private UUID customerId1;
    private UUID customerId2;
    private UserDetailsImpl userDetailsCustomer1;
    private UserDetailsImpl userDetailsAdmin;


    @BeforeEach
    void setUp() {

        CustomerEntity customer1 = new CustomerEntity();
        customer1.setFirstName("user1");
        customer1.setLastName("user1");
        customer1.setUsername("user1");
        customer1.setPassword(passwordEncoder.encode("password"));
        customer1.setRole(RoleType.CUSTOMER);
        customer1 = customerDao.save(customer1);

        CustomerEntity customer2 = new CustomerEntity();
        customer2.setFirstName("user2");
        customer2.setLastName("user2");
        customer2.setUsername("user2");
        customer2.setPassword(passwordEncoder.encode("password"));
        customer2.setRole(RoleType.ADMIN);
        customer2 = customerDao.save(customer2);

        customerId1 = customer1.getId();
        customerId2 = customer2.getId();

        userDetailsCustomer1 = new UserDetailsImpl(customerId1, "user1@example.com", "password", Collections.singleton(new SimpleGrantedAuthority("CUSTOMER")));
        userDetailsAdmin = new UserDetailsImpl(customerId2, "admin@example.com", "password", Collections.singleton(new SimpleGrantedAuthority("ADMIN")));


        // Populate some test data
        AssetEntity asset1 = new AssetEntity();
        asset1.setCustomerId(customerId1.toString());
        asset1.setAssetName("Customer1's Stock A");
        asset1.setUsableSize(BigDecimal.valueOf(10));
        asset1.setSize(BigDecimal.valueOf(10));
        assetDao.save(asset1);

        AssetEntity asset2 = new AssetEntity();
        asset2.setCustomerId(customerId1.toString());
        asset2.setAssetName("Customer1's Bond B");
        asset2.setUsableSize(BigDecimal.valueOf(10));
        asset2.setSize(BigDecimal.valueOf(10));
        assetDao.save(asset2);

        AssetEntity asset3 = new AssetEntity();
        asset3.setCustomerId(customerId2.toString());
        asset3.setAssetName("Customer2's Crypto C");
        asset3.setUsableSize(BigDecimal.valueOf(10));
        asset3.setSize(BigDecimal.valueOf(10));
        assetDao.save(asset3);
    }

    @Test
    @WithMockUser
    void getAssets_shouldReturnAssetsForAuthenticatedCustomer() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsCustomer1)).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.assetRestResponseModels", hasSize(2))).andExpect(jsonPath("$.assetRestResponseModels[0].assetName", anyOf(is("Customer1's Stock A"), is("Customer1's Bond B")))).andExpect(jsonPath("$.assetRestResponseModels[1].assetName", anyOf(is("Customer1's Stock A"), is("Customer1's Bond B")))).andExpect(jsonPath("$.page_count", is(1))).andExpect(jsonPath("$.page_number", is(0))).andExpect(jsonPath("$.page_size", is(10))).andExpect(jsonPath("$.total_records", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssets_shouldReturnAssetsForAdminWithCustomerIdHeader() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsAdmin)).header(BrokageConstants.X_CUSTOMER_ID, customerId2).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.assetRestResponseModels", hasSize(1))).andExpect(jsonPath("$.assetRestResponseModels[0].assetName", is("Customer2's Crypto C"))).andExpect(jsonPath("$.page_count", is(1))).andExpect(jsonPath("$.page_number", is(0))).andExpect(jsonPath("$.page_size", is(10))).andExpect(jsonPath("$.total_records", is(1)));
    }

    @Test
    @WithMockUser
    void getAssets_shouldReturnEmptyList_whenNoAssetsForCustomer() throws Exception {
        UUID nonExistentCustomerId = UUID.randomUUID();
        UserDetailsImpl userDetailsNonExistent = new UserDetailsImpl(nonExistentCustomerId, "noassets@example.com", "password", Collections.singleton(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get(BASE_URL).with(user(userDetailsNonExistent)).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.assetRestResponseModels", hasSize(0))).andExpect(jsonPath("$.page_count", is(0))).andExpect(jsonPath("$.page_number", is(0))).andExpect(jsonPath("$.page_size", is(0))).andExpect(jsonPath("$.total_records", is(0)));
    }

    @Test
    void getAssets_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get(BASE_URL).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getAssets_shouldReturnBadRequest_forInvalidPageNumber() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsCustomer1)).param(BrokageConstants.PAGE_NUMBER, "-1").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.subErrors[*].subText", hasItem(is(BrokageConstants.PAGE_NUMBER_MIN_SIZE_VIOLATION))));
    }

    @Test
    @WithMockUser
    void getAssets_shouldReturnBadRequest_forInvalidPageSize() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsCustomer1)).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "501").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.subErrors[*].subText", hasItem(is(BrokageConstants.PAGE_SIZE_MAX_SIZE_VIOLATION))));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAssets_shouldReturnOwnList_whenCustomerAttemptsToUseCustomerIdHeader() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsCustomer1)).header(BrokageConstants.X_CUSTOMER_ID, customerId2).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.assetRestResponseModels", hasSize(2))).andExpect(jsonPath("$.assetRestResponseModels[0].assetName", is("Customer1's Stock A")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAssets_shouldReturnBadRequestForAdmin_whenCustomerIdHeaderIsNull() throws Exception {
        mockMvc.perform(get(BASE_URL).with(user(userDetailsAdmin)).param(BrokageConstants.PAGE_NUMBER, "0").param(BrokageConstants.PAGE_SIZE, "10").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.text", is(X_CUSTOMER_HEADER_NOT_FOUND.getText())));

    }

}