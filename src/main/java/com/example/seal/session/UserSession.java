package com.example.seal.session;

public final class UserSession {
    private static Long userId;
    private static String fullName;
    private static String role;
    private static String token;

    private UserSession(){
    }
    public static void start(Long userId,
                             String fullName,
                             String role,
                             String token){
        UserSession.userId = userId;
        UserSession.fullName=fullName;
        UserSession.role=role;
        UserSession.token=token;
    }

    public static Long getUserId() {
        return userId;
    }

    public static String getFullName() {
        return fullName;
    }

    public static String getRole() {
        return role;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {
        userId = null;
        fullName = null;
        role = null;
        token = null;
    }
}
