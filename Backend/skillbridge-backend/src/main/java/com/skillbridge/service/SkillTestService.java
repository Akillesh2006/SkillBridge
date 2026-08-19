package com.skillbridge.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.entity.AssessmentQuestion;
import com.skillbridge.entity.SkillAssessment;
import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.AssessmentQuestionRepository;
import com.skillbridge.repository.SkillAssessmentRepository;
import com.skillbridge.repository.UserSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SkillTestService {

    @Autowired
    private SkillAssessmentRepository skillAssessmentRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private GeminiService geminiService;

    @Transactional
    public SkillAssessment startAssessment(User user) {
        // 1. Get user skills
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        
        List<String> skillNames = userSkills.stream()
                .map(UserSkill::getSkillName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();

        if (skillNames.isEmpty()) {
            throw new RuntimeException("No skills found. Please upload a resume first to extract skills.");
        }

        // 2. Generate questions from Gemini
        int totalQuestions = 12; // 10-15 questions per assessment
        String questionsJson = geminiService.generateMcqQuestions(skillNames, totalQuestions);

        // 3. Create Assessment
        SkillAssessment assessment = new SkillAssessment();
        assessment.setUser(user);
        assessment.setCreatedDate(LocalDateTime.now());
        assessment.setScore(0);

        SkillAssessment savedAssessment = skillAssessmentRepository.save(assessment);
        List<AssessmentQuestion> questionList = new ArrayList<>();

        // 4. Parse JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(questionsJson);
            JsonNode array = root.get("questions");

            if (array != null && array.isArray()) {
                for (int i = 0; i < array.size(); i++) {
                    JsonNode node = array.get(i);
                    AssessmentQuestion q = new AssessmentQuestion();
                    q.setAssessment(savedAssessment);
                    q.setSkillName(node.has("skill") ? node.get("skill").asText("General") : "General");
                    q.setDifficulty(node.has("difficulty") ? node.get("difficulty").asText("MEDIUM") : "MEDIUM");
                    q.setQuestionText(node.has("question") ? node.get("question").asText("") : "");
                    q.setCorrectAnswer(node.has("correctAnswer") ? node.get("correctAnswer").asInt(0) : 0);
                    q.setExplanation(node.has("explanation") ? node.get("explanation").asText("") : "");
                    
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
            System.err.println("Failed to parse Gemini MCQ JSON. Creating fallbacks: " + e.getMessage());
            // Fallback questions if Gemini fails or returns invalid JSON
            for (String skill : skillNames) {
                AssessmentQuestion q = new AssessmentQuestion();
                q.setAssessment(savedAssessment);
                q.setSkillName(skill);
                q.setDifficulty("MEDIUM");
                q.setQuestionText("What is the primary usage of " + skill + " in software development?");
                q.setOptions(List.of(
                    "Building scaleable systems and implementations",
                    "Designing styling templates",
                    "Storing temporal configurations",
                    "Handling low level CPU tasks"
                ));
                q.setCorrectAnswer(0);
                q.setExplanation(skill + " is widely used for building scaleable systems and implementations.");
                questionList.add(q);
            }
        }

        if (questionList.isEmpty()) {
            // Ultimate fallback
            AssessmentQuestion q = new AssessmentQuestion();
            q.setAssessment(savedAssessment);
            q.setSkillName("General");
            q.setDifficulty("EASY");
            q.setQuestionText("Which methodology emphasizes iterative software development and collaboration?");
            q.setOptions(List.of("Waterfall", "Agile", "Spiral", "V-Model"));
            q.setCorrectAnswer(1);
            q.setExplanation("Agile methodology focuses on continuous iteration and collaboration.");
            questionList.add(q);
        }

        assessmentQuestionRepository.saveAll(questionList);
        savedAssessment.setQuestions(questionList);
        return skillAssessmentRepository.save(savedAssessment);
    }

    @Transactional
    public SkillAssessment submitAnswers(User user, Long assessmentId, List<Map<String, Object>> answers) {
        SkillAssessment assessment = skillAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized assessment access");
        }

        List<AssessmentQuestion> questions = assessment.getQuestions();
        int correctCount = 0;

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
            }
        }

        // Calculate score
        int scorePct = questions.isEmpty() ? 0 : (correctCount * 100) / questions.size();
        assessment.setScore(scorePct);
        assessmentQuestionRepository.saveAll(questions);
        SkillAssessment updatedAssessment = skillAssessmentRepository.save(assessment);

        // Update UserSkill verification categories
        Map<String, List<AssessmentQuestion>> groupedBySkill = new HashMap<>();
        for (AssessmentQuestion q : questions) {
            groupedBySkill.computeIfAbsent(q.getSkillName().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(q);
        }

        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        for (UserSkill us : userSkills) {
            String skillKey = us.getSkillName().toLowerCase(Locale.ROOT);
            if (groupedBySkill.containsKey(skillKey)) {
                List<AssessmentQuestion> skillQs = groupedBySkill.get(skillKey);
                long correctQs = skillQs.stream().filter(q -> Boolean.TRUE.equals(q.getIsCorrect())).count();
                int pct = (int) ((correctQs * 100) / skillQs.size());

                us.setVerificationScore(pct);
                if (pct >= 80) {
                    us.setVerificationStatus("VERIFIED");
                } else if (pct >= 50) {
                    us.setVerificationStatus("PARTIALLY_VERIFIED");
                } else {
                    us.setVerificationStatus("NEEDS_IMPROVEMENT");
                }
                userSkillRepository.save(us);
            }
        }

        return updatedAssessment;
    }

    @Transactional(readOnly = true)
    public SkillAssessment getAssessment(User user, Long assessmentId) {
        SkillAssessment assessment = skillAssessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new RuntimeException("Assessment not found"));

        if (!assessment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized assessment access");
        }

        return assessment;
    }

    @Transactional(readOnly = true)
    public List<SkillAssessment> getAssessmentHistory(User user) {
        return skillAssessmentRepository.findByUserOrderByCreatedDateDesc(user);
    }
}
