package com.rasit.brokage.rest.security;

import com.rasit.brokage.core.data.CustomerDao;
import com.rasit.brokage.core.data.entity.CustomerEntity;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.auth.AuthRefreshRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestResponseModel;
import com.rasit.brokage.service.AuthService;
import com.rasit.brokage.utility.ErrorMessageType;
import com.rasit.brokage.utility.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CustomerDao customerDao;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private AuthRestRequestModel authRequest;
    private CustomerEntity testUser;
    private Authentication authentication;
    private String username;
    private String password;
    private String jwtToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        username = "testuser@example.com";
        password = "password123";
        jwtToken = "mockJwtToken";
        refreshToken = "mockRefreshToken";

        authRequest = new AuthRestRequestModel(username, password);

        testUser = new CustomerEntity();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername(username);
        testUser.setPassword(password);
        testUser.setRole(RoleType.CUSTOMER);

        authentication = mock(Authentication.class);
        //when(authentication.isAuthenticated()).thenReturn(true);
        //when(authentication.getPrincipal()).thenReturn(testUser);
    }

    @Test
    void authenticate_shouldReturnTokens_whenAuthenticationSuccessful() throws CustomException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password)))
                    .thenReturn(authentication);
            when(customerDao.findByUsername(username)).thenReturn(Optional.of(testUser));
            when(jwtService.generateToken(testUser)).thenReturn(jwtToken);
            when(jwtService.generateRefresh(any(HashMap.class), eq(testUser))).thenReturn(refreshToken);

            ResponseEntity<AuthRestResponseModel> response = authService.authenticate(authRequest);

            assertNotNull(response);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(jwtToken, response.getBody().getToken());
            assertEquals(refreshToken, response.getBody().getRefreshToken());

            verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
            verify(securityContext).setAuthentication(authentication);
            verify(customerDao).findByUsername(username);
            verify(jwtService).generateToken(testUser);
            verify(jwtService).generateRefresh(any(HashMap.class), eq(testUser));
        }
    }

    @Test
    void authenticate_shouldThrowCustomException_whenBadCredentials() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mock(SecurityContext.class));

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            BadCredentialsException thrown = assertThrows(BadCredentialsException.class, () ->
                    authService.authenticate(authRequest));
            assertEquals("Invalid credentials", thrown.getMessage());
            verify(customerDao, never()).findByUsername(anyString());
        }
    }

    @Test
    void authenticate_shouldThrowCustomException_whenUserNotFoundAfterAuthentication() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(customerDao.findByUsername(username)).thenReturn(Optional.empty()); // User not found

            CustomException thrown = assertThrows(CustomException.class, () ->
                    authService.authenticate(authRequest));

            assertEquals(ErrorMessageType.INVALID_CREDENTIAL_ERROR, thrown.getErrorMessageType());
            assertEquals(HttpStatus.BAD_REQUEST, thrown.getStatusCode());
            verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(username, password));
            verify(securityContext).setAuthentication(authentication);
            verify(customerDao).findByUsername(username);
            verify(jwtService, never()).generateToken(any());
        }
    }

    @Test
    void refreshToken_shouldReturnNewTokens_whenRefreshTokenIsValid() throws CustomException {
        AuthRefreshRestRequestModel refreshRequest = new AuthRefreshRestRequestModel(refreshToken);

        when(jwtService.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.getUsernameFromRefreshToken(refreshToken)).thenReturn(username);
        when(customerDao.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("newJwtToken");
        when(jwtService.generateRefresh(any(HashMap.class), eq(testUser))).thenReturn("newRefreshToken");

        ResponseEntity<AuthRestResponseModel> response = authService.refreshToken(refreshRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newJwtToken", response.getBody().getToken());
        assertEquals("newRefreshToken", response.getBody().getRefreshToken());

        verify(jwtService).validateRefreshToken(refreshToken);
        verify(jwtService).getUsernameFromRefreshToken(refreshToken);
        verify(customerDao).findByUsername(username);
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefresh(any(HashMap.class), eq(testUser));
    }

    @Test
    void refreshToken_shouldThrowCustomException_whenRefreshTokenIsInvalid() {
        AuthRefreshRestRequestModel refreshRequest = new AuthRefreshRestRequestModel("invalidToken");

        when(jwtService.validateRefreshToken("invalidToken")).thenReturn(false);

        CustomException thrown = assertThrows(CustomException.class, () ->
                authService.refreshToken(refreshRequest));

        assertEquals(ErrorMessageType.REFRESH_TOKEN_ERROR, thrown.getErrorMessageType());
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatusCode());

        verify(jwtService).validateRefreshToken("invalidToken");
        verify(jwtService, never()).getUsernameFromRefreshToken(anyString());
        verify(customerDao, never()).findByUsername(anyString());
    }

    @Test
    void refreshToken_shouldThrowCustomException_whenUserNotFoundForRefreshToken() {
        AuthRefreshRestRequestModel refreshRequest = new AuthRefreshRestRequestModel(refreshToken);

        when(jwtService.validateRefreshToken(refreshToken)).thenReturn(true);
        when(jwtService.getUsernameFromRefreshToken(refreshToken)).thenReturn(username);
        when(customerDao.findByUsername(username)).thenReturn(Optional.empty());

        CustomException thrown = assertThrows(CustomException.class, () ->
                authService.refreshToken(refreshRequest));

        assertEquals(ErrorMessageType.INVALID_CREDENTIAL_ERROR, thrown.getErrorMessageType());
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatusCode());

        verify(jwtService).validateRefreshToken(refreshToken);
        verify(jwtService).getUsernameFromRefreshToken(refreshToken);
        verify(customerDao).findByUsername(username);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refreshToken_shouldThrowCustomException_whenRefreshTokenIsNull() {
        AuthRefreshRestRequestModel refreshRequest = new AuthRefreshRestRequestModel(null);

        CustomException thrown = assertThrows(CustomException.class, () ->
                authService.refreshToken(refreshRequest));

        assertEquals(ErrorMessageType.REFRESH_TOKEN_ERROR, thrown.getErrorMessageType());
        assertEquals(HttpStatus.UNAUTHORIZED, thrown.getStatusCode());

        verify(jwtService, never()).validateRefreshToken(any());
        verify(jwtService, never()).getUsernameFromRefreshToken(anyString());
        verify(customerDao, never()).findByUsername(anyString());
    }
}