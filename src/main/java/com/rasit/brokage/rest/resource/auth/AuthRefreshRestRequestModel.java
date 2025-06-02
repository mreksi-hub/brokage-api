package com.rasit.brokage.rest.resource.auth;

import com.rasit.brokage.rest.resource.BaseRestModel;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRefreshRestRequestModel implements BaseRestModel {
    @NotBlank
    private String refreshToken;
}