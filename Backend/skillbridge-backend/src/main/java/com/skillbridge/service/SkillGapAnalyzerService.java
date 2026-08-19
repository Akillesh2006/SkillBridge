package com.skillbridge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.dto.SkillGapResult;
import com.skillbridge.entity.JobRole;
import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.JobRoleRepository;
import com.skillbridge.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class SkillGapAnalyzerService {

    private final UserSkillRepository userSkillRepository;
    private final JobRoleRepository jobRoleRepository;

    public SkillGapAnalyzerService(
            UserSkillRepository userSkillRepository,
            JobRoleRepository jobRoleRepository
    ) {
        this.userSkillRepository = userSkillRepository;
        this.jobRoleRepository = jobRoleRepository;
    }

    public SkillGapResult analyze(
            User user,
            String roleName
    ) {
        if (user.getTargetJob() != null && user.getTargetJob().equalsIgnoreCase(roleName) &&
            user.getTargetSkills() != null && !user.getTargetSkills().isBlank()) {
            
            List<String> requiredSkills = parseJsonSkills(user.getTargetSkills());
            if (!requiredSkills.isEmpty()) {
                return calculateSkillGap(user, roleName, requiredSkills);
            }
        }

        JobRole jobRole = jobRoleRepository.findByRoleNameIgnoreCase(roleName)
                .orElseGet(() -> {
                    String lower = roleName.toLowerCase(Locale.ROOT);
                    JobRole dynamicRole = new JobRole();
                    dynamicRole.setRoleName(roleName);

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
        
        List<String> requiredSkills = Arrays.stream(jobRole.getRequiredSkills().split(","))
                        .map(String::trim)
                        .filter(skill -> !skill.isBlank())
                        .toList();
        
        return calculateSkillGap(user, jobRole.getRoleName(), requiredSkills);
    }

    public SkillGapResult analyze(
            User user,
            Long roleId
    ) {
        JobRole jobRole = jobRoleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job role not found: " + roleId
                        )
                );

        List<String> requiredSkills = Arrays.stream(jobRole.getRequiredSkills().split(","))
                        .map(String::trim)
                        .filter(skill -> !skill.isBlank())
                        .toList();

        return calculateSkillGap(user, jobRole.getRoleName(), requiredSkills);
    }

    private SkillGapResult calculateSkillGap(
            User user,
            String displayRoleName,
            List<String> requiredSkills
    ) {
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);

        Set<String> userSkillNames = new HashSet<>();
        for (UserSkill userSkill : userSkills) {
            if (userSkill.getSkillName() != null) {
                userSkillNames.add(normalize(userSkill.getSkillName()));
            }
        }

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {
            if (userSkillNames.contains(normalize(requiredSkill))) {
                matchedSkills.add(requiredSkill);
            } else {
                missingSkills.add(requiredSkill);
            }
        }

        double matchPercentage = 0.0;
        if (!requiredSkills.isEmpty()) {
            matchPercentage = ((double) matchedSkills.size() / requiredSkills.size()) * 100.0;
        }
        matchPercentage = Math.round(matchPercentage * 100.0) / 100.0;

        double matchedWeightSum = 0.0;
        List<SkillGapResult.SkillDetail> skillDetails = new ArrayList<>();

        for (String requiredSkill : requiredSkills) {
            String norm = normalize(requiredSkill);
            UserSkill userSkill = userSkills.stream()
                    .filter(us -> us.getSkillName() != null && normalize(us.getSkillName()).equals(norm))
                    .findFirst()
                    .orElse(null);

            if (userSkill != null) {
                String status = userSkill.getVerificationStatus();
                if ("VERIFIED".equals(status)) {
                    matchedWeightSum += 1.0;
                } else if ("PARTIALLY_VERIFIED".equals(status)) {
                    matchedWeightSum += 0.5;
                } else if ("NEEDS_IMPROVEMENT".equals(status)) {
                    matchedWeightSum += 0.2;
                } else {
                    matchedWeightSum += 1.0;
                }

                String finalStatus = (status == null) ? "DETECTED" : status;
                skillDetails.add(new SkillGapResult.SkillDetail(requiredSkill, finalStatus, userSkill.getVerificationScore()));
            } else {
                skillDetails.add(new SkillGapResult.SkillDetail(requiredSkill, "TARGET_SKILL", null));
            }
        }

        double readinessScore = 0.0;
        if (!requiredSkills.isEmpty()) {
            readinessScore = (matchedWeightSum / requiredSkills.size()) * 100.0;
        }
        readinessScore = Math.round(readinessScore * 100.0) / 100.0;

        return new SkillGapResult(
                displayRoleName,
                matchedSkills,
                missingSkills,
                matchPercentage,
                readinessScore,
                skillDetails
        );
    }

    private List<String> parseJsonSkills(String targetSkillsJson) {
        List<String> list = new ArrayList<>();
        if (targetSkillsJson == null || targetSkillsJson.isBlank()) return list;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String[] arr = mapper.readValue(targetSkillsJson, String[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            // Manual fallback if parsing fails
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

    private String normalize(String skill) {
        return skill.trim().toLowerCase(Locale.ROOT);
    }
}