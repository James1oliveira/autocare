package com.jamesoliveira.autocare.ai.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnowledgeBaseTest {

    private final KnowledgeBase knowledgeBase = new KnowledgeBase(new ClassPathResource("knowledge/workshop-guide.md"));

    KnowledgeBaseTest() throws IOException {
    }

    private String topServiceFor(String question) {
        List<ScoredChunk> results = knowledgeBase.keywordSearch(question, 3);
        assertThat(results).as("results for: " + question).isNotEmpty();
        return results.get(0).chunk().serviceCode();
    }

    @Test
    void everySectionHasAServiceCode() {
        assertThat(knowledgeBase.all()).hasSize(12);
        assertThat(knowledgeBase.all()).allSatisfy(chunk -> assertThat(chunk.serviceCode()).isNotBlank());
    }

    @Test
    void matchesCommonCustomerQuestions() {
        assertThat(topServiceFor("grinding noise when I brake")).isEqualTo("BRAKE_SERVICE");
        assertThat(topServiceFor("my aircon only blows warm air")).isEqualTo("AIRCON_REGAS");
        assertThat(topServiceFor("car pulls to the left")).isEqualTo("WHEEL_ALIGNMENT");
        assertThat(topServiceFor("check engine light came on")).isEqualTo("DIAGNOSTIC_SCAN");
        assertThat(topServiceFor("car won't start, just clicking")).isEqualTo("BATTERY_REPLACEMENT");
        assertThat(topServiceFor("knocking over speed bumps")).isEqualTo("SUSPENSION_REPAIR");
        assertThat(topServiceFor("I want a stage 2 remap")).isEqualTo("PERFORMANCE_TUNE");
    }

    @Test
    void unrelatedTextFindsNothing() {
        assertThat(knowledgeBase.keywordSearch("hello there", 3)).isEmpty();
    }

    @Test
    void stemmingTreatsWordFormsTheSame() {
        assertThat(KnowledgeBase.stem("braking")).isEqualTo(KnowledgeBase.stem("brakes"));
        assertThat(KnowledgeBase.stem("grinding")).isEqualTo("grind");
    }

    @Test
    void cosineSimilarityOfIdenticalVectorsIsOne() {
        float[] v = {0.2f, 0.5f, 0.1f};
        assertThat(KnowledgeBase.cosineSimilarity(v, v)).isCloseTo(1.0, org.assertj.core.data.Offset.offset(1e-6));
    }
}
