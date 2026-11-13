package com.jamesoliveira.autocare.booking.catalog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, String> {

    List<ServiceItem> findAllByOrderByNameAsc();
}
