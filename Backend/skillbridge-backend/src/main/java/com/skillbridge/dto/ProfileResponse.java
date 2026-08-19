package com.skillbridge.dto;

public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String targetJob;
    private String targetCompany;
    private String targetSkills;
    private String recommendedSkills;
    private String interviewTopics;
    private String companyPreparation;

    public ProfileResponse() {
    }

    public ProfileResponse(Long id, String fullName, String email, String college) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.college = college;
    }

    public ProfileResponse(Long id, String fullName, String email, String college,
                           String targetJob, String targetCompany, String targetSkills,
                           String recommendedSkills, String interviewTopics, String companyPreparation) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.college = college;
        this.targetJob = targetJob;
        this.targetCompany = targetCompany;
        this.targetSkills = targetSkills;
        this.recommendedSkills = recommendedSkills;
        this.interviewTopics = interviewTopics;
        this.companyPreparation = companyPreparation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTargetJob() {
        return targetJob;
    }

    public void setTargetJob(String targetJob) {
        this.targetJob = targetJob;
    }

    public String getTargetCompany() {
        return targetCompany;
    }

    public void setTargetCompany(String targetCompany) {
        this.targetCompany = targetCompany;
    }

    public String getTargetSkills() {
        return targetSkills;
    }

    public void setTargetSkills(String targetSkills) {
        this.targetSkills = targetSkills;
    }

    public String getRecommendedSkills() {
        return recommendedSkills;
    }

    public void setRecommendedSkills(String recommendedSkills) {
        this.recommendedSkills = recommendedSkills;
    }

    public String getInterviewTopics() {
        return interviewTopics;
    }

    public void setInterviewTopics(String interviewTopics) {
        this.interviewTopics = interviewTopics;
    }

    public String getCompanyPreparation() {
        return companyPreparation;
    }

    public void setCompanyPreparation(String companyPreparation) {
        this.companyPreparation = companyPreparation;
    }
}