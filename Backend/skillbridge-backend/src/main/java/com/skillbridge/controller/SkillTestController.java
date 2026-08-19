package com.skillbridge.controller;

import com.skillbridge.entity.SkillAssessment;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.SkillTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/skill-test")
public class SkillTestController {

    @Autowired
    private SkillTestService skillTestService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/start")
    public Map<String, Object> startAssessment(Authentication authentication) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillTestService.startAssessment(user);
        return buildSanitizedAssessmentResponse(assessment);
    }

    @GetMapping("/{assessmentId}")
    public Map<String, Object> getAssessment(
            Authentication authentication,
            @PathVariable Long assessmentId
    ) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillTestService.getAssessment(user, assessmentId);
        return buildSanitizedAssessmentResponse(assessment);
    }

    @PostMapping("/{assessmentId}/submit")
    public Map<String, Object> submitAssessment(
            Authentication authentication,
            @PathVariable Long assessmentId,
            @RequestBody List<Map<String, Object>> answers
    ) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillTestService.submitAnswers(user, assessmentId, answers);
        return buildFullAssessmentResponse(assessment);
    }

    @GetMapping("/{assessmentId}/result")
    public Map<String, Object> getAssessmentResult(
            Authentication authentication,
            @PathVariable Long assessmentId
    ) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillTestService.getAssessment(user, assessmentId);
        return buildFullAssessmentResponse(assessment);
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getAssessmentHistory(Authentication authentication) {
        User user = getUser(authentication);
        List<SkillAssessment> history = skillTestService.getAssessmentHistory(user);
        
        return history.stream().map(h -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("score", h.getScore());
            map.put("createdDate", h.getCreatedDate().toString());
            return map;
        }).toList();
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> buildSanitizedAssessmentResponse(SkillAssessment assessment) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", assessment.getId());
        response.put("score", assessment.getScore());
        response.put("createdDate", assessment.getCreatedDate().toString());

        List<Map<String, Object>> questions = assessment.getQuestions().stream().map(q -> {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("skillName", q.getSkillName());
            qMap.put("difficulty", q.getDifficulty());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("options", q.getOptions());
            return qMap;
        }).toList();

        response.put("questions", questions);
        return response;
    }

    private Map<String, Object> buildFullAssessmentResponse(SkillAssessment assessment) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", assessment.getId());
        response.put("score", assessment.getScore());
        response.put("createdDate", assessment.getCreatedDate().toString());

        List<Map<String, Object>> questions = assessment.getQuestions().stream().map(q -> {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("skillName", q.getSkillName());
            qMap.put("difficulty", q.getDifficulty());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("options", q.getOptions());
            qMap.put("correctAnswer", q.getCorrectAnswer());
            qMap.put("studentAnswer", q.getStudentAnswer());
            qMap.put("isCorrect", q.getIsCorrect());
            qMap.put("explanation", q.getExplanation());
            return qMap;
        }).toList();

        response.put("questions", questions);
        
        // Calculate and include skill breakdown details
        Map<String, Map<String, Object>> skillBreakdown = new LinkedHashMap<>();
        for (com.skillbridge.entity.AssessmentQuestion q : assessment.getQuestions()) {
            skillBreakdown.computeIfAbsent(q.getSkillName(), k -> {
                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("skillName", k);
                detail.put("totalQuestions", 0);
                detail.put("correctQuestions", 0);
                return detail;
            });
            
            Map<String, Object> detail = skillBreakdown.get(q.getSkillName());
            detail.put("totalQuestions", (int)detail.get("totalQuestions") + 1);
            if (Boolean.TRUE.equals(q.getIsCorrect())) {
                detail.put("correctQuestions", (int)detail.get("correctQuestions") + 1);
            }
        }
        
        List<Map<String, Object>> breakdownList = new ArrayList<>();
        for (Map<String, Object> detail : skillBreakdown.values()) {
            int total = (int)detail.get("totalQuestions");
            int correct = (int)detail.get("correctQuestions");
            int pct = total == 0 ? 0 : (correct * 100) / total;
            detail.put("percentage", pct);
            
            String status = "NEEDS_IMPROVEMENT";
            if (pct >= 80) status = "VERIFIED";
            else if (pct >= 50) status = "PARTIALLY_VERIFIED";
            
            detail.put("status", status);
            breakdownList.add(detail);
        }
        
        response.put("skillBreakdown", breakdownList);
        return response;
    }
}
