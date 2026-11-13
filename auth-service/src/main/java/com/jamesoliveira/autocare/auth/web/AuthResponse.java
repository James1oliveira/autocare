package com.jamesoliveira.autocare.auth.web;

public record AuthResponse(String token, UserResponse user) {
}
