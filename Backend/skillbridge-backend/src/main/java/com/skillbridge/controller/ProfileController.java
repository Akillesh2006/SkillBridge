package com.skillbridge.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillbridge.dto.ProfileResponse;
import com.skillbridge.dto.UpdateProfileRequest;
import com.skillbridge.entity.User;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private com.skillbridge.service.RoadmapService roadmapService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Fast static catalogue for instant target profile requirements (< 1ms)
    private static final Map<String, String[]> ROLE_PRESETS = Map.of(
            "android", new String[]{
                    "[\"Kotlin\", \"Jetpack Compose\", \"Android SDK\", \"Coroutines\", \"Room\", \"Retrofit\", \"MVVM\", \"Hilt\"]",
                    "[\"Clean Architecture\", \"Unit Testing\", \"Compose Animations\", \"WorkManager\", \"CI/CD\"]",
                    "[\"Android Lifecycle & ViewModels\", \"State Management in Compose\", \"Coroutines Flow & Channels\", \"Memory Leaks & Profiling\"]",
                    "[\"Round 1: DSA & Core Problem Solving\", \"Round 2: Android System Design & Architecture\", \"Round 3: Live Coding & Debugging Challenge\"]"
            },
            "java", new String[]{
                    "[\"Java\", \"Spring Boot\", \"Spring Security\", \"REST APIs\", \"Hibernate/JPA\", \"MySQL\", \"Microservices\"]",
                    "[\"Docker\", \"Redis\", \"Kafka\", \"Kubernetes\", \"AWS\", \"Unit Testing (JUnit/Mockito)\"]",
                    "[\"Spring Bean Lifecycle & Inversion of Control\", \"Concurrency & Multithreading\", \"Database Indexing & Query Tuning\", \"REST API Security & JWT\"]",
                    "[\"Round 1: Core Java & Data Structures\", \"Round 2: Spring Boot Architecture & Microservices Design\", \"Round 3: Low-Level System Design & Database Modeling\"]"
            },
            "data scientist", new String[]{
                    "[\"Python\", \"Pandas\", \"NumPy\", \"Scikit-learn\", \"SQL\", \"Statistics\", \"Machine Learning\", \"Data Visualization\"]",
                    "[\"Deep Learning\", \"TensorFlow/PyTorch\", \"Feature Engineering\", \"MLOps\", \"Docker\"]",
                    "[\"Supervised vs Unsupervised Algorithms\", \"Cross Validation & Overfitting\", \"SQL Complex Queries & Window Functions\", \"Model Evaluation Metrics\"]",
                    "[\"Round 1: Probability, Statistics & SQL\", \"Round 2: Machine Learning Modeling & Feature Engineering\", \"Round 3: Business Problem Scenario & Data Insights\"]"
            },
            "data analyst", new String[]{
                    "[\"SQL\", \"Python\", \"Excel\", \"PowerBI\", \"Tableau\", \"Pandas\", \"Statistics\", \"Data Cleansing\"]",
                    "[\"NumPy\", \"Data Storytelling\", \"ETL Pipelines\", \"A/B Testing\"]",
                    "[\"Advanced SQL Joins & Aggregations\", \"Data Normalization\", \"KPI Dashboard Design\", \"Exploratory Data Analysis (EDA)\"]",
                    "[\"Round 1: SQL Live Query Assessment\", \"Round 2: Analytics Case Study & Dashboard Walkthrough\", \"Round 3: Business Acumen & Problem Solving\"]"
            },
            "front", new String[]{
                    "[\"JavaScript\", \"TypeScript\", \"React\", \"HTML5\", \"CSS3/Tailwind\", \"Next.js\", \"State Management (Redux/Zustand)\"]",
                    "[\"GraphQL\", \"Web Performance\", \"Testing (Jest/Cypress)\", \"Webpack/Vite\"]",
                    "[\"React Virtual DOM & Reconciliation\", \"Hooks & Custom Hooks\", \"Async State & Server State\", \"CSS Layouts & Responsive Design\"]",
                    "[\"Round 1: JavaScript Fundamentals & Polyfills\", \"Round 2: React Component Architecture & Live Coding\", \"Round 3: Frontend System Design\"]"
            },
            "machine learning", new String[]{
                    "[\"Python\", \"TensorFlow\", \"PyTorch\", \"NumPy\", \"Pandas\", \"Scikit-learn\", \"Linear Algebra\", \"Deep Learning\"]",
                    "[\"Computer Vision / NLP\", \"Hugging Face\", \"MLOps\", \"FastAPI\", \"Docker\"]",
                    "[\"Neural Network Architectures & Backpropagation\", \"Loss Functions & Optimizers\", \"Transformers & Attention Mechanisms\", \"Model Quantization\"]",
                    "[\"Round 1: Math Foundations & Machine Learning Concepts\", \"Round 2: Deep Learning Coding & Pipeline Architecture\", \"Round 3: ML System Design & Scale\"]"
            },
            "full stack", new String[]{
                    "[\"JavaScript/TypeScript\", \"React\", \"Node.js\", \"Express\", \"Java/Spring Boot\", \"MySQL/PostgreSQL\", \"REST APIs\"]",
                    "[\"Docker\", \"Redis\", \"Next.js\", \"CI/CD Pipelines\", \"Tailwind CSS\"]",
                    "[\"Client-Server Architecture\", \"Authentication & Authorization\", \"Database Schema Optimization\", \"API Rate Limiting & Caching\"]",
                    "[\"Round 1: Coding & Data Structures\", \"Round 2: Full Stack Feature Implementation\", \"Round 3: End-to-End System Design\"]"
            }
    );

    @GetMapping("/user/profile")
    public ProfileResponse getProfile(Authentication authentication) {
        long startTime = System.currentTimeMillis();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] GetProfile duration = " + duration + " ms (User: " + email + ")");

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getCollege(),
                user.getTargetJob(),
                user.getTargetCompany(),
                user.getTargetSkills(),
                user.getRecommendedSkills(),
                user.getInterviewTopics(),
                user.getCompanyPreparation()
        );
    }

    @PutMapping("/user/profile")
    public ProfileResponse updateProfile(Authentication authentication,
                                         @RequestBody UpdateProfileRequest request) {
        long startTime = System.currentTimeMillis();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getCollege() != null) user.setCollege(request.getCollege());

        String oldJob = user.getTargetJob();
        String oldCompany = user.getTargetCompany();

        String newJob = request.getTargetJob();
        String newCompany = request.getTargetCompany();

        if (newJob != null) user.setTargetJob(newJob);
        if (newCompany != null) user.setTargetCompany(newCompany);

        // Fast instant preset assignment if job/company changed
        if (newJob != null && !newJob.isBlank() && 
            (!newJob.equalsIgnoreCase(oldJob) || (newCompany != null && !newCompany.equalsIgnoreCase(oldCompany)))) {
            
            String lowerJob = newJob.toLowerCase(Locale.ROOT);
            String[] preset = null;
            for (Map.Entry<String, String[]> entry : ROLE_PRESETS.entrySet()) {
                if (lowerJob.contains(entry.getKey())) {
                    preset = entry.getValue();
                    break;
                }
            }

            if (preset != null) {
                user.setTargetSkills(preset[0]);
                user.setRecommendedSkills(preset[1]);
                user.setInterviewTopics(preset[2]);
                user.setCompanyPreparation(preset[3]);
            } else {
                // Fallback instant defaults
                user.setTargetSkills("[\"" + newJob + "\", \"Core Architecture\", \"Database Design\", \"Problem Solving\", \"Git\", \"REST APIs\"]");
                user.setRecommendedSkills("[\"System Design\", \"Unit Testing\", \"CI/CD Pipelines\"]");
                user.setInterviewTopics("[\"" + newJob + " Fundamentals\", \"System Architecture\", \"Database Query Tuning\", \"Scenario Problem Solving\"]");
                user.setCompanyPreparation("[\"Round 1: Technical Foundations\", \"Round 2: Practical Coding & Implementation\", \"Round 3: System Design & Cultural Fit\"]");
            }
        }

        User savedUser = userRepository.save(user);

        if (newJob != null && !newJob.isBlank() && !newJob.equalsIgnoreCase(oldJob)) {
            try {
                // Instantly update dynamic roadmap in MySQL without blocking on external AI
                roadmapService.generateFastDynamicRoadmap(savedUser, newJob);
            } catch (Exception e) {
                System.err.println("Roadmap fast auto-update note on role change: " + e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[PERF] TargetJob / Profile update = " + duration + " ms (Job: " + newJob + ", Company: " + newCompany + ")");

        return new ProfileResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getCollege(),
                savedUser.getTargetJob(),
                savedUser.getTargetCompany(),
                savedUser.getTargetSkills(),
                savedUser.getRecommendedSkills(),
                savedUser.getInterviewTopics(),
                savedUser.getCompanyPreparation()
        );
    }
}