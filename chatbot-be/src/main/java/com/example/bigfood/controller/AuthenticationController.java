package com.example.bigfood.controller;

import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bigfood.dto.request.AuthenticationRequest;
import com.example.bigfood.dto.request.IntrospectRequest;
import com.example.bigfood.dto.response.ApiResponse;
import com.example.bigfood.dto.response.AuthenticationResponse;
import com.example.bigfood.dto.response.IntrospectResponse;
import com.example.bigfood.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

@RestController
@RequestMapping("/api/auth")

public class AuthenticationController {
    @Autowired
    AuthenticationService authenticationService;

    @PostMapping
    public ApiResponse<AuthenticationResponse> authenticated(@RequestBody AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> apiResponse = new ApiResponse<>();
        apiResponse.setResults(authenticationService.authenticated(request));
        return apiResponse;
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .results(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Jwt jwt) throws ParseException, JOSEException {
        String token = jwt.getTokenValue();
        System.out.println(token);
        authenticationService.logout(token);
        return ApiResponse.<Void>builder()
                .message("Logout successfully.")
                .build();
    }
}
