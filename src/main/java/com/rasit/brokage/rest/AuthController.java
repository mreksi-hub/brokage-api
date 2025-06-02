package com.rasit.brokage.rest;

import com.rasit.brokage.rest.exception.CustomException;
import com.rasit.brokage.rest.resource.RestErrorResponseModel;
import com.rasit.brokage.rest.resource.auth.AuthRefreshRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestRequestModel;
import com.rasit.brokage.rest.resource.auth.AuthRestResponseModel;
import com.rasit.brokage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.rasit.brokage.utility.ApiPathValues.*;

@Validated
@RestController
@RequestMapping(BASE_V1 + AUTH_ENDPOINT)
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Returns a JWT after a successful login")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "JWT is created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AuthRestResponseModel.class), examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "400", description = "Asset not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponseModel.class))})})
    @PostMapping(LOGIN)
    public ResponseEntity<AuthRestResponseModel> login(@Valid @RequestBody AuthRestRequestModel request) throws CustomException {
        return authService.authenticate(request);
    }

    @Operation(summary = "Refresh JWT")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "JWT and Refresh JWT are created.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = AuthRestResponseModel.class), examples = {@ExampleObject(value = "")})}), @ApiResponse(responseCode = "401"), @ApiResponse(responseCode = "400", description = "Asset not found.", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = RestErrorResponseModel.class))})})
    @PostMapping(LOGIN + RELOAD)
    public ResponseEntity<AuthRestResponseModel> refresh(@Valid @RequestBody AuthRefreshRestRequestModel request) throws

            CustomException {
        return authService.refreshToken(request);
    }
}
