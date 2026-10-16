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
    public Roadmap generateFastDynamicRoadmap(User user, String roleName) {
        return generateRoadmap(user, roleName);
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

        // 6. Parse tasks and attach to roadmap's Hibernate-managed collection
        List<RoadmapTask> newTasks = parseGeminiRoadmapTasks(roadmap, geminiJson, missingSkills, targetRole);
        roadmap.setTasks(newTasks);

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);
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
                        int weekNum = weekObj.has("week") ? weekObj.get("week").asInt(i + 1) : i + 1;

                        String skillName = weekObj.has("skillName") ? weekObj.get("skillName").asText("") : "";
                        if (skillName.isBlank() && weekObj.has("skills") && weekObj.get("skills").isArray() && weekObj.get("skills").size() > 0) {
                            skillName = weekObj.get("skills").get(0).asText("Core Concepts");
                        }
                        if (skillName.isBlank()) skillName = "Core Concepts";

                        String weekTitle = weekObj.has("title") ? weekObj.get("title").asText("Module " + weekNum + " — " + skillName) : "Module " + weekNum + " — " + skillName;
                        String weekDesc = weekObj.has("description") ? weekObj.get("description").asText("") : "Master core principles and practical exercises for " + skillName + ".";
                        String weekUrl = weekObj.has("resourceUrl") ? weekObj.get("resourceUrl").asText("") : getResourceUrlForSkill(skillName);

                        JsonNode taskNodes = weekObj.get("tasks");
                        if (taskNodes != null && taskNodes.isArray() && taskNodes.size() > 0) {
                            for (int t = 0; t < taskNodes.size(); t++) {
                                String taskTitle = taskNodes.get(t).asText("Task " + (t + 1));
                                RoadmapTask task = new RoadmapTask();
                                task.setRoadmap(roadmap);
                                task.setWeekNumber(weekNum);
                                task.setSkillName(skillName);
                                task.setTitle(taskTitle);
                                task.setDescription(weekDesc);
                                task.setResourceUrl(weekUrl);
                                task.setCompleted(false);
                                task.setStatus("PENDING");
                                task.setDueDate(java.time.LocalDate.now().plusDays(i * 7L + t * 2L));
                                tasks.add(task);
                            }
                        } else {
                            RoadmapTask task1 = new RoadmapTask();
                            task1.setRoadmap(roadmap);
                            task1.setWeekNumber(weekNum);
                            task1.setSkillName(skillName);
                            task1.setTitle(weekTitle);
                            task1.setDescription(weekDesc);
                            task1.setResourceUrl(weekUrl);
                            task1.setCompleted(false);
                            task1.setStatus("PENDING");
                            task1.setDueDate(java.time.LocalDate.now().plusDays(i * 7L));
                            tasks.add(task1);

                            RoadmapTask task2 = new RoadmapTask();
                            task2.setRoadmap(roadmap);
                            task2.setWeekNumber(weekNum);
                            task2.setSkillName(skillName);
                            task2.setTitle("Practice: Hands-on mini project for " + skillName);
                            task2.setDescription("Build and verify a practical implementation applying " + skillName + " principles.");
                            task2.setResourceUrl(weekUrl);
                            task2.setCompleted(false);
                            task2.setStatus("PENDING");
                            task2.setDueDate(java.time.LocalDate.now().plusDays(i * 7L + 3L));
                            tasks.add(task2);
                        }
                    }
                    if (!tasks.isEmpty()) {
                        return tasks;
                    }
                }
            } catch (Exception e) {
                System.err.println("Jackson parse fallback for Gemini roadmap: " + e.getMessage());
            }
        }

        // 4-module fallback curriculum
        return generateDynamicCurriculum(roadmap, targetRole, missingSkills);
    }

    private List<RoadmapTask> generateDynamicCurriculum(Roadmap roadmap, String targetRole, List<String> missingSkills) {
        String lowerRole = targetRole.toLowerCase(Locale.ROOT);
        List<ModuleDef> moduleDefs;

        if (lowerRole.contains("android") || lowerRole.contains("mobile")) {
            moduleDefs = List.of(
                    new ModuleDef(1, "Kotlin Fundamentals & OOP", "Kotlin", "Master Kotlin variables, control flow, functions, null safety, and OOP.",
                            List.of("Learn Kotlin syntax, basic types, null safety, and control flow", "Implement OOP classes, sealed classes, and extension functions")),
                    new ModuleDef(2, "Jetpack Compose & UI Design", "Jetpack Compose", "Build modern responsive user interfaces with Jetpack Compose.",
                            List.of("Create Composable layouts with Column, Row, Box, and LazyColumn", "Implement state hoisting, remember, and Material 3 design")),
                    new ModuleDef(3, "Coroutines, MVVM & Networking", "Retrofit", "Asynchronous architecture with Coroutines, StateFlow, and Retrofit REST APIs.",
                            List.of("Handle async operations with Coroutines, Dispatchers, and StateFlow", "Consume REST APIs with Retrofit and implement MVVM architecture")),
                    new ModuleDef(4, "Room Database, Testing & Deployment", "Room Database", "Local data persistence, automated testing, and capstone project.",
                            List.of("Store offline data with Room database entities and DAOs", "Write unit tests and build release capstone project"))
            );
        } else if (lowerRole.contains("data scientist") || lowerRole.contains("data science") || lowerRole.contains("analyst")) {
            moduleDefs = List.of(
                    new ModuleDef(1, "Python & Numerical Computing", "Python & NumPy", "Master Python data structures, functions, and NumPy vector operations.",
                            List.of("Write Python scripts with data structures and comprehensions", "Perform array math and statistical aggregates with NumPy")),
                    new ModuleDef(2, "Data Wrangling with Pandas", "Pandas", "Clean, transform, filter, and aggregate complex datasets.",
                            List.of("Load CSV/JSON into Pandas DataFrames and clean missing values", "Perform groupby aggregations, pivots, and data merges")),
                    new ModuleDef(3, "SQL & Exploratory Data Analysis", "SQL & Matplotlib", "Query relational databases and visualize distributions.",
                            List.of("Write complex SQL queries with joins, window functions, and aggregates", "Generate exploratory plots and correlations with Matplotlib and Seaborn")),
                    new ModuleDef(4, "Machine Learning & Capstone Modeling", "Scikit-Learn", "Train predictive ML models, evaluate metrics, and deploy findings.",
                            List.of("Train Regression and Classification models with Scikit-learn", "Evaluate models with ROC-AUC, RMSE, and deliver capstone insights"))
            );
        } else {
            // Java Backend Developer / Full Stack / Default
            moduleDefs = List.of(
                    new ModuleDef(1, "Core Java & OOP Architecture", "Java", "Master Java OOP, Collections Framework, Streams, and Generics.",
                            List.of("Implement OOP principles, Interfaces, and Collections (List, Set, Map)", "Write functional stream processing and exception handling")),
                    new ModuleDef(2, "Spring Boot & RESTful APIs", "Spring Boot", "Build REST APIs with Spring MVC, Dependency Injection, and validation.",
                            List.of("Create @RestController endpoints with proper HTTP verbs and DTOs", "Configure dependency injection, beans, and validation handlers")),
                    new ModuleDef(3, "Database Persistence with JPA & Hibernate", "JPA & MySQL", "Map database entities, write JPQL queries, and manage transactions.",
                            List.of("Define JPA entities, relationships (@OneToMany, @ManyToOne), and indices", "Write Spring Data JpaRepository methods and manage @Transactional boundaries")),
                    new ModuleDef(4, "Security, Testing & Capstone Deployment", "Spring Security", "Secure APIs with JWT, write automated tests, and deploy backend service.",
                            List.of("Implement JWT authentication filter and password encryption", "Write JUnit 5 unit and integration tests for service and endpoints"))
            );
        }

        List<RoadmapTask> tasks = new ArrayList<>();
        for (ModuleDef mod : moduleDefs) {
            for (int t = 0; t < mod.tasks.size(); t++) {
                RoadmapTask task = new RoadmapTask();
                task.setRoadmap(roadmap);
                task.setWeekNumber(mod.weekNumber);
                task.setSkillName(mod.skillName);
                task.setTitle(mod.tasks.get(t));
                task.setDescription(mod.description);
                task.setResourceUrl(getResourceUrlForSkill(mod.skillName));
                task.setCompleted(false);
                task.setStatus("PENDING");
                task.setDueDate(java.time.LocalDate.now().plusDays((mod.weekNumber - 1) * 7L + t * 3L));
                tasks.add(task);
            }
        }
        return tasks;
    }

    private static class ModuleDef {
        int weekNumber;
        String title;
        String skillName;
        String description;
        List<String> tasks;

        ModuleDef(int weekNumber, String title, String skillName, String description, List<String> tasks) {
            this.weekNumber = weekNumber;
            this.title = title;
            this.skillName = skillName;
            this.description = description;
            this.tasks = tasks;
        }
    }

    private String getResourceUrlForSkill(String skill) {
        String s = skill.toLowerCase(Locale.ROOT);
        if (s.contains("spring") || s.contains("jpa") || s.contains("hibernate")) return "https://spring.io/guides";
        if (s.contains("android") || s.contains("kotlin") || s.contains("compose") || s.contains("room")) return "https://developer.android.com";
        if (s.contains("python") || s.contains("pandas") || s.contains("numpy") || s.contains("scikit")) return "https://docs.python.org/3/";
        if (s.contains("sql") || s.contains("mysql")) return "https://dev.mysql.com/doc/";
        if (s.contains("react") || s.contains("javascript") || s.contains("html") || s.contains("css")) return "https://developer.mozilla.org";
        if (s.contains("docker") || s.contains("devops")) return "https://docs.docker.com";
        return "https://www.geeksforgeeks.org";
    }

    @Transactional
    public List<Roadmap> getRoadmaps(User user) {
        List<Roadmap> list = roadmapRepository.findByUser(user);
        if (list.isEmpty()) {
            String currentRole = (user.getTargetJob() != null && !user.getTargetJob().isBlank())
                    ? user.getTargetJob()
                    : "Android Developer";
            Roadmap generated = generateRoadmap(user, currentRole);
            return List.of(generated);
        }

        // Return the existing roadmaps with their tasks intact
        for (Roadmap r : list) {
            List<RoadmapTask> tasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(r);
            r.setTasks(tasks);
        }

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
        task.setStatus(completed ? "COMPLETED" : "PENDING");
        RoadmapTask savedTask = roadmapTaskRepository.save(task);

        // Recalculate roadmap overall progress percentage
        Roadmap roadmap = task.getRoadmap();
        List<RoadmapTask> allTasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(roadmap);
        if (!allTasks.isEmpty()) {
            long completedCount = allTasks.stream().filter(RoadmapTask::isCompleted).count();
            double pct = Math.round((completedCount * 100.0 / allTasks.size()) * 100.0) / 100.0;
            roadmap.setMatchPercentage(pct);
            roadmap.setReadinessScore(pct);
            roadmapRepository.save(roadmap);
        }

        return savedTask;
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
            task.setStatus(completed ? "COMPLETED" : "PENDING");
        }

        List<RoadmapTask> saved = roadmapTaskRepository.saveAll(tasks);

        // Recalculate overall progress
        List<RoadmapTask> allTasks = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(roadmap);
        if (!allTasks.isEmpty()) {
            long completedCount = allTasks.stream().filter(RoadmapTask::isCompleted).count();
            double pct = Math.round((completedCount * 100.0 / allTasks.size()) * 100.0) / 100.0;
            roadmap.setMatchPercentage(pct);
            roadmap.setReadinessScore(pct);
            roadmapRepository.save(roadmap);
        }

        return saved;
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