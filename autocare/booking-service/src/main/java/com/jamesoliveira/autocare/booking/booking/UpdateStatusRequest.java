package com.jamesoliveira.autocare.booking.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateStatusRequest(
        @NotNull BookingStatus status,
        @Size(max = 1000) String mechanicNotes
) {
}
