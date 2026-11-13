package com.jamesoliveira.autocare.ai.assistant;

import com.jamesoliveira.autocare.ai.knowledge.KnowledgeBase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AssistantController {

    private final DiagnosisService diagnosisService;
    private final KnowledgeBase knowledgeBase;

    public AssistantController(DiagnosisService diagnosisService, KnowledgeBase knowledgeBase) {
        this.diagnosisService = diagnosisService;
        this.knowledgeBase = knowledgeBase;
    }

    @PostMapping("/diagnose")
    public DiagnosisResponse diagnose(@Valid @RequestBody DiagnoseRequest request) {
        return diagnosisService.diagnose(request.message().trim());
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "mode", diagnosisService.isAiEnabled() ? "ai" : "offline",
                "knowledgeSections", knowledgeBase.all().size());
    }
}
