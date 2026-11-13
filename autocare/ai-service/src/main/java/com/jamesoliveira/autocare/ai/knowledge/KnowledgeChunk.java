package com.jamesoliveira.autocare.ai.knowledge;

/** One section of the workshop guide, e.g. "Brakes grinding, squealing or vibrating". */
public record KnowledgeChunk(String title, String serviceCode, String text) {
}
