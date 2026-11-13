package com.jamesoliveira.autocare.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-controller-test;DB_CLOSE_DELAY=-1",
        "app.seed-demo-bookings=false"
})
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private static MockHttpServletRequestBuilder as(MockHttpServletRequestBuilder request, long id, String role, String name) {
        return request.header("X-User-Id", id).header("X-User-Role", role).header("X-User-Name", name);
    }

    private String newBookingJson() {
        return """
                {"vehicle":"2021 Mercedes-AMG A45","serviceCode":"WHEEL_ALIGNMENT",
                 "description":"Pulls to the left","preferredDate":"%s"}
                """.formatted(LocalDate.now().plusDays(3));
    }

    private long createBooking(long customerId) throws Exception {
        String response = mvc.perform(as(post("/api/bookings"), customerId, "CUSTOMER", "Ayesha Patel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBookingJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(response);
        return node.get("id").asLong();
    }

    @Test
    void catalogIsPublicAndSeeded() throws Exception {
        mvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void customerCanBookAndSeeOwnBookings() throws Exception {
        createBooking(501);

        mvc.perform(as(get("/api/bookings/mine"), 501, "CUSTOMER", "Ayesha Patel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName").value("Wheel alignment & balancing"))
                .andExpect(jsonPath("$[0].status").value("BOOKED"));
    }

    @Test
    void bookingNeedsLogin() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON).content(newBookingJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownServiceIsRejected() throws Exception {
        mvc.perform(as(post("/api/bookings"), 502, "CUSTOMER", "Sipho")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehicle":"Polo Vivo","serviceCode":"TIME_TRAVEL","preferredDate":"%s"}
                                """.formatted(LocalDate.now().plusDays(1))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mechanicMovesJobThroughTheWorkshop() throws Exception {
        long id = createBooking(503);

        mvc.perform(as(patch("/api/bookings/" + id + "/status"), 2, "MECHANIC", "Kabelo Dlamini")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.mechanicName").value("Kabelo Dlamini"));

        mvc.perform(as(patch("/api/bookings/" + id + "/status"), 2, "MECHANIC", "Kabelo Dlamini")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY\",\"mechanicNotes\":\"Aligned and balanced.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mechanicNotes").value("Aligned and balanced."));
    }

    @Test
    void mechanicCannotSkipSteps() throws Exception {
        long id = createBooking(504);

        mvc.perform(as(patch("/api/bookings/" + id + "/status"), 2, "MECHANIC", "Kabelo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COLLECTED\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void customerCannotChangeJobStatus() throws Exception {
        long id = createBooking(505);

        mvc.perform(as(patch("/api/bookings/" + id + "/status"), 505, "CUSTOMER", "Ayesha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"READY\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void customersOnlySeeTheirOwnBookings() throws Exception {
        long id = createBooking(506);

        mvc.perform(as(post("/api/bookings/" + id + "/cancel"), 999, "CUSTOMER", "Someone Else"))
                .andExpect(status().isNotFound());
        mvc.perform(as(post("/api/bookings/" + id + "/cancel"), 506, "CUSTOMER", "Ayesha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void onlyStaffSeeAllJobs() throws Exception {
        mvc.perform(as(get("/api/bookings"), 507, "CUSTOMER", "Ayesha"))
                .andExpect(status().isForbidden());
        mvc.perform(as(get("/api/bookings"), 2, "MECHANIC", "Kabelo"))
                .andExpect(status().isOk());
    }
}
