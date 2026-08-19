package com.skillbridge.repository;

import com.skillbridge.entity.JobRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRoleRepository extends JpaRepository<JobRole, Long> {

    Optional<JobRole> findByRoleNameIgnoreCase(String roleName);
}