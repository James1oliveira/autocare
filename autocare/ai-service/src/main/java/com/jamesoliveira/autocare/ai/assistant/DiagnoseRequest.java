package com.jamesoliveira.autocare.ai.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Messages are capped at 500 characters to limit cost and prompt-injection attempts. */
public record DiagnoseRequest(@NotBlank @Size(max = 500) String message) {
}
