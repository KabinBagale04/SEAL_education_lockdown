package com.example.seal.service;

import com.example.seal.dto.LoginRequest;
import com.example.seal.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.AreaAveragingScaleFilter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {
    private static final String LOGIN_URL=  "http://localhost:8080/api/auth/login";

    // is the things that sends http requests
    private final HttpClient httpClient;
    // is jackson's main json conversion object
    //object mapper le java object lai json ma change garx and vice versa
    private final ObjectMapper objectMapperl;

    public AuthService(){
        this.httpClient=HttpClient.newHttpClient();
        this.objectMapperl=new ObjectMapper();
    }

    public LoginResponse authenticate(String username, String password) throws Exception{
        LoginRequest loginRequest = new LoginRequest(username,password);
        String requestJson = objectMapperl.writeValueAsString(loginRequest);


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LOGIN_URL))
                .header("Content-Type","application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request,HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        return objectMapperl.readValue(response.body(),LoginResponse.class);

    }

    public void logout(String token) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/logout"))
                .header("Authorization","Bearer"+token)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = httpClient.send(request,HttpResponse.BodyHandlers.discarding());

        if(response.statusCode() !=204){
            throw new RuntimeException("Logout failed with HTTP"+response.statusCode());
        }
    }
}
