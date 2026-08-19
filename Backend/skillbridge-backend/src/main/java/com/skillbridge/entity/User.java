package com.skillbridge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private String college;

    @JsonIgnore
    private String password;

    private boolean notificationsEnabled = true;
    private String reminderTime = "20:00";
    private String quietHoursStart = "22:00";
    private String quietHoursEnd = "08:00";

    private String targetJob;
    private String targetCompany;

    @Column(columnDefinition = "TEXT")
    private String targetSkills;

    @Column(columnDefinition = "TEXT")
    private String recommendedSkills;

    @Column(columnDefinition = "TEXT")
    private String interviewTopics;

    @Column(columnDefinition = "TEXT")
    private String companyPreparation;

    // Constructors
    public User() {
    }

    public User(Long id, String fullName, String email, String college, String password) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.college = college;
        this.password = password;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getCollege() {
        return college;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(String quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }

    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(String quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
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