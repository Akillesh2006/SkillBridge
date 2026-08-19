package com.skillbridge.controller;

import com.skillbridge.entity.User;
import com.skillbridge.entity.UserSkill;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.repository.UserSkillRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/skills")
public class UserSkillController {

    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    public UserSkillController(
            UserSkillRepository userSkillRepository,
            UserRepository userRepository) {

        this.userSkillRepository = userSkillRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserSkill> getMySkills(Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return userSkillRepository.findByUser(user);
    }
}