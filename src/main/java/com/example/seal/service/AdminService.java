package com.example.seal.service;
import com.example.seal.dto.*;
import java.util.*;

public class AdminService {
    private final ApiClient api = new ApiClient();
    public UserResponse createUser(CreateUserRequest request, String token) throws Exception {
        return ApiClient.JSON.treeToValue(api.request("POST", "/api/admin/users", request, token), UserResponse.class);
    }
    public List<UserResponse> getUsers(String token) throws Exception {
        return Arrays.asList(ApiClient.JSON.treeToValue(api.request("GET", "/api/admin/users", null, token), UserResponse[].class));
    }
    public UserResponse setActive(Long id, boolean active, String token) throws Exception {
        return ApiClient.JSON.treeToValue(api.request("PATCH", "/api/admin/users/" + id + "/active",
                Map.of("active", active), token), UserResponse.class);
    }
}
