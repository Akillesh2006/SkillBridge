package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.repository.MockInterviewRepository;
import com.skillbridge.repository.RoadmapRepository;
import com.skillbridge.repository.UserRepository;
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
@RequestMapping("/ai/mock-interview")
public class MockInterviewController {

    private final UserRepository userRepository;
    private final MockInterviewRepository mockInterviewRepository;
    private final RoadmapRepository roadmapRepository;
    private final GeminiService geminiService;

    public MockInterviewController(
            UserRepository userRepository,
            MockInterviewRepository mockInterviewRepository,
            RoadmapRepository roadmapRepository,
            GeminiService geminiService) {
        this.userRepository = userRepository;
        this.mockInterviewRepository = mockInterviewRepository;
        this.roadmapRepository = roadmapRepository;
        this.geminiService = geminiService;
    }

    public static class AnswerRequest {
        private String answer;
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/start")
    @Transactional
    public ResponseEntity<Map<String, Object>> startInterview(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        
        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        String role = user.getTargetJob() != null && !user.getTargetJob().isBlank() ? user.getTargetJob() : 
                      (!roadmaps.isEmpty() ? roadmaps.get(0).getRoleName() : "Software Developer");

        String companyContext = "";
        if (user.getTargetCompany() != null && !user.getTargetCompany().isBlank() && !"Any company".equalsIgnoreCase(user.getTargetCompany())) {
            companyContext = " (Company Prep Mode: " + user.getTargetCompany() + ")";
        }
        String targetContext = role + companyContext;

        MockInterview interview = new MockInterview();
        interview.setUser(user);
        interview.setTargetRole(role);
        interview.setCompleted(false);
        interview.setCreatedAt(LocalDateTime.now());

        String questionText = geminiService.generateMockInterviewQuestion(targetContext, "EASY", null);

        InterviewQuestion question = new InterviewQuestion();
        question.setInterview(interview);
        question.setQuestionText(questionText);
        question.setQuestionOrder(1);
        question.setDifficulty("EASY");

        interview.getQuestions().add(question);
        MockInterview saved = mockInterviewRepository.save(interview);

        Map<String, Object> response = new HashMap<>();
        response.put("interviewId", saved.getId());
        response.put("role", role);
        response.put("questionId", saved.getQuestions().get(0).getId());
        response.put("questionText", questionText);
        response.put("questionOrder", 1);
        response.put("difficulty", "EASY");
        response.put("completed", false);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answer")
    @Transactional
    public ResponseEntity<Map<String, Object>> submitAnswer(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestBody AnswerRequest request) {

        User user = getAuthenticatedUser(authentication);
        MockInterview interview = mockInterviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview not found"));

        if (!interview.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (interview.isCompleted()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Interview already completed"));
        }

        // Get the active unanswered question
        List<InterviewQuestion> questions = interview.getQuestions();
        InterviewQuestion activeQuestion = questions.stream()
                .filter(q -> q.getStudentAnswer() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active question found"));

        String answerText = request.getAnswer() != null ? request.getAnswer() : "";
        activeQuestion.setStudentAnswer(answerText);

        // Grade the answer using Gemini
        String evaluationJson = geminiService.evaluateInterviewAnswer(activeQuestion.getQuestionText(), answerText);
        
        int tech = 70;
        int concept = 70;
        int comm = 70;
        int score = 70;
        String feedback = "Good try. Focus on explaining technical constraints.";

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(evaluationJson);
            tech = root.has("technicalAccuracy") ? root.get("technicalAccuracy").asInt() : 70;
            concept = root.has("conceptUnderstanding") ? root.get("conceptUnderstanding").asInt() : 70;
            comm = root.has("communication") ? root.get("communication").asInt() : 70;
            score = root.has("score") ? root.get("score").asInt() : 70;
            feedback = root.has("feedback") ? root.get("feedback").asText() : feedback;
        } catch (Exception e) {
            System.err.println("Failed to parse evaluation JSON: " + e.getMessage());
        }

        activeQuestion.setTechnicalAccuracy(tech);
        activeQuestion.setConceptUnderstanding(concept);
        activeQuestion.setCommunication(comm);
        activeQuestion.setScore(score);
        activeQuestion.setFeedback(feedback);

        // Adaptive questioning logic
        String currentDiff = activeQuestion.getDifficulty();
        String nextDiff = currentDiff;
        if (score >= 80) {
            if ("EASY".equals(currentDiff)) nextDiff = "MEDIUM";
            else if ("MEDIUM".equals(currentDiff)) nextDiff = "HARD";
        } else if (score < 50) {
            if ("HARD".equals(currentDiff)) nextDiff = "MEDIUM";
            else if ("MEDIUM".equals(currentDiff)) nextDiff = "EASY";
        }

        int nextOrder = activeQuestion.getQuestionOrder() + 1;
        Map<String, Object> response = new HashMap<>();

        if (nextOrder > 5) {
            // End interview
            interview.setCompleted(true);
            int avgScore = (int) questions.stream().mapToInt(InterviewQuestion::getScore).average().orElse(70.0);
            interview.setOverallScore(avgScore);
            
            // Generate overall feedback summary
            StringBuilder coachSummary = new StringBuilder();
            coachSummary.append("Target Role: ").append(interview.getTargetRole()).append("\n");
            coachSummary.append("Answer performance list:\n");
            for (InterviewQuestion q : questions) {
                coachSummary.append("- ").append(q.getQuestionText()).append(" (Score: ").append(q.getScore()).append(")\n");
            }
            String finalFeedback = geminiService.chatWithAssistant("Generate a 3-sentence overall mock interview feedback coach summary.", coachSummary.toString());
            interview.setFeedback(finalFeedback);
            
            mockInterviewRepository.save(interview);

            response.put("completed", true);
            response.put("overallScore", avgScore);
            response.put("feedback", finalFeedback);
        } else {
            // Generate next adaptive question
            StringBuilder contextBuilder = new StringBuilder();
            for (InterviewQuestion q : questions) {
                contextBuilder.append("Q: ").append(q.getQuestionText()).append("\n");
                if (q.getStudentAnswer() != null) {
                    contextBuilder.append("A: ").append(q.getStudentAnswer()).append("\n");
                    contextBuilder.append("Score: ").append(q.getScore()).append("\n");
                }
            }
            String nextQuestionText = geminiService.generateMockInterviewQuestion(interview.getTargetRole(), nextDiff, contextBuilder.toString());

            InterviewQuestion nextQuestion = new InterviewQuestion();
            nextQuestion.setInterview(interview);
            nextQuestion.setQuestionText(nextQuestionText);
            nextQuestion.setQuestionOrder(nextOrder);
            nextQuestion.setDifficulty(nextDiff);

            interview.getQuestions().add(nextQuestion);
            mockInterviewRepository.save(interview);

            response.put("completed", false);
            response.put("questionId", nextQuestion.getId());
            response.put("questionText", nextQuestionText);
            response.put("questionOrder", nextOrder);
            response.put("difficulty", nextDiff);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<MockInterview> getInterviewResult(Authentication authentication, @PathVariable("id") Long id) {
        User user = getAuthenticatedUser(authentication);
        MockInterview interview = mockInterviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mock interview not found"));

        if (!interview.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return ResponseEntity.ok(interview);
    }

    @GetMapping("/history")
    public ResponseEntity<List<MockInterview>> getInterviewHistory(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        List<MockInterview> list = mockInterviewRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(list);
    }
}
