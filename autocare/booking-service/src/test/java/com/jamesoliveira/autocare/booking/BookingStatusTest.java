package com.jamesoliveira.autocare.booking;

import com.jamesoliveira.autocare.booking.booking.BookingStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusTest {

    @Test
    void jobsMoveForwardOneStepAtATime() {
        assertThat(BookingStatus.BOOKED.canMoveTo(BookingStatus.IN_PROGRESS)).isTrue();
        assertThat(BookingStatus.IN_PROGRESS.canMoveTo(BookingStatus.READY)).isTrue();
        assertThat(BookingStatus.READY.canMoveTo(BookingStatus.COLLECTED)).isTrue();
    }

    @Test
    void stepsCannotBeSkippedOrReversed() {
        assertThat(BookingStatus.BOOKED.canMoveTo(BookingStatus.READY)).isFalse();
        assertThat(BookingStatus.READY.canMoveTo(BookingStatus.IN_PROGRESS)).isFalse();
        assertThat(BookingStatus.IN_PROGRESS.canMoveTo(BookingStatus.CANCELLED)).isFalse();
    }

    @Test
    void finishedJobsAreFinal() {
        assertThat(BookingStatus.COLLECTED.nextAllowed()).isEmpty();
        assertThat(BookingStatus.CANCELLED.nextAllowed()).isEmpty();
    }
}
