package com.jamesoliveira.autocare.booking.booking;

import java.util.Set;

/**
 * The life of a job in the workshop:
 * BOOKED -> IN_PROGRESS -> READY -> COLLECTED, or BOOKED -> CANCELLED.
 */
public enum BookingStatus {
    BOOKED,
    IN_PROGRESS,
    READY,
    COLLECTED,
    CANCELLED;

    public Set<BookingStatus> nextAllowed() {
        return switch (this) {
            case BOOKED -> Set.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> Set.of(READY);
            case READY -> Set.of(COLLECTED);
            case COLLECTED, CANCELLED -> Set.of();
        };
    }

    public boolean canMoveTo(BookingStatus next) {
        return nextAllowed().contains(next);
    }
}
