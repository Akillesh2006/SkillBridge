package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.repository.DailyChallengeRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.repository.UserSkillRepository;
import com.skillbridge.service.GeminiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/ai/challenge")
public class DailyChallengeController {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final DailyChallengeRepository dailyChallengeRepository;
    private final GeminiService geminiService;

    public DailyChallengeController(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            DailyChallengeRepository dailyChallengeRepository,
            GeminiService geminiService) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.geminiService = geminiService;
    }

    public static class ChallengeSubmitRequest {
        private String answer;
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/generate")
    @Transactional
    public ResponseEntity<Map<String, Object>> generateChallenge(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<UserSkill> skills = userSkillRepository.findByUser(user);
        
        String skillToTest = "Java";
        
        // Find weakest skill
        Optional<UserSkill> weakest = skills.stream()
                .filter(s -> s.getVerificationScore() != null)
                .min(Comparator.comparingInt(UserSkill::getVerificationScore));

        if (weakest.isPresent()) {
            skillToTest = weakest.get().getSkillName();
        } else if (!skills.isEmpty()) {
            skillToTest = skills.get(0).getSkillName();
        }

        String challengeJson = geminiService.generateDailyChallenge(skillToTest);
        
        String type = "MCQ";
        String question = "What is JVM?";
        String options = "Java Virtual Machine,Java Variable Method,Java Visual Manager,None";
        String correctAns = "Java Virtual Machine";
        String exp = "JVM stands for Java Virtual Machine.";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(challengeJson);
            type = root.has("challengeType") ? root.get("challengeType").asText() : "MCQ";
            question = root.has("questionText") ? root.get("questionText").asText() : question;
            options = root.has("options") ? root.get("options").asText() : options;
            correctAns = root.has("correctAnswer") ? root.get("correctAnswer").asText() : correctAns;
            exp = root.has("explanation") ? root.get("explanation").asText() : exp;
        } catch (Exception e) {
            System.err.println("Jackson daily challenge parse fallback: " + e.getMessage());
        }

        DailyChallenge challenge = new DailyChallenge();
        challenge.setUser(user);
        challenge.setSkillName(skillToTest);
        challenge.setChallengeType(type);
        challenge.setQuestionText(question);
        challenge.setOptions(options);
        challenge.setCorrectAnswer(correctAns);
        challenge.setExplanation(exp);
        challenge.setCompleted(false);
        challenge.setCreatedAt(LocalDateTime.now());

        DailyChallenge saved = dailyChallengeRepository.save(challenge);

        Map<String, Object> response = new HashMap<>();
        response.put("challengeId", saved.getId());
        response.put("skillName", skillToTest);
        response.put("challengeType", type);
        response.put("questionText", question);
        response.put("options", options);
        response.put("completed", false);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/submit")
    @Transactional
    public ResponseEntity<DailyChallenge> submitAnswer(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestBody ChallengeSubmitRequest request) {

        User user = getAuthenticatedUser(authentication);
        DailyChallenge challenge = dailyChallengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Daily challenge not found"));

        if (!challenge.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (challenge.isCompleted()) {
            return ResponseEntity.badRequest().body(challenge);
        }

        String studentAnswerText = request.getAnswer() != null ? request.getAnswer() : "";
        challenge.setStudentAnswer(studentAnswerText);

        // Grade via Gemini
        String resultJson = geminiService.evaluateChallengeAnswer(
                challenge.getQuestionText(),
                challenge.getChallengeType(),
                challenge.getCorrectAnswer(),
                studentAnswerText
        );

        boolean correct = false;
        String improvement = "Review core language syntax guides.";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resultJson);
            correct = root.has("correct") && root.get("correct").asBoolean();
            if (root.has("explanation")) {
                challenge.setExplanation(root.get("explanation").asText());
            }
            improvement = root.has("improvementSuggestion") ? root.get("improvementSuggestion").asText() : improvement;
        } catch (Exception e) {
            System.err.println("Jackson evaluation daily challenge parse fallback: " + e.getMessage());
            // Fallback check
            correct = studentAnswerText.trim().equalsIgnoreCase(challenge.getCorrectAnswer().trim());
        }

        challenge.setCorrect(correct);
        challenge.setImprovementSuggestion(improvement);
        challenge.setCompleted(true);

        DailyChallenge saved = dailyChallengeRepository.save(challenge);

        // If correct, update the verified status of the user's skill
        if (correct) {
            List<UserSkill> skills = userSkillRepository.findByUser(user);
            Optional<UserSkill> match = skills.stream()
                    .filter(s -> s.getSkillName().equalsIgnoreCase(challenge.getSkillName()))
                    .findFirst();
            if (match.isPresent()) {
                UserSkill skill = match.get();
                skill.setVerificationStatus("VERIFIED");
                skill.setVerificationScore(Math.max(80, skill.getVerificationScore() != null ? skill.getVerificationScore() + 5 : 85));
                userSkillRepository.save(skill);
            }
        }

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyChallenge>> getChallengeHistory(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<DailyChallenge> list = dailyChallengeRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(list);
    }
}
