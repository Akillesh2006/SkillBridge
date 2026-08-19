package com.skillbridge.controller;

import com.skillbridge.entity.*;
import com.skillbridge.repository.*;
import com.skillbridge.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ai")
public class CopilotController {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ResumeRepository resumeRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final MockInterviewRepository mockInterviewRepository;
    private final DailyChallengeRepository dailyChallengeRepository;
    private final SkillAssessmentRepository skillAssessmentRepository;
    private final GeminiService geminiService;

    public CopilotController(
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            ResumeRepository resumeRepository,
            RoadmapRepository roadmapRepository,
            RoadmapTaskRepository roadmapTaskRepository,
            MockInterviewRepository mockInterviewRepository,
            DailyChallengeRepository dailyChallengeRepository,
            SkillAssessmentRepository skillAssessmentRepository,
            GeminiService geminiService) {
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.resumeRepository = resumeRepository;
        this.roadmapRepository = roadmapRepository;
        this.roadmapTaskRepository = roadmapTaskRepository;
        this.mockInterviewRepository = mockInterviewRepository;
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.skillAssessmentRepository = skillAssessmentRepository;
        this.geminiService = geminiService;
    }

    public static class ChatRequest {
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SettingsRequest {
        private boolean notificationsEnabled;
        private String reminderTime;
        private String quietHoursStart;
        private String quietHoursEnd;

        public boolean isNotificationsEnabled() { return notificationsEnabled; }
        public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }
        public String getReminderTime() { return reminderTime; }
        public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }
        public String getQuietHoursStart() { return quietHoursStart; }
        public void setQuietHoursStart(String quietHoursStart) { this.quietHoursStart = quietHoursStart; }
        public String getQuietHoursEnd() { return quietHoursEnd; }
        public void setQuietHoursEnd(String quietHoursEnd) { this.quietHoursEnd = quietHoursEnd; }
    }

    private User getAuthenticatedUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/copilot/context")
    public ResponseEntity<Map<String, Object>> getContext(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Map<String, Object> context = buildContextMap(user);
        return ResponseEntity.ok(context);
    }

    @PostMapping("/copilot/chat")
    public ResponseEntity<Map<String, String>> chat(Authentication authentication, @RequestBody ChatRequest request) {
        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Message cannot be empty"));
        }

        User user = getAuthenticatedUser(authentication);
        Map<String, Object> contextMap = buildContextMap(user);

        // Convert contextMap to system prompt readable string
        StringBuilder builder = new StringBuilder();
        builder.append("Target Role: ").append(contextMap.getOrDefault("targetRole", "Android Developer")).append("\n");
        builder.append("Verified Skills:\n");
        List<Map<String, Object>> verified = (List<Map<String, Object>>) contextMap.get("verifiedSkills");
        for (Map<String, Object> s : verified) {
            builder.append("- ").append(s.get("name")).append(": ").append(s.get("score")).append("% (").append(s.get("status")).append(")\n");
        }
        builder.append("Skill Gaps: ").append(String.join(", ", (List<String>) contextMap.get("skillGaps"))).append("\n");
        builder.append("Roadmap Progress: ").append(contextMap.get("roadmapProgress")).append("%\n");

        List<Map<String, Object>> mastery = (List<Map<String, Object>>) contextMap.get("masteryResults");
        if (mastery != null && !mastery.isEmpty()) {
            builder.append("Recent Skill Mastery Results:\n");
            for (Map<String, Object> m : mastery) {
                builder.append("- Topic: ").append(m.get("topic")).append(", Score: ").append(m.get("score"))
                       .append("%, Level: ").append(m.get("masteryLevel")).append(", Weak Concepts: ").append(m.get("weakConcepts")).append("\n");
            }
        }

        builder.append("Recent Missed Tasks:\n");
        List<Map<String, Object>> missed = (List<Map<String, Object>>) contextMap.get("missedTasks");
        for (Map<String, Object> t : missed) {
            builder.append("- ").append(t.get("title")).append(" (Due: ").append(t.get("dueDate")).append(")\n");
        }

        String aiResponse = geminiService.chatWithAssistant(request.getMessage(), builder.toString());
        return ResponseEntity.ok(Map.of("message", aiResponse));
    }

    @GetMapping("/daily-mission")
    public ResponseEntity<Map<String, Object>> getDailyMission(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        
        List<UserSkill> skills = userSkillRepository.findByUser(user);
        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        
        String focusSkill = "General Programming";
        String reason = "Keep practicing basic programming to build strong foundation.";
        List<String> tasks = List.of("Solve 1 medium practice question", "Revise data structures");
        int estimatedTime = 30;

        // 1. Check missed tasks first
        if (!roadmaps.isEmpty()) {
            List<RoadmapTask> roadmapTasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(roadmaps.get(0));
            
            // Mark missed tasks due before today
            LocalDate today = LocalDate.now();
            for (RoadmapTask t : roadmapTasks) {
                if (t.getDueDate() != null && t.getDueDate().isBefore(today) && !t.isCompleted() && !"MISSED".equals(t.getStatus())) {
                    t.setStatus("MISSED");
                    roadmapTaskRepository.save(t);
                }
            }

            List<RoadmapTask> pendingMastery = roadmapTasks.stream()
                    .filter(t -> "MASTERY_PENDING".equals(t.getStatus()))
                    .toList();

            List<RoadmapTask> needsWork = roadmapTasks.stream()
                    .filter(t -> "NEEDS_IMPROVEMENT".equals(t.getStatus()) || "REINFORCEMENT_REQUIRED".equals(t.getStatus()))
                    .toList();

            List<RoadmapTask> missedTasks = roadmapTasks.stream()
                    .filter(t -> "MISSED".equals(t.getStatus()))
                    .toList();

            if (!pendingMastery.isEmpty()) {
                RoadmapTask priorityTask = pendingMastery.get(0);
                focusSkill = priorityTask.getSkillName();
                reason = "Your task '" + priorityTask.getTitle() + "' is completed, but your Skill Mastery Check is pending. Verify your understanding!";
                tasks = List.of("Start Skill Mastery Check: " + priorityTask.getTitle(), "Submit answers for AI evaluation");
                estimatedTime = 15;
            } else if (!needsWork.isEmpty()) {
                RoadmapTask priorityTask = needsWork.get(0);
                focusSkill = priorityTask.getSkillName();
                Optional<SkillAssessment> ma = skillAssessmentRepository.findFirstByUserAndRoadmapTaskOrderByCreatedDateDesc(user, priorityTask);
                String weakMsg = ma.isPresent() && ma.get().getWeakConcepts() != null ? " targeting weak areas: " + ma.get().getWeakConcepts() : "";
                reason = "Your latest mastery check for '" + priorityTask.getTitle() + "' flagged concepts for reinforcement. Let's do some practice!";
                tasks = List.of("Complete reinforcement exercises" + weakMsg, "Review topic learning materials", "Start mastery check retest");
                estimatedTime = 35;
            } else if (!missedTasks.isEmpty()) {
                RoadmapTask priorityTask = missedTasks.get(0);
                focusSkill = priorityTask.getSkillName();
                reason = "Your roadmap task '" + priorityTask.getTitle() + "' was missed on " + priorityTask.getDueDate() + ". Let's get back on track!";
                tasks = List.of(priorityTask.getTitle(), "Review task resources", "Complete coding exercise");
                estimatedTime = 45;
            } else {
                // 2. Check today's tasks
                List<RoadmapTask> todaysTasks = roadmapTasks.stream()
                        .filter(t -> today.equals(t.getDueDate()) && !t.isCompleted())
                        .toList();

                if (!todaysTasks.isEmpty()) {
                    RoadmapTask todaysTask = todaysTasks.get(0);
                    focusSkill = todaysTask.getSkillName();
                    reason = "Today's scheduled learning objective is " + focusSkill + ".";
                    tasks = List.of(todaysTask.getTitle(), "Implement code snippet", "Take mini challenge");
                    estimatedTime = 40;
                } else {
                    List<String> targetGaps = new ArrayList<>();
                    if (user.getTargetSkills() != null && !user.getTargetSkills().isBlank()) {
                        List<String> reqSkills = parseJsonSkills(user.getTargetSkills());
                        Set<String> lowerDetected = new HashSet<>();
                        for (UserSkill s : skills) {
                            if (s.getSkillName() != null) {
                                lowerDetected.add(s.getSkillName().trim().toLowerCase(Locale.ROOT));
                            }
                        }
                        for (String rs : reqSkills) {
                            if (!lowerDetected.contains(rs.trim().toLowerCase(Locale.ROOT))) {
                                targetGaps.add(rs);
                            }
                        }
                    }
                    if (!targetGaps.isEmpty()) {
                        String gapSkill = targetGaps.get(0);
                        focusSkill = gapSkill;
                        reason = "This is currently one of your highest-priority gaps for your " + user.getTargetJob() + " target.";
                        tasks = List.of("Learn core concepts of " + gapSkill, "Build a simple project applying " + gapSkill, "Take Skill Verification Test");
                        estimatedTime = 45;
                    } else {
                        // 3. Focus on weakest verified skills
                        Optional<UserSkill> weakest = skills.stream()
                                .filter(s -> s.getVerificationScore() != null)
                                .min(Comparator.comparingInt(UserSkill::getVerificationScore));

                        if (weakest.isPresent() && weakest.get().getVerificationScore() < 70) {
                            focusSkill = weakest.get().getSkillName();
                            reason = "Your verification score for " + focusSkill + " is currently " + weakest.get().getVerificationScore() + "%. Let's improve it!";
                            tasks = List.of("Revise " + focusSkill + " core topics", "Complete practice problem", "Retake skill assessment test");
                            estimatedTime = 50;
                        }
                    }
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("focusSkill", focusSkill);
        response.put("reason", reason);
        response.put("tasks", tasks);
        response.put("estimatedTime", estimatedTime);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/career-readiness")
    public ResponseEntity<Map<String, Object>> getCareerReadiness(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        
        List<UserSkill> skills = userSkillRepository.findByUser(user);
        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        List<MockInterview> interviews = mockInterviewRepository.findByUserOrderByCreatedAtDesc(user);
        List<DailyChallenge> challenges = dailyChallengeRepository.findByUserOrderByCreatedAtDesc(user);

        int techScore = 60;
        int verifiedScore = 0;
        int roadmapProgress = 0;
        int interviewScore = 0;
        int challengeScore = 0;

        List<UserSkill> verifiedSkills = skills.stream().filter(s -> s.getVerificationScore() != null).toList();
        if (!verifiedSkills.isEmpty()) {
            verifiedScore = (int) verifiedSkills.stream().mapToInt(UserSkill::getVerificationScore).average().orElse(0.0);
            techScore = Math.max(techScore, verifiedScore);
        }

        if (!roadmaps.isEmpty()) {
            Roadmap r = roadmaps.get(0);
            List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(r);
            if (!tasks.isEmpty()) {
                long completed = tasks.stream().filter(RoadmapTask::isCompleted).count();
                roadmapProgress = (int) ((completed * 100) / tasks.size());
            }
        }

        List<MockInterview> completedInterviews = interviews.stream().filter(MockInterview::isCompleted).toList();
        if (!completedInterviews.isEmpty()) {
            interviewScore = (int) completedInterviews.stream()
                    .mapToInt(MockInterview::getOverallScore)
                    .average().orElse(0.0);
        }

        List<DailyChallenge> completedChallenges = challenges.stream().filter(DailyChallenge::isCompleted).toList();
        if (!completedChallenges.isEmpty()) {
            long correct = completedChallenges.stream().filter(c -> Boolean.TRUE.equals(c.getCorrect())).count();
            challengeScore = (int) ((correct * 100) / completedChallenges.size());
        }

        int resumeMatch = 0;
        List<String> reqSkills = new ArrayList<>();
        if (user.getTargetSkills() != null && !user.getTargetSkills().isBlank()) {
            reqSkills = parseJsonSkills(user.getTargetSkills());
        }
        if (!reqSkills.isEmpty()) {
            Set<String> lowerDetected = new HashSet<>();
            for (UserSkill s : skills) {
                if (s.getSkillName() != null) {
                    lowerDetected.add(s.getSkillName().trim().toLowerCase(Locale.ROOT));
                }
            }
            long matched = reqSkills.stream()
                    .filter(req -> lowerDetected.contains(req.trim().toLowerCase(Locale.ROOT)))
                    .count();
            resumeMatch = (int) ((matched * 100) / reqSkills.size());
        } else {
            resumeMatch = 65; // fallback
        }

        List<SkillAssessment> masteryList = skillAssessmentRepository.findByUserAndAssessmentTypeOrderByCreatedDateDesc(user, "MASTERY");
        int masteryScore = 0;
        int overall = 0;
        if (!masteryList.isEmpty()) {
            masteryScore = (int) masteryList.stream()
                    .mapToInt(SkillAssessment::getScore)
                    .average().orElse(0.0);
            overall = (resumeMatch + verifiedScore + roadmapProgress + interviewScore + challengeScore + masteryScore) / 6;
        } else {
            overall = (resumeMatch + verifiedScore + roadmapProgress + interviewScore + challengeScore) / 5;
        }
        if (overall == 0) overall = 50;

        // Build summary for Gemini
        String role = user.getTargetJob() != null ? user.getTargetJob() : (!roadmaps.isEmpty() ? roadmaps.get(0).getRoleName() : "Software Developer");
        StringBuilder summary = new StringBuilder();
        summary.append("Target Role: ").append(role).append("\n");
        if (user.getTargetCompany() != null) {
            summary.append("Target Company: ").append(user.getTargetCompany()).append("\n");
        }
        summary.append("Resume Match: ").append(resumeMatch).append("%\n");
        summary.append("Average Verification Score: ").append(verifiedScore).append("%\n");
        summary.append("Roadmap Progress: ").append(roadmapProgress).append("%\n");
        summary.append("Mock Interview Score: ").append(interviewScore).append("%\n");
        summary.append("Daily Challenge Performance: ").append(challengeScore).append("%\n");
        summary.append("AI Skill Mastery Score: ").append(masteryScore).append("%\n");

        String geminiJson = geminiService.generateCareerReadinessReport(role, summary.toString());

        Map<String, Object> response = new HashMap<>();
        response.put("technicalScore", techScore);
        response.put("resumeMatch", resumeMatch);
        response.put("verifiedScore", verifiedScore);
        response.put("roadmapProgress", roadmapProgress);
        response.put("interviewScore", interviewScore);
        response.put("challengeScore", challengeScore);
        response.put("masteryScore", masteryScore);
        response.put("overallScore", overall);
        response.put("reportJson", geminiJson);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reminders/settings")
    public ResponseEntity<Map<String, Object>> getSettings(Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        Map<String, Object> settings = new HashMap<>();
        settings.put("notificationsEnabled", user.isNotificationsEnabled());
        settings.put("reminderTime", user.getReminderTime());
        settings.put("quietHoursStart", user.getQuietHoursStart());
        settings.put("quietHoursEnd", user.getQuietHoursEnd());
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/reminders/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(Authentication authentication, @RequestBody SettingsRequest request) {
        User user = getAuthenticatedUser(authentication);
        user.setNotificationsEnabled(request.isNotificationsEnabled());
        user.setReminderTime(request.getReminderTime());
        user.setQuietHoursStart(request.getQuietHoursStart());
        user.setQuietHoursEnd(request.getQuietHoursEnd());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Settings updated successfully"));
    }

    private Map<String, Object> buildContextMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", user.getFullName() != null ? user.getFullName() : user.getEmail());

        List<UserSkill> skills = userSkillRepository.findByUser(user);
        List<Map<String, Object>> verified = new ArrayList<>();
        for (UserSkill s : skills) {
            if (s.getVerificationStatus() != null) {
                Map<String, Object> sMap = new HashMap<>();
                sMap.put("name", s.getSkillName());
                sMap.put("status", s.getVerificationStatus());
                sMap.put("score", s.getVerificationScore() != null ? s.getVerificationScore() : 0);
                verified.add(sMap);
            }
        }
        map.put("verifiedSkills", verified);

        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        String targetRole = user.getTargetJob() != null && !user.getTargetJob().isBlank() ? user.getTargetJob() : "Android Developer";
        String targetCompany = user.getTargetCompany() != null && !user.getTargetCompany().isBlank() ? user.getTargetCompany() : "Any Company";
        int progress = 0;
        List<Map<String, Object>> missedList = new ArrayList<>();

        if (!roadmaps.isEmpty()) {
            Roadmap r = roadmaps.get(0);
            List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(r);
            if (!tasks.isEmpty()) {
                long completed = tasks.stream().filter(RoadmapTask::isCompleted).count();
                progress = (int) ((completed * 100) / tasks.size());

                LocalDate today = LocalDate.now();
                for (RoadmapTask t : tasks) {
                    if (!t.isCompleted() && ("MISSED".equals(t.getStatus()) || (t.getDueDate() != null && t.getDueDate().isBefore(today)))) {
                        Map<String, Object> tMap = new HashMap<>();
                        tMap.put("id", t.getId());
                        tMap.put("title", t.getTitle());
                        tMap.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : "");
                        missedList.add(tMap);
                    }
                }
            }
        }
        map.put("targetRole", targetRole);
        map.put("targetCompany", targetCompany);
        map.put("roadmapProgress", progress);
        map.put("missedTasks", missedList);

        // Fetch missing skills / gaps
        List<String> detected = skills.stream().map(UserSkill::getSkillName).distinct().toList();
        List<String> required = List.of("Java", "Kotlin", "Android", "SQL", "Git", "Jetpack Compose");
        if (user.getTargetSkills() != null && !user.getTargetSkills().isBlank()) {
            required = parseJsonSkills(user.getTargetSkills());
        }
        List<String> gaps = required.stream().filter(req -> !detected.contains(req)).collect(Collectors.toList());
        map.put("skillGaps", gaps);

        List<SkillAssessment> masteryList = skillAssessmentRepository.findByUserAndAssessmentTypeOrderByCreatedDateDesc(user, "MASTERY");
        List<Map<String, Object>> masteryContext = new ArrayList<>();
        for (SkillAssessment m : masteryList) {
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("topic", m.getRoadmapTask() != null ? m.getRoadmapTask().getTitle() : "General");
            mMap.put("score", m.getScore());
            mMap.put("masteryLevel", m.getMasteryLevel());
            mMap.put("weakConcepts", m.getWeakConcepts());
            mMap.put("aiFeedback", m.getAiFeedback());
            masteryContext.add(mMap);
        }
        map.put("masteryResults", masteryContext);

        return map;
    }

    private List<String> parseJsonSkills(String targetSkillsJson) {
        List<String> list = new ArrayList<>();
        if (targetSkillsJson == null || targetSkillsJson.isBlank()) return list;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String[] arr = mapper.readValue(targetSkillsJson, String[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            String trimmed = targetSkillsJson.trim();
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
                String[] split = trimmed.split(",");
                for (String s : split) {
                    String clean = s.trim().replace("\"", "").replace("'", "");
                    if (!clean.isBlank()) list.add(clean);
                }
            }
        }
        return list;
    }
}
