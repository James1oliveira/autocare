package com.jamesoliveira.autocare.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs without an OpenAI key and with booking-service unreachable,
 * so it tests the offline fallback that keeps the assistant working.
 */
@SpringBootTest(properties = {
        "spring.ai.openai.api-key=not-set",
        "app.booking-service-url=http://localhost:1"
})
@AutoConfigureMockMvc
class AssistantControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void statusShowsOfflineModeWithoutKey() throws Exception {
        mvc.perform(get("/api/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.knowledgeSections").value(12));
    }

    @Test
    void suggestsBrakeServiceForGrindingNoise() throws Exception {
        mvc.perform(post("/api/ai/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"There's a grinding noise when I brake"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.suggestions[0].code").value("BRAKE_SERVICE"))
                .andExpect(jsonPath("$.reply").value(containsString("mechanic")));
    }

    @Test
    void unknownProblemSuggestsDiagnosticScan() throws Exception {
        mvc.perform(post("/api/ai/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"hello there"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions[0].code").value("DIAGNOSTIC_SCAN"));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        mvc.perform(post("/api/ai/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void veryLongMessageIsRejected() throws Exception {
        String longMessage = "a".repeat(501);
        mvc.perform(post("/api/ai/diagnose")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"" + longMessage + "\"}"))
                .andExpect(status().isBadRequest());
    }
}
