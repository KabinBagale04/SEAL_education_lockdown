package com.example.seal.service;
import com.example.seal.dto.*;
import java.util.Map;

public class AuthService {
    private final ApiClient api = new ApiClient();
    public LoginResponse authenticate(String username, String password) throws Exception {
        return ApiClient.JSON.treeToValue(api.request("POST", "/api/auth/login",
                new LoginRequest(username, password), null), LoginResponse.class);
    }
    public void logout(String token) throws Exception { api.request("POST", "/api/auth/logout", null, token); }
}
