package com.jamesoliveira.autocare.auth.config;

import com.jamesoliveira.autocare.auth.user.Role;
import com.jamesoliveira.autocare.auth.user.User;
import com.jamesoliveira.autocare.auth.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates demo accounts so visitors can try every role straight away.
 * The customer is created first so it gets id 1 on a fresh database
 * (booking-service seeds a few example bookings for that id).
 */
@Component
@ConditionalOnProperty(name = "app.seed-demo-users", havingValue = "true", matchIfMissing = true)
public class DemoUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        create("Demo Customer", "customer@autocare.demo", "Customer123!", Role.CUSTOMER);
        create("Demo Mechanic", "mechanic@autocare.demo", "Mechanic123!", Role.MECHANIC);
        create("Demo Admin", "admin@autocare.demo", "Admin123!", Role.ADMIN);
    }

    private void create(String name, String email, String password, Role role) {
        if (!users.existsByEmailIgnoreCase(email)) {
            users.save(new User(name, email, passwordEncoder.encode(password), role));
            log.info("Created demo {} account: {}", role, email);
        }
    }
}
