package com.jamesoliveira.autocare.booking.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public price list. Also used by ai-service so the assistant quotes real prices. */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final ServiceItemRepository items;

    public CatalogController(ServiceItemRepository items) {
        this.items = items;
    }

    @GetMapping
    public List<ServiceItem> all() {
        return items.findAllByOrderByNameAsc();
    }
}
