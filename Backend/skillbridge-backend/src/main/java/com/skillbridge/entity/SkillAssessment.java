package com.skillbridge.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skill_assessments", indexes = {
        @Index(name = "idx_assessment_user", columnList = "user_id"),
        @Index(name = "idx_assessment_user_task", columnList = "user_id, roadmap_task_id")
})
public class SkillAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer score; // overall score percentage (0-100)

    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "roadmap_task_id", nullable = true)
    private RoadmapTask roadmapTask;

    private String assessmentType = "VERIFICATION";

    private String masteryLevel;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String weakConcepts;

    @Column(columnDefinition = "TEXT")
    private String aiFeedback;

    @Column(columnDefinition = "TEXT")
    private String recommendedPractice;

    private Integer conceptualScore;
    private Integer applicationScore;
    private Integer problemSolvingScore;
    private Integer debuggingScore;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentQuestion> questions = new ArrayList<>();

    public SkillAssessment() {
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public RoadmapTask getRoadmapTask() {
        return roadmapTask;
    }

    public void setRoadmapTask(RoadmapTask roadmapTask) {
        this.roadmapTask = roadmapTask;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(String masteryLevel) {
        this.masteryLevel = masteryLevel;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeakConcepts() {
        return weakConcepts;
    }

    public void setWeakConcepts(String weakConcepts) {
        this.weakConcepts = weakConcepts;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public String getRecommendedPractice() {
        return recommendedPractice;
    }

    public void setRecommendedPractice(String recommendedPractice) {
        this.recommendedPractice = recommendedPractice;
    }

    public Integer getConceptualScore() {
        return conceptualScore;
    }

    public void setConceptualScore(Integer conceptualScore) {
        this.conceptualScore = conceptualScore;
    }

    public Integer getApplicationScore() {
        return applicationScore;
    }

    public void setApplicationScore(Integer applicationScore) {
        this.applicationScore = applicationScore;
    }

    public Integer getProblemSolvingScore() {
        return problemSolvingScore;
    }

    public void setProblemSolvingScore(Integer problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }

    public Integer getDebuggingScore() {
        return debuggingScore;
    }

    public void setDebuggingScore(Integer debuggingScore) {
        this.debuggingScore = debuggingScore;
    }

    public List<AssessmentQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AssessmentQuestion> questions) {
        this.questions = questions;
    }
}
