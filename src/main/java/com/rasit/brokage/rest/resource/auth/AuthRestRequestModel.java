package com.rasit.brokage.rest.resource.auth;

import com.rasit.brokage.rest.resource.BaseRestModel;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRestRequestModel implements BaseRestModel {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}