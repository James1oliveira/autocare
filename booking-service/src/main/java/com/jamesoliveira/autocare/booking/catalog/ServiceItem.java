package com.jamesoliveira.autocare.booking.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One service the workshop offers, with a price range in Rands. */
@Entity
@Table(name = "service_items")
public class ServiceItem {

    @Id
    @Column(length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private int priceFrom;

    @Column(nullable = false)
    private int priceTo;

    @Column(nullable = false, length = 40)
    private String estimatedTime;

    protected ServiceItem() {
        // required by JPA
    }

    public ServiceItem(String code, String name, String description, int priceFrom, int priceTo, String estimatedTime) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.priceFrom = priceFrom;
        this.priceTo = priceTo;
        this.estimatedTime = estimatedTime;
    }

    public void updateFrom(ServiceItem other) {
        this.name = other.name;
        this.description = other.description;
        this.priceFrom = other.priceFrom;
        this.priceTo = other.priceTo;
        this.estimatedTime = other.estimatedTime;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPriceFrom() {
        return priceFrom;
    }

    public int getPriceTo() {
        return priceTo;
    }

    public String getEstimatedTime() {
        return estimatedTime;
    }
}
