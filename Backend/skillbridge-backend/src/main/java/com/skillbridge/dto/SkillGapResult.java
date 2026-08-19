package com.skillbridge.dto;

import java.util.List;

public class SkillGapResult {

    private String roleName;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private double matchPercentage;
    private double readinessScore;
    private List<SkillDetail> skillDetails;

    public SkillGapResult() {
    }

    public SkillGapResult(
            String roleName,
            List<String> matchedSkills,
            List<String> missingSkills,
            double matchPercentage,
            double readinessScore,
            List<SkillDetail> skillDetails
    ) {
        this.roleName = roleName;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.matchPercentage = matchPercentage;
        this.readinessScore = readinessScore;
        this.skillDetails = skillDetails;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(List<String> matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public List<SkillDetail> getSkillDetails() {
        return skillDetails;
    }

    public void setSkillDetails(List<SkillDetail> skillDetails) {
        this.skillDetails = skillDetails;
    }

    public static class SkillDetail {
        private String skillName;
        private String status; // DETECTED, VERIFIED, PARTIALLY_VERIFIED, NEEDS_IMPROVEMENT, TARGET_SKILL
        private Integer score;

        public SkillDetail() {
        }

        public SkillDetail(String skillName, String status, Integer score) {
            this.skillName = skillName;
            this.status = status;
            this.score = score;
        }

        public String getSkillName() {
            return skillName;
        }

        public void setSkillName(String skillName) {
            this.skillName = skillName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}