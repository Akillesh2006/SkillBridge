package com.skillbridge.controller;

import com.skillbridge.service.GeminiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gemini-test")
public class GeminiTestController {

    private final GeminiService geminiService;

    public GeminiTestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping
    public String testGemini() {
        return geminiService.analyzeResume(
                """
                Akillesh K P
                B.E Computer Science and Engineering
                Skills: Java, Python, Kotlin, Android, MySQL, Git
                Projects: Android application development
                """
        );
    }
}