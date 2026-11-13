package com.jamesoliveira.autocare.booking.booking;

import com.jamesoliveira.autocare.booking.catalog.ServiceItem;
import com.jamesoliveira.autocare.booking.catalog.ServiceItemRepository;
import com.jamesoliveira.autocare.booking.common.ApiException;
import com.jamesoliveira.autocare.booking.common.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final ServiceItemRepository catalog;

    public BookingService(BookingRepository bookings, ServiceItemRepository catalog) {
        this.bookings = bookings;
        this.catalog = catalog;
    }

    @Transactional
    public BookingResponse create(CurrentUser user, CreateBookingRequest request) {
        user.requireCustomer();
        ServiceItem service = catalog.findById(request.serviceCode())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown service: " + request.serviceCode()));

        Booking booking = new Booking(
                user.id(),
                user.name(),
                request.vehicle().trim(),
                service.getCode(),
                service.getName(),
                request.description() == null ? null : request.description().trim(),
                request.preferredDate());
        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(CurrentUser user) {
        return bookings.findByCustomerIdOrderByCreatedAtDesc(user.id()).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> allBookings(CurrentUser user, BookingStatus status) {
        user.requireStaff();
        List<Booking> result = status == null
                ? bookings.findAllByOrderByPreferredDateAscIdAsc()
                : bookings.findByStatusOrderByPreferredDateAscIdAsc(status);
        return result.stream().map(BookingResponse::from).toList();
    }

    @Transactional
    public BookingResponse updateStatus(CurrentUser user, Long bookingId, UpdateStatusRequest request) {
        user.requireStaff();
        Booking booking = find(bookingId);
        if (!booking.getStatus().canMoveTo(request.status())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Can't move a job from " + booking.getStatus() + " to " + request.status());
        }
        String mechanic = request.status() == BookingStatus.IN_PROGRESS ? user.name() : null;
        booking.changeStatus(request.status(), mechanic, request.mechanicNotes());
        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional
    public BookingResponse cancel(CurrentUser user, Long bookingId) {
        Booking booking = find(bookingId);
        boolean owner = booking.getCustomerId().equals(user.id());
        if (!owner && !user.isStaff()) {
            // Don't reveal that someone else's booking exists
            throw new ApiException(HttpStatus.NOT_FOUND, "Booking not found");
        }
        if (!booking.getStatus().canMoveTo(BookingStatus.CANCELLED)) {
            throw new ApiException(HttpStatus.CONFLICT, "Only bookings that haven't started can be cancelled");
        }
        booking.changeStatus(BookingStatus.CANCELLED, null, null);
        return BookingResponse.from(bookings.save(booking));
    }

    private Booking find(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found"));
    }
}
