package com.jamesoliveira.autocare.ai.knowledge;

/** A knowledge chunk plus how well it matched the customer's question. */
public record ScoredChunk(KnowledgeChunk chunk, double score) {
}
