package com.rasit.brokage.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rasit.brokage.core.data.CustomerDao;
import com.rasit.brokage.core.data.entity.CustomerEntity;
import com.rasit.brokage.rest.resource.auth.AuthRefreshRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestResponseModel;
import com.rasit.brokage.utility.ApiPathValues;
import com.rasit.brokage.utility.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerMvcIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String BASE_AUTH_URL = ApiPathValues.BASE_V1 + ApiPathValues.AUTH_ENDPOINT;
    private final String LOGIN_URL = BASE_AUTH_URL + ApiPathValues.LOGIN;
    private final String REFRESH_URL = LOGIN_URL + ApiPathValues.RELOAD;

    private String username;
    private String testUserPassword;

    @BeforeEach
    void setUp() {
        username = "testuser";
        testUserPassword = "password123";

        CustomerEntity customer1 = new CustomerEntity();
        customer1.setUsername(username);
        customer1.setPassword(passwordEncoder.encode(testUserPassword));
        customer1.setRole(RoleType.CUSTOMER);
        customerDao.save(customer1);
    }

    @Test
    void login_shouldReturnAuthTokens_forValidCredentials() throws Exception {
        AuthRestRequestModel request = new AuthRestRequestModel(username, testUserPassword);

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.token", notNullValue())).andExpect(jsonPath("$.refreshToken", notNullValue())).andReturn();
    }

    @Test
    void login_shouldReturnUnauthorized_forInvalidCredentials() throws Exception {
        AuthRestRequestModel request = new AuthRestRequestModel("wronguser@example.com", "wrongpassword");

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.text", not(emptyString()))); // Expect an error message
    }

    @Test
    void login_shouldReturnBadRequest_forMissingFields() throws Exception {
        AuthRestRequestModel request = new AuthRestRequestModel(username, null);

        mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.text", not(emptyString())));
    }

    //@Test
    void refreshToken_shouldReturnNewAuthTokens_forValidRefreshToken() throws Exception {
        AuthRestRequestModel loginRequest = new AuthRestRequestModel(username, testUserPassword);
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isCreated()).andReturn();

        AuthRestResponseModel loginResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthRestResponseModel.class);
        String refreshToken = loginResponse.getRefreshToken();

        // Now use the refresh token
        AuthRefreshRestRequestModel refreshRequest = new AuthRefreshRestRequestModel(refreshToken);

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(refreshRequest))).andExpect(status().isCreated()).andExpect(jsonPath("$.token", notNullValue())).andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void refreshToken_shouldReturnUnauthorized_forInvalidRefreshToken() throws Exception {
        AuthRefreshRestRequestModel request = new AuthRefreshRestRequestModel("invalid_refresh_token");

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_shouldReturnBadRequest_forMissingRefreshToken() throws Exception {
        AuthRefreshRestRequestModel request = new AuthRefreshRestRequestModel(null);

        mockMvc.perform(post(REFRESH_URL).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))).andExpect(status().isBadRequest()).andExpect(jsonPath("$.text", not(emptyString())));
    }
}