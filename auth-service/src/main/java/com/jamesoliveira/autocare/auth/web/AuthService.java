package com.jamesoliveira.autocare.auth.web;

import com.jamesoliveira.autocare.auth.common.ApiException;
import com.jamesoliveira.autocare.auth.security.JwtService;
import com.jamesoliveira.autocare.auth.security.LoginAttemptService;
import com.jamesoliveira.autocare.auth.user.Role;
import com.jamesoliveira.autocare.auth.user.User;
import com.jamesoliveira.autocare.auth.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttempts;

    public AuthService(UserRepository users,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       LoginAttemptService loginAttempts) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttempts = loginAttempts;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        // New sign-ups are always customers. Mechanic/admin accounts can't be self-assigned.
        User user = users.save(new User(
                request.fullName().trim(),
                request.email().trim(),
                passwordEncoder.encode(request.password()),
                Role.CUSTOMER));
        return new AuthResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim();
        if (loginAttempts.isLocked(email)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Please try again in a few minutes.");
        }

        User user = users.findByEmailIgnoreCase(email).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            loginAttempts.recordFailure(email);
            // Same message for "no such user" and "wrong password", so attackers can't discover emails
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        loginAttempts.recordSuccess(email);
        return new AuthResponse(jwtService.createToken(user), UserResponse.from(user));
    }

    public UserResponse me(Long userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
