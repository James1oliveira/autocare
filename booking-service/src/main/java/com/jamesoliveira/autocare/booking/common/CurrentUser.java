package com.jamesoliveira.autocare.booking.common;

import org.springframework.http.HttpStatus;

/**
 * The logged-in user, taken from the X-User-* headers the API gateway adds
 * after checking the JWT. These headers are removed from outside requests by
 * the gateway, so a client can't fake them.
 */
public record CurrentUser(Long id, String name, String role) {

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }

    public boolean isStaff() {
        return "MECHANIC".equals(role) || "ADMIN".equals(role);
    }

    public void requireCustomer() {
        if (!isCustomer() && !"ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only customers can do this");
        }
    }

    public void requireStaff() {
        if (!isStaff()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only mechanics can do this");
        }
    }
}
