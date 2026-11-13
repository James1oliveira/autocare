package com.jamesoliveira.autocare.auth.web;

import com.jamesoliveira.autocare.auth.user.User;

public record UserResponse(Long id, String fullName, String email, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
