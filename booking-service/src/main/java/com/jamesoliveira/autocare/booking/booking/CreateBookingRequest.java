package com.jamesoliveira.autocare.booking.booking;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotBlank @Size(max = 80) String vehicle,
        @NotBlank @Size(max = 40) String serviceCode,
        @Size(max = 1000) String description,
        @NotNull @FutureOrPresent LocalDate preferredDate
) {
}
