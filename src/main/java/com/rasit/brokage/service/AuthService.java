package com.rasit.brokage.service;

import com.rasit.brokage.core.data.CustomerDao;
import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.auth.AuthRefreshRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestResponseModel;
import com.rasit.brokage.rest.security.JwtService;
import com.rasit.brokage.utility.ErrorMessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service

public class AuthService {
    private final CustomerDao customerDao;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(CustomerDao customerDao, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.customerDao = customerDao;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public ResponseEntity<AuthRestResponseModel> authenticate(AuthRestRequestModel authenticationRequest) throws CustomException {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = customerDao.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> new CustomException(ErrorMessageType.INVALID_CREDENTIAL_ERROR,
                new String[]{},
                HttpStatus.BAD_REQUEST));
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefresh(new HashMap<>(), user);
        return new ResponseEntity<>(AuthRestResponseModel.builder().token(jwtToken).refreshToken(refreshToken).build(), HttpStatus.CREATED);
    }

    public ResponseEntity<AuthRestResponseModel> refreshToken(AuthRefreshRestRequestModel refreshTokenRequest) throws CustomException {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            String username = jwtService.getUsernameFromRefreshToken(refreshToken);
            var user = customerDao.findByUsername(username).orElseThrow(() -> new CustomException(ErrorMessageType.INVALID_CREDENTIAL_ERROR,
                    new String[]{},
                    HttpStatus.UNAUTHORIZED));

            String newToken = jwtService.generateToken(user);
            var newRefreshToken = jwtService.generateRefresh(new HashMap<>(), user);

            return new ResponseEntity<>(AuthRestResponseModel.builder().token(newToken).refreshToken(newRefreshToken).build(), HttpStatus.CREATED);
        }
        throw new CustomException(ErrorMessageType.REFRESH_TOKEN_ERROR,
                new String[]{},
                HttpStatus.UNAUTHORIZED);
    }
}