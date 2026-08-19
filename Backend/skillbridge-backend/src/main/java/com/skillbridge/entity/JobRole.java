package com.skillbridge.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_roles")
public class JobRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roleName;

    @Column(length = 5000)
    private String requiredSkills;

    public JobRole() {
    }

    public JobRole(Long id, String roleName, String requiredSkills) {
        this.id = id;
        this.roleName = roleName;
        this.requiredSkills = requiredSkills;
    }

    public Long getId() {
        return id;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }
}