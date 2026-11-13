package com.jamesoliveira.autocare.ai.knowledge;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The "retrieval" part of retrieval-augmented generation (RAG).
 *
 * The workshop guide is split into sections. For each question we find the most
 * relevant sections and give only those to the language model, so its answer is
 * based on the workshop's own notes and prices instead of guesses.
 *
 * Two ways to search:
 *  - semanticSearch: compares OpenAI embeddings (meaning-based, used when an API key is set)
 *  - keywordSearch:  simple word matching (works offline, used as a fallback)
 */
@Component
public class KnowledgeBase {

    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "when", "my", "car", "its", "it's", "has", "have", "had", "was",
            "are", "but", "not", "you", "your", "can", "this", "that", "from", "there", "what", "why", "how",
            "does", "doing", "very", "just", "some", "really", "please", "help", "need", "want", "get",
            "been", "into", "out", "too", "also", "any", "all", "while", "then", "than", "like", "makes", "make");

    private final List<KnowledgeChunk> chunks;
    private final List<Set<String>> chunkWords;
    private final List<Set<String>> titleWords;
    private volatile List<float[]> embeddings;

    public KnowledgeBase(@Value("classpath:knowledge/workshop-guide.md") Resource guide) throws IOException {
        this.chunks = parse(guide.getContentAsString(StandardCharsets.UTF_8));
        this.chunkWords = chunks.stream().map(c -> words(c.text())).toList();
        this.titleWords = chunks.stream().map(c -> words(c.title())).toList();
    }

    public List<KnowledgeChunk> all() {
        return chunks;
    }

    /** Offline search: counts matching words, with words in the section title counting double. */
    public List<ScoredChunk> keywordSearch(String question, int limit) {
        Set<String> queryWords = words(question);
        List<ScoredChunk> results = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            double score = 0;
            for (String word : queryWords) {
                if (titleWords.get(i).contains(word)) {
                    score += 2;
                } else if (chunkWords.get(i).contains(word)) {
                    score += 1;
                }
            }
            if (score > 0) {
                results.add(new ScoredChunk(chunks.get(i), score));
            }
        }
        results.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return results.stream().limit(limit).toList();
    }

    /** Meaning-based search using embeddings and cosine similarity. */
    public List<ScoredChunk> semanticSearch(EmbeddingModel embeddingModel, String question, int limit) {
        List<float[]> sectionVectors = sectionEmbeddings(embeddingModel);
        float[] questionVector = embeddingModel.embed(question);

        List<ScoredChunk> results = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            results.add(new ScoredChunk(chunks.get(i), cosineSimilarity(questionVector, sectionVectors.get(i))));
        }
        results.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
        return results.stream().limit(limit).toList();
    }

    /** Embeddings for the guide are created once, on first use, and then reused. */
    private List<float[]> sectionEmbeddings(EmbeddingModel embeddingModel) {
        List<float[]> current = embeddings;
        if (current == null) {
            synchronized (this) {
                if (embeddings == null) {
                    embeddings = embeddingModel.embed(chunks.stream().map(KnowledgeChunk::text).toList());
                }
                current = embeddings;
            }
        }
        return current;
    }

    static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    static List<KnowledgeChunk> parse(String markdown) {
        List<KnowledgeChunk> result = new ArrayList<>();
        String[] sections = markdown.split("\\R## ");
        for (int i = 1; i < sections.length; i++) {
            String section = sections[i].trim();
            String[] lines = section.split("\\R");
            String title = lines[0].trim();
            String serviceCode = null;
            for (String line : lines) {
                if (line.startsWith("Service code:")) {
                    serviceCode = line.substring("Service code:".length()).trim();
                }
            }
            result.add(new KnowledgeChunk(title, serviceCode, section));
        }
        return List.copyOf(result);
    }

    /** Lower-case words with simple endings removed, so "braking" and "brakes" both become "brak". */
    static Set<String> words(String text) {
        Set<String> result = new HashSet<>();
        for (String raw : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (raw.length() < 3 || STOP_WORDS.contains(raw)) {
                continue;
            }
            result.add(stem(raw));
        }
        return result;
    }

    static String stem(String word) {
        for (String suffix : new String[]{"ing", "ed", "es", "e", "s", "y"}) {
            if (word.endsWith(suffix) && word.length() - suffix.length() >= 4) {
                return word.substring(0, word.length() - suffix.length());
            }
        }
        return word;
    }
}
