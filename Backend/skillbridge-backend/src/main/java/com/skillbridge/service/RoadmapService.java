package com.skillbridge.service;

import com.skillbridge.entity.JobRole;
import com.skillbridge.entity.Resume;
import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.RoadmapTask;
import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.JobRoleRepository;
import com.skillbridge.repository.ResumeRepository;
import com.skillbridge.repository.RoadmapRepository;
import com.skillbridge.repository.RoadmapTaskRepository;
import com.skillbridge.repository.UserSkillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;
    private final UserSkillRepository userSkillRepository;
    private final JobRoleRepository jobRoleRepository;
    private final ResumeRepository resumeRepository;
    private final GeminiService geminiService;

    public RoadmapService(
            RoadmapRepository roadmapRepository,
            RoadmapTaskRepository roadmapTaskRepository,
            UserSkillRepository userSkillRepository,
            JobRoleRepository jobRoleRepository,
            ResumeRepository resumeRepository,
            GeminiService geminiService) {

        this.roadmapRepository = roadmapRepository;
        this.roadmapTaskRepository = roadmapTaskRepository;
        this.userSkillRepository = userSkillRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.resumeRepository = resumeRepository;
        this.geminiService = geminiService;
    }

    @Transactional
    public Roadmap generateRoadmap(User user, String roleName) {

        System.out.println("ROADMAP GENERATION STARTED");
        System.out.println("USER: " + user.getEmail());

        String targetRole = (roleName != null && !roleName.isBlank()) ? roleName : "Android Developer";

        // 1. Retrieve user's detected skills from UserSkillRepository
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        List<String> detectedSkills = userSkills.stream()
                .map(UserSkill::getSkillName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        System.out.println("DETECTED SKILLS: " + detectedSkills);

        // 2. Identify missing skills based on target role
        JobRole jobRole = jobRoleRepository
                .findByRoleNameIgnoreCase(targetRole)
                .orElseGet(() -> {
                    String lower = targetRole.toLowerCase(Locale.ROOT);
                    JobRole dynamicRole = new JobRole();
                    dynamicRole.setRoleName(targetRole);

                    if (lower.contains("data scientist") || lower.contains("data science")) {
                        dynamicRole.setRequiredSkills("Python,Pandas,NumPy,SQL,Statistics,Machine Learning,Data Visualization,Scikit-learn");
                    } else if (lower.contains("data analyst") || lower.contains("analytics")) {
                        dynamicRole.setRequiredSkills("Python,Pandas,NumPy,SQL,Excel,Statistics,Data Visualization");
                    } else if (lower.contains("android") || lower.contains("mobile")) {
                        dynamicRole.setRequiredSkills("Android,Kotlin,Java,Jetpack Compose,Room,Coroutines,Retrofit,MVVM");
                    } else if (lower.contains("front") || lower.contains("ui")) {
                        dynamicRole.setRequiredSkills("HTML,CSS,JavaScript,React,Git,REST APIs");
                    } else if (lower.contains("machine learning") || lower.contains("ai")) {
                        dynamicRole.setRequiredSkills("Python,Machine Learning,NumPy,Pandas,TensorFlow,SQL,Git");
                    } else {
                        dynamicRole.setRequiredSkills("Java,Spring Boot,SQL,REST APIs,Git,JPA,Hibernate");
                    }
                    return jobRoleRepository.save(dynamicRole);
                });

        Set<String> lowerDetected = new HashSet<>(
                detectedSkills.stream().map(s -> s.toLowerCase(Locale.ROOT)).toList()
        );

        List<String> requiredSkills;
        if (user.getTargetJob() != null && user.getTargetJob().equalsIgnoreCase(targetRole) &&
            user.getTargetSkills() != null && !user.getTargetSkills().isBlank()) {
            requiredSkills = parseJsonSkills(user.getTargetSkills());
        } else {
            requiredSkills = Arrays.stream(jobRole.getRequiredSkills().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        List<String> missingSkills = requiredSkills.stream()
                .filter(req -> !lowerDetected.contains(req.toLowerCase(Locale.ROOT)))
                .toList();

        System.out.println("SKILL GAPS: " + missingSkills);

        List<String> masteredSkillsPrompt = new ArrayList<>();
        List<String> focusSkillsPrompt = new ArrayList<>();

        for (UserSkill us : userSkills) {
            String skill = us.getSkillName();
            if (skill == null) continue;
            String status = us.getVerificationStatus();
            
            if ("VERIFIED".equals(status)) {
                masteredSkillsPrompt.add(skill + " (Verified)");
            } else if ("PARTIALLY_VERIFIED".equals(status)) {
                masteredSkillsPrompt.add(skill + " (Partially Verified)");
                focusSkillsPrompt.add(skill + " (Partially Verified - focus on intermediate concepts)");
            } else if ("NEEDS_IMPROVEMENT".equals(status)) {
                focusSkillsPrompt.add(skill + " (Needs Improvement - focus on fundamental practice)");
            } else {
                masteredSkillsPrompt.add(skill + " (Detected)");
            }
        }

        for (String req : requiredSkills) {
            boolean hasIt = userSkills.stream()
                    .anyMatch(us -> us.getSkillName() != null && us.getSkillName().equalsIgnoreCase(req));
            if (!hasIt) {
                focusSkillsPrompt.add(req + " (Missing - learn from scratch)");
            }
        }

        // 3. Retrieve user's resume summary if available
        Optional<Resume> userResume = resumeRepository.findByUser(user);
        String profileSummary = userResume.map(Resume::getAiAnalysis).orElse("Candidate aiming for " + targetRole);
        if (user.getTargetCompany() != null && !user.getTargetCompany().isBlank()) {
            profileSummary += "\nTarget Company: " + user.getTargetCompany() + " (Focus preparation on this company's expectations)";
        }

        // 4. Request dynamic roadmap from Gemini AI
        System.out.println("SENDING ROADMAP REQUEST TO GEMINI");
        String geminiJson = "";
        try {
            geminiJson = geminiService.generateRoadmap(targetRole, masteredSkillsPrompt, focusSkillsPrompt, profileSummary);
            System.out.println("ROADMAP GENERATED");
        } catch (Exception e) {
            System.err.println("Gemini roadmap request failed, utilizing dynamic fallback: " + e.getMessage());
        }

        // 5. Update existing roadmap for this user or create a new one
        Roadmap roadmap = roadmapRepository
                .findByUserAndRoleName(user, targetRole)
        .orElseGet(() -> roadmapRepository.findByUser(user).stream().findFirst().orElse(new Roadmap()));

        roadmap.setUser(user);
        roadmap.setRoleName(targetRole);

        double matchPercentage = requiredSkills.isEmpty() ? 80.0 :
                ((requiredSkills.size() - missingSkills.size()) * 100.0 / requiredSkills.size());
        roadmap.setMatchPercentage(Math.round(matchPercentage * 100.0) / 100.0);
        roadmap.setReadinessScore(Math.round(matchPercentage * 100.0) / 100.0);
        roadmap.setCreatedAt(LocalDateTime.now());

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Remove old tasks before saving new tasks
        List<RoadmapTask> oldTasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(savedRoadmap);
        if (!oldTasks.isEmpty()) {
            roadmapTaskRepository.deleteAll(oldTasks);
        }

        // 6. Parse Gemini JSON tasks or fallback to missing skills
        List<RoadmapTask> tasks = parseGeminiRoadmapTasks(savedRoadmap, geminiJson, missingSkills, targetRole);
        roadmapTaskRepository.saveAll(tasks);

        savedRoadmap.setTasks(roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(savedRoadmap));
        System.out.println("ROADMAP SAVED");

        return savedRoadmap;
    }

    private List<RoadmapTask> parseGeminiRoadmapTasks(Roadmap roadmap, String geminiJson, List<String> missingSkills, String targetRole) {
        List<RoadmapTask> tasks = new ArrayList<>();

        if (geminiJson != null && !geminiJson.isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(geminiJson);
                JsonNode weeksArray = root.get("weeks");

                if (weeksArray != null && weeksArray.isArray() && weeksArray.size() > 0) {
                    for (int i = 0; i < weeksArray.size(); i++) {
                        JsonNode weekObj = weeksArray.get(i);
                        RoadmapTask task = new RoadmapTask();

                        task.setRoadmap(roadmap);
                        task.setWeekNumber(weekObj.has("week") ? weekObj.get("week").asInt(i + 1) : i + 1);

                        String skillName = weekObj.has("skillName") ? weekObj.get("skillName").asText("") : "";
                        if (skillName.isBlank() && weekObj.has("skills") && weekObj.get("skills").isArray() && weekObj.get("skills").size() > 0) {
                            skillName = weekObj.get("skills").get(0).asText("Core Concepts");
                        }
                        if (skillName.isBlank()) skillName = "Core Concepts";
                        task.setSkillName(skillName);

                        String title = weekObj.has("title") ? weekObj.get("title").asText("Learn " + skillName) : "Learn " + skillName;
                        task.setTitle(title);

                        String description = weekObj.has("description") ? weekObj.get("description").asText("") : "";
                        if (description.isBlank()) {
                            String outcome = weekObj.has("outcome") ? weekObj.get("outcome").asText("") : "";
                            description = "Focus on " + skillName + ". " + outcome;
                        }
                        task.setDescription(description);

                        String url = weekObj.has("resourceUrl") ? weekObj.get("resourceUrl").asText("") : "";
                        if (url.isBlank()) {
                            url = getResourceUrlForSkill(skillName);
                        }
                        task.setResourceUrl(url);
                        task.setCompleted(false);
                        task.setStatus("PENDING");
                        task.setDueDate(java.time.LocalDate.now().plusDays(i));

                        tasks.add(task);
                    }
                    return tasks;
                }
            } catch (Exception e) {
                System.err.println("Jackson parse fallback for Gemini roadmap: " + e.getMessage());
            }
        }

        // Fallback: Generate tasks from missing skills list
        int week = 1;
        List<String> skillsToUse = (missingSkills != null && !missingSkills.isEmpty()) ? missingSkills :
                List.of("Core Frameworks", "REST APIs", "Database Systems", "Testing & Deployment");

        for (String skill : skillsToUse) {
            RoadmapTask task = new RoadmapTask();
            task.setRoadmap(roadmap);
            task.setWeekNumber(week);
            task.setSkillName(skill);
            task.setTitle("Master " + skill + " Fundamentals");
            task.setDescription("Study core concepts of " + skill + " and build a practical mini-project for your " + targetRole + " profile.");
            task.setResourceUrl(getResourceUrlForSkill(skill));
            task.setCompleted(false);
            task.setStatus("PENDING");
            task.setDueDate(java.time.LocalDate.now().plusDays(week - 1));
            tasks.add(task);
            week++;
        }

        return tasks;
    }

    private String getResourceUrlForSkill(String skill) {
        String s = skill.toLowerCase(Locale.ROOT);
        if (s.contains("spring")) return "https://spring.io/guides";
        if (s.contains("android") || s.contains("kotlin") || s.contains("compose")) return "https://developer.android.com";
        if (s.contains("python") || s.contains("pandas") || s.contains("numpy")) return "https://docs.python.org/3/";
        if (s.contains("sql") || s.contains("mysql")) return "https://dev.mysql.com/doc/";
        if (s.contains("react") || s.contains("javascript") || s.contains("html")) return "https://developer.mozilla.org";
        return "https://www.geeksforgeeks.org";
    }

    @Transactional(readOnly = true)
    public List<Roadmap> getRoadmaps(User user) {
        List<Roadmap> list = roadmapRepository.findByUser(user);
        System.out.println("ROADMAP FETCHED");
        return list;
    }

    @Transactional
    public RoadmapTask toggleTaskCompletion(User user, Long taskId, boolean completed) {
        RoadmapTask task = roadmapTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getRoadmap().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: Task does not belong to this user");
        }

        task.setCompleted(completed);
        task.setStatus(completed ? "MASTERY_PENDING" : "PENDING");
        return roadmapTaskRepository.save(task);
    }

    @Transactional
    public List<RoadmapTask> toggleWeekCompletion(User user, int weekNumber, boolean completed) {
        List<Roadmap> roadmaps = roadmapRepository.findByUser(user);
        if (roadmaps.isEmpty()) {
            throw new RuntimeException("No roadmap found for user");
        }

        Roadmap roadmap = roadmaps.stream()
                .max(Comparator.comparing(Roadmap::getId))
                .orElseThrow(() -> new RuntimeException("No active roadmap found"));

        List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(roadmap)
                .stream()
                .filter(t -> t.getWeekNumber() == weekNumber)
                .toList();

        for (RoadmapTask task : tasks) {
            task.setCompleted(completed);
            task.setStatus(completed ? "MASTERY_PENDING" : "PENDING");
        }

        return roadmapTaskRepository.saveAll(tasks);
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