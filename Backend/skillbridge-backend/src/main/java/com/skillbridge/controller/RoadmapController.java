package com.skillbridge.controller;

import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.RoadmapTask;
import com.skillbridge.entity.User;
import com.skillbridge.repository.RoadmapTaskRepository;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.RoadmapService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final UserRepository userRepository;
    private final RoadmapTaskRepository roadmapTaskRepository;

    public RoadmapController(
            RoadmapService roadmapService,
            UserRepository userRepository,
            RoadmapTaskRepository roadmapTaskRepository) {
        this.roadmapService = roadmapService;
        this.userRepository = userRepository;
        this.roadmapTaskRepository = roadmapTaskRepository;
    }

    /**
     * Generate a personalized roadmap based on
     * the user's detected skills and selected job role.
     */
    @PostMapping("/generate")
    public Map<String, Object> generateRoadmap(
            Authentication authentication,
            @RequestParam(required = false) String roleName) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String targetRole = (roleName != null && !roleName.isBlank()) ? roleName : "Android Developer";

        Roadmap roadmap =
                roadmapService.generateRoadmap(user, targetRole);

        return buildResponse(roadmap);
    }

    /**
     * Get an existing roadmap for the logged-in user.
     */
    @GetMapping
    public List<Map<String, Object>> getRoadmaps(
            Authentication authentication) {
        long startTime = System.currentTimeMillis();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Roadmap> roadmaps = roadmapService.getRoadmaps(user);
        List<Map<String, Object>> result = roadmaps
                .stream()
                .map(this::buildResponse)
                .toList();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] GetRoadmaps DB query = " + duration + " ms (Found: " + result.size() + " roadmaps)");
        return result;
    }

    @PatchMapping("/tasks/{taskId}/completion")
    public List<Map<String, Object>> toggleTaskCompletion(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestBody Map<String, Boolean> request) {
        long startTime = System.currentTimeMillis();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean completed = request != null && request.getOrDefault("completed", false);

        roadmapService.toggleTaskCompletion(user, taskId, completed);

        List<Map<String, Object>> result = roadmapService.getRoadmaps(user)
                .stream()
                .map(this::buildResponse)
                .toList();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] ToggleTaskCompletion = " + duration + " ms (TaskId: " + taskId + ")");
        return result;
    }

    @PatchMapping("/weeks/{weekNumber}/completion")
    public List<Map<String, Object>> toggleWeekCompletion(
            Authentication authentication,
            @PathVariable int weekNumber,
            @RequestBody Map<String, Boolean> request) {
        long startTime = System.currentTimeMillis();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean completed = request != null && request.getOrDefault("completed", false);

        roadmapService.toggleWeekCompletion(user, weekNumber, completed);

        List<Map<String, Object>> result = roadmapService.getRoadmaps(user)
                .stream()
                .map(this::buildResponse)
                .toList();
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] ToggleWeekCompletion = " + duration + " ms (Week: " + weekNumber + ")");
        return result;
    }

    private Map<String, Object> buildResponse(Roadmap roadmap) {
        if (roadmap == null) return Collections.emptyMap();

        List<RoadmapTask> taskList = roadmap.getTasks();
        if (taskList == null || taskList.isEmpty()) {
            taskList = roadmapTaskRepository.findByRoadmapOrderByWeekNumberAsc(roadmap);
        }

        List<Map<String, Object>> tasks = taskList != null
                ? taskList.stream().map(this::buildTaskResponse).toList()
                : Collections.emptyList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", roadmap.getId() != null ? roadmap.getId() : 1L);
        response.put("roleName", roadmap.getRoleName() != null ? roadmap.getRoleName() : "Software Developer");
        response.put("matchPercentage", roadmap.getMatchPercentage() != null ? roadmap.getMatchPercentage() : 0.0);
        response.put("readinessScore", roadmap.getReadinessScore() != null ? roadmap.getReadinessScore() : 0.0);
        response.put("createdAt", roadmap.getCreatedAt() != null ? roadmap.getCreatedAt().toString() : java.time.LocalDateTime.now().toString());
        response.put("tasks", tasks);

        return response;
    }

    private Map<String, Object> buildTaskResponse(RoadmapTask task) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", task.getId() != null ? task.getId() : 0L);
        response.put("weekNumber", task.getWeekNumber());
        response.put("skillName", task.getSkillName() != null ? task.getSkillName() : "Core Skills");
        response.put("title", task.getTitle() != null ? task.getTitle() : "Task");
        response.put("description", task.getDescription() != null ? task.getDescription() : "");
        response.put("resourceUrl", task.getResourceUrl() != null ? task.getResourceUrl() : "");
        response.put("completed", task.isCompleted());
        response.put("status", task.getStatus() != null ? task.getStatus() : "PENDING");
        response.put("lastAssessmentId", null);

        return response;
    }
}