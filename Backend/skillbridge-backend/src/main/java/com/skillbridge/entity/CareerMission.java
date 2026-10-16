package com.skillbridge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "career_missions", indexes = {
        @Index(name = "idx_mission_user", columnList = "user_id"),
        @Index(name = "idx_mission_user_completed", columnList = "user_id, completed")
})
public class CareerMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String targetJob;

    private String title;

    @Column(length = 3000)
    private String scenario;

    @Column(length = 3000)
    private String taskInstructions;

    private String expectedResponseType = "Technical Plan & Implementation";

    private String difficulty = "INTERMEDIATE";

    @Column(length = 1000)
    private String skillsEvaluated;

    private Integer estimatedMinutes = 15;

    @Column(length = 4000)
    private String studentResponse;

    private Integer score;

    private Integer technicalScore;

    private Integer problemSolvingScore;

    private Integer clarityScore;

    private Integer architectureScore;

    private Integer codeQualityScore;

    private Integer testingScore;

    private Integer documentationScore;

    private String githubUrl;

    @Column(length = 4000)
    private String projectRequirements;

    @Column(length = 2000)
    private String evaluationCriteria;

    @Column(length = 2000)
    private String strengths;

    @Column(length = 2000)
    private String needsImprovement;

    @Column(length = 2000)
    private String missingConcepts;

    @Column(length = 2000)
    private String recommendedNextStep;

    @Column(length = 2000)
    private String feedback;

    private String status = "GENERATED"; // GENERATED, IN_PROGRESS, COMPLETED

    @Column(length = 2000)
    private String whyThisMission;

    private Integer requirementsScore;

    private Integer technologyScore;

    private Integer careerRelevanceScore;

    @Column(length = 3000)
    private String completedRequirements;

    @Column(length = 3000)
    private String missingRequirements;

    private boolean completed = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime completedAt;

    public CareerMission() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWhyThisMission() {
        return whyThisMission;
    }

    public void setWhyThisMission(String whyThisMission) {
        this.whyThisMission = whyThisMission;
    }

    public Integer getRequirementsScore() {
        return requirementsScore;
    }

    public void setRequirementsScore(Integer requirementsScore) {
        this.requirementsScore = requirementsScore;
    }

    public Integer getTechnologyScore() {
        return technologyScore;
    }

    public void setTechnologyScore(Integer technologyScore) {
        this.technologyScore = technologyScore;
    }

    public Integer getCareerRelevanceScore() {
        return careerRelevanceScore;
    }

    public void setCareerRelevanceScore(Integer careerRelevanceScore) {
        this.careerRelevanceScore = careerRelevanceScore;
    }

    public String getCompletedRequirements() {
        return completedRequirements;
    }

    public void setCompletedRequirements(String completedRequirements) {
        this.completedRequirements = completedRequirements;
    }

    public String getMissingRequirements() {
        return missingRequirements;
    }

    public void setMissingRequirements(String missingRequirements) {
        this.missingRequirements = missingRequirements;
    }

    public Integer getArchitectureScore() {
        return architectureScore;
    }

    public void setArchitectureScore(Integer architectureScore) {
        this.architectureScore = architectureScore;
    }

    public Integer getCodeQualityScore() {
        return codeQualityScore;
    }

    public void setCodeQualityScore(Integer codeQualityScore) {
        this.codeQualityScore = codeQualityScore;
    }

    public Integer getTestingScore() {
        return testingScore;
    }

    public void setTestingScore(Integer testingScore) {
        this.testingScore = testingScore;
    }

    public Integer getDocumentationScore() {
        return documentationScore;
    }

    public void setDocumentationScore(Integer documentationScore) {
        this.documentationScore = documentationScore;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getProjectRequirements() {
        return projectRequirements;
    }

    public void setProjectRequirements(String projectRequirements) {
        this.projectRequirements = projectRequirements;
    }

    public String getEvaluationCriteria() {
        return evaluationCriteria;
    }

    public void setEvaluationCriteria(String evaluationCriteria) {
        this.evaluationCriteria = evaluationCriteria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTargetJob() {
        return targetJob;
    }

    public void setTargetJob(String targetJob) {
        this.targetJob = targetJob;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getTaskInstructions() {
        return taskInstructions;
    }

    public void setTaskInstructions(String taskInstructions) {
        this.taskInstructions = taskInstructions;
    }

    public String getExpectedResponseType() {
        return expectedResponseType;
    }

    public void setExpectedResponseType(String expectedResponseType) {
        this.expectedResponseType = expectedResponseType;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getSkillsEvaluated() {
        return skillsEvaluated;
    }

    public void setSkillsEvaluated(String skillsEvaluated) {
        this.skillsEvaluated = skillsEvaluated;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getStudentResponse() {
        return studentResponse;
    }

    public void setStudentResponse(String studentResponse) {
        this.studentResponse = studentResponse;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTechnicalScore() {
        return technicalScore;
    }

    public void setTechnicalScore(Integer technicalScore) {
        this.technicalScore = technicalScore;
    }

    public Integer getProblemSolvingScore() {
        return problemSolvingScore;
    }

    public void setProblemSolvingScore(Integer problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }

    public Integer getClarityScore() {
        return clarityScore;
    }

    public void setClarityScore(Integer clarityScore) {
        this.clarityScore = clarityScore;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getNeedsImprovement() {
        return needsImprovement;
    }

    public void setNeedsImprovement(String needsImprovement) {
        this.needsImprovement = needsImprovement;
    }

    public String getMissingConcepts() {
        return missingConcepts;
    }

    public void setMissingConcepts(String missingConcepts) {
        this.missingConcepts = missingConcepts;
    }

    public String getRecommendedNextStep() {
        return recommendedNextStep;
    }

    public void setRecommendedNextStep(String recommendedNextStep) {
        this.recommendedNextStep = recommendedNextStep;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
