package com.skillbridge.dto;

public class UpdateProfileRequest {

    private String fullName;
    private String college;
    private String targetJob;
    private String targetCompany;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullName, String college) {
        this.fullName = fullName;
        this.college = college;
    }

    public UpdateProfileRequest(String fullName, String college, String targetJob, String targetCompany) {
        this.fullName = fullName;
        this.college = college;
        this.targetJob = targetJob;
        this.targetCompany = targetCompany;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
}