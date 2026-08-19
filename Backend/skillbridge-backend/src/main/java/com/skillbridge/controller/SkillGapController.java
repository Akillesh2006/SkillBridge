package com.skillbridge.controller;

import com.skillbridge.dto.SkillGapResult;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.SkillGapAnalyzerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/skill-gap")
public class SkillGapController {

    private final SkillGapAnalyzerService skillGapAnalyzerService;
    private final UserRepository userRepository;

    public SkillGapController(
            SkillGapAnalyzerService skillGapAnalyzerService,
            UserRepository userRepository
    ) {
        this.skillGapAnalyzerService = skillGapAnalyzerService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{roleId}")
    public SkillGapResult analyzeSkillGap(
            @PathVariable Long roleId,
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        return skillGapAnalyzerService.analyze(user, roleId);
    }

    @GetMapping("/analyze")
    public SkillGapResult analyzeSkillGapByName(
            @RequestParam String roleName,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return skillGapAnalyzerService.analyze(user, roleName);
    }
}