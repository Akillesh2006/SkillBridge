package com.skillbridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills", indexes = {
        @Index(name = "idx_skill_user", columnList = "user_id"),
        @Index(name = "idx_skill_user_name", columnList = "user_id, skillName")
})
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillName;

    private String category;

    private String proficiency;

    private String verificationStatus; // VERIFIED, PARTIALLY_VERIFIED, NEEDS_IMPROVEMENT, UNVERIFIED

    private Integer verificationScore;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserSkill() {
    }

    public UserSkill(Long id, String skillName, String category,
                     String proficiency, User user) {
        this.id = id;
        this.skillName = skillName;
        this.category = category;
        this.proficiency = proficiency;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getSkillName() {
        return skillName;
    }

    public String getCategory() {
        return category;
    }

    public String getProficiency() {
        return proficiency;
    }

    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setProficiency(String proficiency) {
        this.proficiency = proficiency;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public Integer getVerificationScore() {
        return verificationScore;
    }

    public void setVerificationScore(Integer verificationScore) {
        this.verificationScore = verificationScore;
    }
}