package com.skillbridge.repository;

import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    @Modifying
    @Transactional
    void deleteByUser(User user);
}