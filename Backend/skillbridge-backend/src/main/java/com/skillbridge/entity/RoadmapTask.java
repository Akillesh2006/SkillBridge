package com.skillbridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "roadmap_tasks")
public class RoadmapTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int weekNumber;

    private String skillName;

    private String title;

    @Column(length = 2000)
    private String description;

    private String resourceUrl;

    private boolean completed;

    private String status = "PENDING"; // PENDING, IN_PROGRESS, COMPLETED, MISSED

    private java.time.LocalDate dueDate;

    @ManyToOne
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    public RoadmapTask() {
    }

    public Long getId() {
        return id;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Roadmap getRoadmap() {
        return roadmap;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setRoadmap(Roadmap roadmap) {
        this.roadmap = roadmap;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.time.LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(java.time.LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}