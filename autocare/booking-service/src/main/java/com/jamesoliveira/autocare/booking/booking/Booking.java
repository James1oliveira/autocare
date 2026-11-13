package com.jamesoliveira.autocare.booking.booking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, length = 80)
    private String vehicle;

    @Column(nullable = false, length = 40)
    private String serviceCode;

    @Column(nullable = false, length = 80)
    private String serviceName;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(length = 80)
    private String mechanicName;

    @Column(length = 1000)
    private String mechanicNotes;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    protected Booking() {
        // required by JPA
    }

    public Booking(Long customerId, String customerName, String vehicle, String serviceCode,
                   String serviceName, String description, LocalDate preferredDate) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.vehicle = vehicle;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.preferredDate = preferredDate;
    }

    @PreUpdate
    void touch() {
        this.updatedAt = Instant.now();
    }

    public void changeStatus(BookingStatus newStatus, String mechanicName, String notes) {
        this.status = newStatus;
        if (mechanicName != null && !mechanicName.isBlank()) {
            this.mechanicName = mechanicName;
        }
        if (notes != null && !notes.isBlank()) {
            this.mechanicNotes = notes.trim();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getVehicle() {
        return vehicle;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getMechanicName() {
        return mechanicName;
    }

    public String getMechanicNotes() {
        return mechanicNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
