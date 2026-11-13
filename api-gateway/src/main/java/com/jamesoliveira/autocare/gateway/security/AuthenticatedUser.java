package com.jamesoliveira.autocare.gateway.security;

public record AuthenticatedUser(String id, String name, String role) {
}
