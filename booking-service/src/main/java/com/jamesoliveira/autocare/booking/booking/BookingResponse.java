package com.jamesoliveira.autocare.booking.booking;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record BookingResponse(
        Long id,
        Long customerId,
        String customerName,
        String vehicle,
        String serviceCode,
        String serviceName,
        String description,
        LocalDate preferredDate,
        BookingStatus status,
        Set<BookingStatus> nextStatuses,
        String mechanicName,
        String mechanicNotes,
        Instant createdAt,
        Instant updatedAt
) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(), b.getCustomerId(), b.getCustomerName(), b.getVehicle(),
                b.getServiceCode(), b.getServiceName(), b.getDescription(), b.getPreferredDate(),
                b.getStatus(), b.getStatus().nextAllowed(), b.getMechanicName(), b.getMechanicNotes(),
                b.getCreatedAt(), b.getUpdatedAt());
    }
}
