package com.skillbridge.controller;

import com.skillbridge.entity.Resume;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Resume getResume(Authentication authentication) {
        System.out.println("GET RESUME API HIT");

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return resumeService.getResume(user);
    }

    @PostMapping("/upload")
    public Resume uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("UPLOAD API HIT");

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return resumeService.uploadResume(user, file);
    }
}