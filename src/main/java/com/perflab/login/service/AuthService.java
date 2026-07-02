package com.perflab.login.service;

import com.perflab.login.dto.LoginRequest;
import com.perflab.login.dto.LoginResponse;
import com.perflab.login.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "password";

    private final JwtService jwtService;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (!isValidUser(request)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return new LoginResponse(jwtService.generateToken(request.getUsername()), jwtService.getExpirationSeconds());
    }

    private boolean isValidUser(LoginRequest request) {
        return request != null
                && VALID_USERNAME.equals(request.getUsername())
                && VALID_PASSWORD.equals(request.getPassword());
    }
}
