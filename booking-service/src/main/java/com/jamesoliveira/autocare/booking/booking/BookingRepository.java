package com.jamesoliveira.autocare.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Booking> findAllByOrderByPreferredDateAscIdAsc();

    List<Booking> findByStatusOrderByPreferredDateAscIdAsc(BookingStatus status);
}
