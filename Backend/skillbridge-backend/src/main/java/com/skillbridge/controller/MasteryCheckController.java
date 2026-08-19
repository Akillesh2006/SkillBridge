package com.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.entity.AssessmentQuestion;
import com.skillbridge.entity.RoadmapTask;
import com.skillbridge.entity.SkillAssessment;
import com.skillbridge.entity.User;
import com.skillbridge.repository.AssessmentQuestionRepository;
import com.skillbridge.repository.RoadmapTaskRepository;
import com.skillbridge.repository.SkillAssessmentRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/mastery")
public class MasteryCheckController {

    @Autowired
    private SkillAssessmentRepository skillAssessmentRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private RoadmapTaskRepository roadmapTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiService geminiService;

    @PostMapping("/tasks/{taskId}/start")
    public ResponseEntity<?> startMasteryCheck(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "10") int questionCount
    ) {
        User user = getUser(authentication);
        RoadmapTask task = roadmapTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Roadmap task not found"));

        if (!task.getRoadmap().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Task does not belong to this user");
        }

        // Return incomplete mastery check if one already exists
        Optional<SkillAssessment> incompleteOpt = skillAssessmentRepository
                .findFirstByUserAndRoadmapTaskOrderByCreatedDateDesc(user, task);
        if (incompleteOpt.isPresent() && incompleteOpt.get().getScore() == 0) {
            return ResponseEntity.ok(buildSanitizedResponse(incompleteOpt.get()));
        }

        // Generate questions from Gemini
        String studentInfo = "";
        if (incompleteOpt.isPresent()) {
            studentInfo = "Previous mastery check score for this task: " + incompleteOpt.get().getScore() + "%. "
                    + "Previous weak concepts: " + incompleteOpt.get().getWeakConcepts();
        }

        String questionsJson = geminiService.generateMasteryQuestions(
                task.getTitle(),
                task.getDescription(),
                task.getSkillName(),
                studentInfo,
                questionCount
        );

        SkillAssessment assessment = new SkillAssessment();
        assessment.setUser(user);
        assessment.setRoadmapTask(task);
        assessment.setAssessmentType("MASTERY");
        assessment.setCreatedDate(LocalDateTime.now());
        assessment.setScore(0);

        SkillAssessment savedAssessment = skillAssessmentRepository.save(assessment);
        List<AssessmentQuestion> questionList = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(questionsJson);

            if (array != null && array.isArray()) {
                for (int i = 0; i < array.size(); i++) {
                    JsonNode node = array.get(i);
                    AssessmentQuestion q = new AssessmentQuestion();
                    q.setAssessment(savedAssessment);
                    q.setSkillName(task.getSkillName());
                    q.setDifficulty(node.has("difficulty") ? node.get("difficulty").asText("MEDIUM") : "MEDIUM");
                    q.setQuestionText(node.has("questionText") ? node.get("questionText").asText("") : "");
                    q.setCorrectAnswer(node.has("correctAnswer") ? node.get("correctAnswer").asInt(0) : 0);
                    q.setExplanation(node.has("explanation") ? node.get("explanation").asText("") : "");
                    q.setConcept(node.has("concept") ? node.get("concept").asText("General") : "General");
                    q.setQuestionType(node.has("questionType") ? node.get("questionType").asText("CONCEPTUAL") : "CONCEPTUAL");

                    List<String> options = new ArrayList<>();
                    JsonNode optsArray = node.get("options");
                    if (optsArray != null && optsArray.isArray()) {
                        for (int j = 0; j < optsArray.size(); j++) {
                            options.add(optsArray.get(j).asText());
                        }
                    }
                    if (options.isEmpty()) {
                        options = List.of("Option A", "Option B", "Option C", "Option D");
                    }
                    q.setOptions(options);
                    questionList.add(q);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini Mastery JSON. Using fallbacks: " + e.getMessage());
            // Fallback questions
            for (int i = 1; i <= questionCount; i++) {
                AssessmentQuestion q = new AssessmentQuestion();
                q.setAssessment(savedAssessment);
                q.setSkillName(task.getSkillName());
                q.setDifficulty("MEDIUM");
                q.setQuestionText("Explain concept related to " + task.getSkillName() + " (Problem " + i + ")");
                q.setOptions(List.of(
                        "Understanding core design constraints",
                        "Executing database structures",
                        "Handling networking models",
                        "Debugging compiler directives"
                ));
                q.setCorrectAnswer(0);
                q.setExplanation("This option correctly defines the foundational constraint of " + task.getSkillName());
                q.setConcept(task.getSkillName());
                q.setQuestionType(i % 2 == 0 ? "CONCEPTUAL" : "APPLICATION");
                questionList.add(q);
            }
        }

        assessmentQuestionRepository.saveAll(questionList);
        savedAssessment.setQuestions(questionList);
        return ResponseEntity.ok(buildSanitizedResponse(skillAssessmentRepository.save(savedAssessment)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMasteryCheck(Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        return ResponseEntity.ok(buildSanitizedResponse(assessment));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitMasteryCheck(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody List<Map<String, Object>> answers
    ) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        List<AssessmentQuestion> questions = assessment.getQuestions();
        int correctCount = 0;

        int conceptualTotal = 0, conceptualCorrect = 0;
        int applicationTotal = 0, applicationCorrect = 0;
        int problemSolvingTotal = 0, problemSolvingCorrect = 0;
        int debuggingTotal = 0, debuggingCorrect = 0;

        StringBuilder qaSummary = new StringBuilder();

        for (Map<String, Object> ans : answers) {
            if (!ans.containsKey("questionId") || !ans.containsKey("studentAnswer")) continue;
            Long qId = Long.valueOf(ans.get("questionId").toString());
            Integer studentAns = (Integer) ans.get("studentAnswer");

            AssessmentQuestion question = questions.stream()
                    .filter(q -> q.getId().equals(qId))
                    .findFirst()
                    .orElse(null);

            if (question != null) {
                question.setStudentAnswer(studentAns);
                boolean correct = studentAns != null && studentAns.equals(question.getCorrectAnswer());
                question.setIsCorrect(correct);
                if (correct) {
                    correctCount++;
                }

                String type = question.getQuestionType();
                if ("CONCEPTUAL".equalsIgnoreCase(type) || "UNDERSTANDING".equalsIgnoreCase(type)) {
                    conceptualTotal++;
                    if (correct) conceptualCorrect++;
                } else if ("APPLICATION".equalsIgnoreCase(type)) {
                    applicationTotal++;
                    if (correct) applicationCorrect++;
                } else if ("SCENARIO".equalsIgnoreCase(type)) {
                    problemSolvingTotal++;
                    if (correct) problemSolvingCorrect++;
                } else if ("DEBUGGING".equalsIgnoreCase(type)) {
                    debuggingTotal++;
                    if (correct) debuggingCorrect++;
                }

                qaSummary.append("Q: ").append(question.getQuestionText()).append("\n")
                        .append("Concept: ").append(question.getConcept()).append("\n")
                        .append("Correct Option Index: ").append(question.getCorrectAnswer()).append("\n")
                        .append("Student Choice Index: ").append(studentAns).append("\n")
                        .append("Correct: ").append(correct).append("\n\n");
            }
        }

        int scorePct = questions.isEmpty() ? 0 : (correctCount * 100) / questions.size();
        assessment.setScore(scorePct);

        assessment.setConceptualScore(conceptualTotal == 0 ? 0 : (conceptualCorrect * 100) / conceptualTotal);
        assessment.setApplicationScore(applicationTotal == 0 ? 0 : (applicationCorrect * 100) / applicationTotal);
        assessment.setProblemSolvingScore(problemSolvingTotal == 0 ? 0 : (problemSolvingCorrect * 100) / problemSolvingTotal);
        assessment.setDebuggingScore(debuggingTotal == 0 ? 0 : (debuggingCorrect * 100) / debuggingTotal);

        // Fetch AI Analysis feedback from Gemini
        String feedbackJson = geminiService.evaluateMasteryCheck(
                assessment.getRoadmapTask().getTitle(),
                scorePct,
                qaSummary.toString()
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(feedbackJson);

            assessment.setMasteryLevel(root.has("masteryLevel") ? root.get("masteryLevel").asText("REINFORCE") : "REINFORCE");
            assessment.setAiFeedback(root.has("explanation") ? root.get("explanation").asText("") : "");

            if (root.has("strengths") && root.get("strengths").isArray()) {
                assessment.setStrengths(root.get("strengths").toString());
            } else {
                assessment.setStrengths("[\"Foundational concepts\"]");
            }

            if (root.has("weakConcepts") && root.get("weakConcepts").isArray()) {
                assessment.setWeakConcepts(root.get("weakConcepts").toString());
            } else {
                assessment.setWeakConcepts("[]");
            }

            if (root.has("recommendedPractice") && root.get("recommendedPractice").isArray()) {
                assessment.setRecommendedPractice(root.get("recommendedPractice").toString());
            } else {
                assessment.setRecommendedPractice("[]");
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini feedback evaluation JSON: " + e.getMessage());
            assessment.setMasteryLevel(scorePct >= 80 ? "MASTERED" : (scorePct >= 60 ? "REINFORCE" : "NEEDS_WORK"));
            assessment.setAiFeedback("Based on your score, please review the topics where you made mistakes.");
            assessment.setStrengths("[\"Core topic fundamentals\"]");
            assessment.setWeakConcepts("[]");
            assessment.setRecommendedPractice("[\"Review previous lessons\", \"Practice coding exercises\"]");
        }

        assessmentQuestionRepository.saveAll(questions);
        SkillAssessment updatedAssessment = skillAssessmentRepository.save(assessment);

        // Update RoadmapTask status based on Mastery levels
        RoadmapTask task = assessment.getRoadmapTask();
        if (scorePct >= 80) {
            task.setStatus("MASTERED");
            task.setCompleted(true);
        } else if (scorePct >= 60) {
            task.setStatus("REINFORCEMENT_REQUIRED");
            task.setCompleted(true);
        } else {
            task.setStatus("NEEDS_IMPROVEMENT");
            task.setCompleted(false);
        }
        roadmapTaskRepository.save(task);

        return ResponseEntity.ok(buildFullResponse(updatedAssessment));
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<?> getMasteryCheckResult(Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        SkillAssessment assessment = skillAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        return ResponseEntity.ok(buildFullResponse(assessment));
    }

    @PostMapping("/{id}/retest")
    public ResponseEntity<?> retestMasteryCheck(Authentication authentication, @PathVariable Long id) {
        User user = getUser(authentication);
        SkillAssessment oldAssessment = skillAssessmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!oldAssessment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        RoadmapTask task = oldAssessment.getRoadmapTask();

        // Generate questions from Gemini with explicit instruction to introduce NEW equivalent questions
        String studentInfo = "Retest required. Previous attempt score: " + oldAssessment.getScore() + "%. Previous weak concepts: " + oldAssessment.getWeakConcepts()
                + ". Generate equivalent but entirely NEW questions. Do not repeat previous questions.";

        String questionsJson = geminiService.generateMasteryQuestions(
                task.getTitle(),
                task.getDescription(),
                task.getSkillName(),
                studentInfo,
                10
        );

        SkillAssessment newAssessment = new SkillAssessment();
        newAssessment.setUser(user);
        newAssessment.setRoadmapTask(task);
        newAssessment.setAssessmentType("MASTERY");
        newAssessment.setCreatedDate(LocalDateTime.now());
        newAssessment.setScore(0);

        SkillAssessment savedAssessment = skillAssessmentRepository.save(newAssessment);
        List<AssessmentQuestion> questionList = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode array = mapper.readTree(questionsJson);

            if (array != null && array.isArray()) {
                for (int i = 0; i < array.size(); i++) {
                    JsonNode node = array.get(i);
                    AssessmentQuestion q = new AssessmentQuestion();
                    q.setAssessment(savedAssessment);
                    q.setSkillName(task.getSkillName());
                    q.setDifficulty(node.has("difficulty") ? node.get("difficulty").asText("MEDIUM") : "MEDIUM");
                    q.setQuestionText(node.has("questionText") ? node.get("questionText").asText("") : "");
                    q.setCorrectAnswer(node.has("correctAnswer") ? node.get("correctAnswer").asInt(0) : 0);
                    q.setExplanation(node.has("explanation") ? node.get("explanation").asText("") : "");
                    q.setConcept(node.has("concept") ? node.get("concept").asText("General") : "General");
                    q.setQuestionType(node.has("questionType") ? node.get("questionType").asText("CONCEPTUAL") : "CONCEPTUAL");

                    List<String> options = new ArrayList<>();
                    JsonNode optsArray = node.get("options");
                    if (optsArray != null && optsArray.isArray()) {
                        for (int j = 0; j < optsArray.size(); j++) {
                            options.add(optsArray.get(j).asText());
                        }
                    }
                    if (options.isEmpty()) {
                        options = List.of("Option A", "Option B", "Option C", "Option D");
                    }
                    q.setOptions(options);
                    questionList.add(q);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini Mastery Retest JSON. Using fallbacks: " + e.getMessage());
            for (int i = 1; i <= 10; i++) {
                AssessmentQuestion q = new AssessmentQuestion();
                q.setAssessment(savedAssessment);
                q.setSkillName(task.getSkillName());
                q.setDifficulty("MEDIUM");
                q.setQuestionText("Explain concept related to " + task.getSkillName() + " (Retest problem " + i + ")");
                q.setOptions(List.of(
                        "Understanding core design constraints",
                        "Executing database structures",
                        "Handling networking models",
                        "Debugging compiler directives"
                ));
                q.setCorrectAnswer(0);
                q.setExplanation("This option correctly defines the foundational constraint of " + task.getSkillName());
                q.setConcept(task.getSkillName());
                q.setQuestionType(i % 2 == 0 ? "CONCEPTUAL" : "APPLICATION");
                questionList.add(q);
            }
        }

        assessmentQuestionRepository.saveAll(questionList);
        savedAssessment.setQuestions(questionList);
        return ResponseEntity.ok(buildSanitizedResponse(skillAssessmentRepository.save(savedAssessment)));
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Map<String, Object> buildSanitizedResponse(SkillAssessment assessment) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", assessment.getId());
        response.put("score", assessment.getScore());
        response.put("createdDate", assessment.getCreatedDate().toString());
        response.put("assessmentType", assessment.getAssessmentType());

        List<Map<String, Object>> questions = assessment.getQuestions().stream().map(q -> {
            Map<String, Object> qMap = new LinkedHashMap<>();
            qMap.put("id", q.getId());
            qMap.put("skillName", q.getSkillName());
            qMap.put("difficulty", q.getDifficulty());
            qMap.put("questionText", q.getQuestionText());
            qMap.put("options", q.getOptions());
            qMap.put("concept", q.getConcept());
            qMap.put("questionType", q.getQuestionType());
            return qMap;
        }).toList();

        response.put("questions", questions);
        return response;
    }

    private Map<String, Object> buildFullResponse(SkillAssessment assessment) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", assessment.getId());
        response.put("score", assessment.getScore());
        response.put("createdDate", assessment.getCreatedDate().toString());
        response.put("assessmentType", assessment.getAssessmentType());
        response.put("masteryLevel", assessment.getMasteryLevel());
        response.put("strengths", assessment.getStrengths());
        response.put("weakConcepts", assessment.getWeakConcepts());
        response.put("aiFeedback", assessment.getAiFeedback());
        response.put("recommendedPractice", assessment.getRecommendedPractice());
        response.put("conceptualScore", assessment.getConceptualScore());
        response.put("applicationScore", assessment.getApplicationScore());
        response.put("problemSolvingScore", assessment.getProblemSolvingScore());
        response.put("debuggingScore", assessment.getDebuggingScore());

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
            qMap.put("concept", q.getConcept());
            qMap.put("questionType", q.getQuestionType());
            return qMap;
        }).toList();

        response.put("questions", questions);
        return response;
    }
}
