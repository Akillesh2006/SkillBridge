package com.skillbridge.controller;

import com.skillbridge.entity.Roadmap;
import com.skillbridge.entity.RoadmapTask;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.RoadmapService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/roadmap")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final UserRepository userRepository;
    private final com.skillbridge.repository.SkillAssessmentRepository skillAssessmentRepository;

    public RoadmapController(
            RoadmapService roadmapService,
            UserRepository userRepository,
            com.skillbridge.repository.SkillAssessmentRepository skillAssessmentRepository) {
        this.roadmapService = roadmapService;
        this.userRepository = userRepository;
        this.skillAssessmentRepository = skillAssessmentRepository;
    }

    /**
     * Generate a personalized roadmap based on
     * the user's detected skills and selected job role.
     *
     * Example:
     * POST /roadmap/generate?roleName=Java%20Backend%20Developer
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

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return roadmapService.getRoadmaps(user)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @PatchMapping("/tasks/{taskId}/completion")
    public List<Map<String, Object>> toggleTaskCompletion(
            Authentication authentication,
            @PathVariable Long taskId,
            @RequestBody Map<String, Boolean> request) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean completed = request.getOrDefault("completed", false);

        roadmapService.toggleTaskCompletion(user, taskId, completed);

        return roadmapService.getRoadmaps(user)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    @PatchMapping("/weeks/{weekNumber}/completion")
    public List<Map<String, Object>> toggleWeekCompletion(
            Authentication authentication,
            @PathVariable int weekNumber,
            @RequestBody Map<String, Boolean> request) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean completed = request.getOrDefault("completed", false);

        roadmapService.toggleWeekCompletion(user, weekNumber, completed);

        return roadmapService.getRoadmaps(user)
                .stream()
                .map(this::buildResponse)
                .toList();
    }

    private Map<String, Object> buildResponse(Roadmap roadmap) {

        List<Map<String, Object>> tasks =
                roadmap.getTasks()
                        .stream()
                        .map(this::buildTaskResponse)
                        .toList();

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", roadmap.getId());
        response.put("roleName", roadmap.getRoleName());
        response.put("matchPercentage",
                roadmap.getMatchPercentage());
        response.put("readinessScore",
                roadmap.getReadinessScore());
        response.put("createdAt",
                roadmap.getCreatedAt());
        response.put("tasks", tasks);

        return response;
    }

    private Map<String, Object> buildTaskResponse(
            RoadmapTask task) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put("id", task.getId());
        response.put("weekNumber", task.getWeekNumber());
        response.put("skillName", task.getSkillName());
        response.put("title", task.getTitle());
        response.put("description",
                task.getDescription());
        response.put("resourceUrl",
                task.getResourceUrl());
        response.put("completed",
                task.isCompleted());
        response.put("status",
                task.getStatus());

        java.util.Optional<com.skillbridge.entity.SkillAssessment> latestOpt = skillAssessmentRepository
                .findFirstByUserAndRoadmapTaskOrderByCreatedDateDesc(task.getRoadmap().getUser(), task);
        response.put("lastAssessmentId", latestOpt.isPresent() ? latestOpt.get().getId() : null);

        return response;
    }
}