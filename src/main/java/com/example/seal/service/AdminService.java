package com.example.seal.service;

import com.example.seal.dto.CreateUserRequest;
import com.example.seal.dto.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.List;

public class AdminService {
    private static final String USERS_URL=
            "http://localhost:8080/api/admin/users";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AdminService(){
        this.httpClient=HttpClient.newHttpClient();
        this.objectMapper=new ObjectMapper();
    }

    public UserResponse createUser(
            CreateUserRequest createUserRequest,
            String token
    ) throws Exception{
        String requestJson = objectMapper.writeValueAsString(createUserRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL))
                .header("Content-Type","application/json")
                .header("Authorization","Bearer "+token)
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        if(response.statusCode()==201){
            return objectMapper.readValue(
                    response.body(),
                    UserResponse.class
            );
        }
        throw new IllegalArgumentException(response.body());
    }

    public List<UserResponse> getUsers(String token) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERS_URL))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        if(response.statusCode()!=200){
            throw new IllegalArgumentException(response.body());
        }
        UserResponse[] users = objectMapper.readValue(
                response.body(),
                UserResponse[].class
        );
        return Arrays.asList(users);
    }
}
