package com.example.seal.service;

public class AuthService {
    public boolean authenticate
        (String username, String password, String role){
            if(role.equals("STUDENT")){
                return username.equals("student") && password.equals("student123");
            }
            if(role.equals("TEACHER")){
                return username.equals("teacher") && password.equals("teacher123");
            }
            return false;
        }
    }

