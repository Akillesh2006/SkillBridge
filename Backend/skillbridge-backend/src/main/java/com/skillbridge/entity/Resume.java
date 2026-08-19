package com.skillbridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String filePath;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String aiAnalysis;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Resume() {
    }

    public Resume(
            Long id,
            String fileName,
            String filePath,
            String extractedText,
            String aiAnalysis,
            User user
    ) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.extractedText = extractedText;
        this.aiAnalysis = aiAnalysis;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public String getAiAnalysis() {
        return aiAnalysis;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }

    public void setUser(User user) {
        this.user = user;
    }
}