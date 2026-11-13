package com.jamesoliveira.autocare.ai.assistant;

import java.util.List;

/**
 * mode is "ai" when the answer came from the language model,
 * or "offline" when the built-in keyword fallback answered.
 */
public record DiagnosisResponse(String reply, List<Suggestion> suggestions, String mode) {
}
