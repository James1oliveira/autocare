package com.jamesoliveira.autocare.booking.booking;

import com.jamesoliveira.autocare.booking.common.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Customer books a service. */
    @PostMapping
    public ResponseEntity<BookingResponse> create(CurrentUser user, @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.create(user, request));
    }

    /** Customer's own bookings. */
    @GetMapping("/mine")
    public List<BookingResponse> mine(CurrentUser user) {
        return bookingService.myBookings(user);
    }

    /** Mechanic/admin: every job, optionally filtered by status. */
    @GetMapping
    public List<BookingResponse> all(CurrentUser user, @RequestParam(required = false) BookingStatus status) {
        return bookingService.allBookings(user, status);
    }

    /** Mechanic/admin: move a job to its next stage. */
    @PatchMapping("/{id}/status")
    public BookingResponse updateStatus(CurrentUser user,
                                        @PathVariable Long id,
                                        @Valid @RequestBody UpdateStatusRequest request) {
        return bookingService.updateStatus(user, id, request);
    }

    /** Customer cancels a booking that hasn't started yet. */
    @PostMapping("/{id}/cancel")
    public BookingResponse cancel(CurrentUser user, @PathVariable Long id) {
        return bookingService.cancel(user, id);
    }
}
