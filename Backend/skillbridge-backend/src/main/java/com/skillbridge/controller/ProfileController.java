package com.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.dto.ProfileResponse;
import com.skillbridge.dto.UpdateProfileRequest;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiService geminiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/user/profile")
    public ProfileResponse getProfile(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCollege(),
                user.getTargetJob(),
                user.getTargetCompany(),
                user.getTargetSkills(),
                user.getRecommendedSkills(),
                user.getInterviewTopics(),
                user.getCompanyPreparation()
        );
    }

    @PutMapping("/user/profile")
    public ProfileResponse updateProfile(Authentication authentication,
                                         @RequestBody UpdateProfileRequest request) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        user.setFullName(request.getFullName());
        user.setCollege(request.getCollege());

        String oldJob = user.getTargetJob();
        String oldCompany = user.getTargetCompany();

        String newJob = request.getTargetJob();
        String newCompany = request.getTargetCompany();

        user.setTargetJob(newJob);
        user.setTargetCompany(newCompany);

        // If job or company changes, regenerate target requirements via Gemini
        if (newJob != null && !newJob.isBlank() && 
            (!newJob.equalsIgnoreCase(oldJob) || (newCompany != null && !newCompany.equalsIgnoreCase(oldCompany)))) {
            
            String jsonResult = geminiService.generateTargetProfile(newJob, newCompany);
            try {
                JsonNode node = objectMapper.readTree(jsonResult);
                user.setTargetSkills(node.path("targetSkills").toString());
                user.setRecommendedSkills(node.path("recommendedSkills").toString());
                user.setInterviewTopics(node.path("interviewTopics").toString());
                user.setCompanyPreparation(node.path("companyPreparation").toString());
            } catch (Exception e) {
                System.err.println("Failed to parse target profile requirements JSON: " + e.getMessage());
            }
        }

        userRepository.save(user);

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCollege(),
                user.getTargetJob(),
                user.getTargetCompany(),
                user.getTargetSkills(),
                user.getRecommendedSkills(),
                user.getInterviewTopics(),
                user.getCompanyPreparation()
        );
    }
}