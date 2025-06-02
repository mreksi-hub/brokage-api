package com.rasit.brokage.rest.resource.auth;


import com.rasit.brokage.rest.resource.BaseRestModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRestResponseModel implements BaseRestModel {
    private String token;
    private String refreshToken;
}