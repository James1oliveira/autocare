package com.jamesoliveira.autocare.booking.config;

import com.jamesoliveira.autocare.booking.booking.Booking;
import com.jamesoliveira.autocare.booking.booking.BookingRepository;
import com.jamesoliveira.autocare.booking.booking.BookingStatus;
import com.jamesoliveira.autocare.booking.catalog.ServiceItem;
import com.jamesoliveira.autocare.booking.catalog.ServiceItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Keeps the price list up to date on every start-up and, on an empty database,
 * adds a few example jobs for the demo customer (user id 1 in auth-service).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    static final List<ServiceItem> CATALOG = List.of(
            new ServiceItem("MINOR_SERVICE", "Minor service",
                    "Oil and oil filter change, fluid top-ups and a 20-point safety check.", 1200, 2200, "1-2 hours"),
            new ServiceItem("MAJOR_SERVICE", "Major service",
                    "Oil, air, fuel and cabin filters, spark plugs where needed and a full safety inspection.", 3500, 6500, "3-5 hours"),
            new ServiceItem("BRAKE_SERVICE", "Brake pads & discs",
                    "Replace worn brake pads and skim or replace discs, plus a brake fluid check.", 1800, 4500, "2-3 hours"),
            new ServiceItem("DIAGNOSTIC_SCAN", "Diagnostic scan",
                    "Computer scan of fault codes when a warning light is on, with a written report and quote.", 550, 950, "1 hour"),
            new ServiceItem("AIRCON_REGAS", "Aircon regas & leak test",
                    "Check the system for leaks, vacuum test and regas so the aircon blows cold again.", 850, 1500, "1-2 hours"),
            new ServiceItem("WHEEL_ALIGNMENT", "Wheel alignment & balancing",
                    "Computer alignment and balancing of all four wheels to stop pulling and uneven tyre wear.", 450, 900, "1 hour"),
            new ServiceItem("TYRE_REPLACEMENT", "Tyre replacement",
                    "Supply and fit new tyres (price per tyre), including balancing and valve.", 1400, 3500, "1 hour"),
            new ServiceItem("BATTERY_REPLACEMENT", "Battery test & replacement",
                    "Test the battery and charging system, and fit a new battery if needed.", 1600, 3500, "30 minutes"),
            new ServiceItem("CLUTCH_REPLACEMENT", "Clutch replacement",
                    "Replace the clutch kit (plate, pressure plate and release bearing).", 6500, 14000, "1 day"),
            new ServiceItem("SUSPENSION_REPAIR", "Suspension repair",
                    "Inspect and replace shocks, bushes, links or ball joints causing knocks and poor handling.", 2500, 8000, "Half a day"),
            new ServiceItem("COOLING_SYSTEM", "Cooling system repair",
                    "Find and fix overheating: coolant leaks, water pump, thermostat or radiator.", 1200, 4800, "2-4 hours"),
            new ServiceItem("PERFORMANCE_TUNE", "Performance tuning",
                    "ECU remap and supporting upgrades (intake, exhaust, intercooler) with dyno runs before and after.", 6000, 25000, "1-2 days")
    );

    private final ServiceItemRepository catalog;
    private final BookingRepository bookings;
    private final boolean seedDemoBookings;

    public DataSeeder(ServiceItemRepository catalog,
                      BookingRepository bookings,
                      @Value("${app.seed-demo-bookings:true}") boolean seedDemoBookings) {
        this.catalog = catalog;
        this.bookings = bookings;
        this.seedDemoBookings = seedDemoBookings;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (ServiceItem item : CATALOG) {
            catalog.findById(item.getCode()).ifPresentOrElse(
                    existing -> existing.updateFrom(item),
                    () -> catalog.save(item));
        }

        if (seedDemoBookings && bookings.count() == 0) {
            LocalDate today = LocalDate.now();
            bookings.save(new Booking(1L, "Demo Customer", "2019 VW Golf GTI", "BRAKE_SERVICE", "Brake pads & discs",
                    "Grinding noise when braking and the steering wheel shakes at high speed.", today.plusDays(2)));

            Booking inProgress = new Booking(1L, "Demo Customer", "2020 Toyota Hilux 2.8 GD-6", "MAJOR_SERVICE",
                    "Major service", "Due for the 60,000 km service.", today);
            inProgress.changeStatus(BookingStatus.IN_PROGRESS, "Demo Mechanic", "Filters replaced, doing the safety check now.");
            bookings.save(inProgress);

            Booking ready = new Booking(1L, "Demo Customer", "2018 BMW 320i", "AIRCON_REGAS",
                    "Aircon regas & leak test", "Aircon only blows warm air.", today.minusDays(1));
            ready.changeStatus(BookingStatus.IN_PROGRESS, "Demo Mechanic", null);
            ready.changeStatus(BookingStatus.READY, null, "No leaks found. System regassed and blowing cold.");
            bookings.save(ready);
        }
    }
}
