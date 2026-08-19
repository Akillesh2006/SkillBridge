package com.skillbridge.controller;

import com.skillbridge.entity.Resume;
import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.ResumeRepository;
import com.skillbridge.repository.RoadmapRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.repository.UserSkillRepository;
import com.skillbridge.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping
public class ChatController {

    private final GeminiService geminiService;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ResumeRepository resumeRepository;
    private final RoadmapRepository roadmapRepository;

    public ChatController(
            GeminiService geminiService,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            ResumeRepository resumeRepository,
            RoadmapRepository roadmapRepository) {
        this.geminiService = geminiService;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.resumeRepository = resumeRepository;
        this.roadmapRepository = roadmapRepository;
    }

    public static class ChatRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    @PostMapping({"/ai/chat", "/chat"})
    public ResponseEntity<Map<String, String>> chat(
            Authentication authentication,
            @RequestBody ChatRequest request) {

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message cannot be empty"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build rich personalized user context for Gemini AI
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Candidate Name: ").append(user.getFullName() != null ? user.getFullName() : user.getEmail()).append("\n");
        contextBuilder.append("Email: ").append(user.getEmail()).append("\n");
        contextBuilder.append("College/University: ").append(user.getCollege() != null ? user.getCollege() : "Not specified").append("\n");

        // Skills
        List<UserSkill> skills = userSkillRepository.findByUser(user);
        List<String> skillNames = skills.stream().map(UserSkill::getSkillName).distinct().toList();
        contextBuilder.append("Detected Skills: ").append(skillNames.isEmpty() ? "None recorded" : String.join(", ", skillNames)).append("\n");

        // Resume AI Analysis
        Optional<Resume> resumeOpt = resumeRepository.findByUser(user);
        if (resumeOpt.isPresent() && resumeOpt.get().getAiAnalysis() != null) {
            contextBuilder.append("Resume AI Analysis: ").append(resumeOpt.get().getAiAnalysis()).append("\n");
        }

        // Target career and Roadmap
        if (user.getTargetJob() != null && !user.getTargetJob().isBlank()) {
            contextBuilder.append("Target Career Role: ").append(user.getTargetJob()).append("\n");
        }
        if (user.getTargetCompany() != null && !user.getTargetCompany().isBlank()) {
            contextBuilder.append("Target Company: ").append(user.getTargetCompany()).append("\n");
        }

        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        if (!roadmaps.isEmpty()) {
            Roadmap activeRoadmap = roadmaps.get(0);
            if (user.getTargetJob() == null || user.getTargetJob().isBlank()) {
                contextBuilder.append("Target Career Role: ").append(activeRoadmap.getRoleName()).append("\n");
            }
            contextBuilder.append("Career Match Percentage: ").append(activeRoadmap.getMatchPercentage()).append("%\n");
            if (activeRoadmap.getTasks() != null && !activeRoadmap.getTasks().isEmpty()) {
                contextBuilder.append("Roadmap Tasks Total: ").append(activeRoadmap.getTasks().size()).append("\n");
            }
        }

        String userContext = contextBuilder.toString();
        System.out.println("========== AI CHAT API HIT ==========");
        System.out.println("Authenticated User Email: " + email);
        System.out.println("User Context Loaded for: " + (user.getFullName() != null ? user.getFullName() : email));
        System.out.println("User Question: " + request.getMessage());
        System.out.println("Sending Request to Gemini...");

        String aiResponse = geminiService.chatWithAssistant(request.getMessage(), userContext);

        System.out.println("Gemini Response Received Successfully (Length: " + (aiResponse != null ? aiResponse.length() : 0) + ")");
        System.out.println("=====================================");

        return ResponseEntity.ok(Map.of("message", aiResponse));
    }
}
