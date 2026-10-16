package com.skillbridge.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    private final Client client;

    public GeminiService(
            @Value("${gemini.api.key}") String apiKey
    ) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }

    public String analyzeResume(String resumeText) {

        String prompt = """
                You are the AI analysis engine for SkillBridge career guidance application.

                Analyze the following resume text carefully.

                Return ONLY a single valid JSON object without markdown code blocks, containing:
                - candidateName: Candidate's full name extracted from resume (or "Candidate")
                - education: Highest degree or college extracted from resume (or "N/A")
                - experience: Key experience or internship extracted from resume (or "Fresher")
                - detectedSkills: Array of technical skills explicitly found in the resume
                - strengths: Array of 3-5 technical strengths based on resume content
                - missingSkills: Array of 3-5 key industry skills to learn next
                - careerPaths: Array of 3 suitable job roles for this candidate
                - resumeScore: Integer between 0 and 100 representing resume quality
                - aiConfidence: Integer between 0 and 100 representing confidence in analysis
                - summary: Brief 1-2 sentence profile summary

                Do not invent skills or credentials not supported by the resume text.

                Resume text:
                %s
                """.formatted(resumeText);

        try {
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            prompt,
                            null
                    );

            String responseText = response.text();
            if (responseText != null) {
                // Strip markdown backticks if present
                responseText = responseText.trim();
                if (responseText.startsWith("```json")) {
                    responseText = responseText.substring(7);
                } else if (responseText.startsWith("```")) {
                    responseText = responseText.substring(3);
                }
                if (responseText.endsWith("```")) {
                    responseText = responseText.substring(0, responseText.length() - 3);
                }
                return responseText.trim();
            }
            return "";
        } catch (Exception e) {
            System.err.println("Gemini analysis error: " + e.getMessage());
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    public String generateRoadmap(String roleName, java.util.List<String> detectedSkills, java.util.List<String> missingSkills, String profileSummary) {

        String prompt = """
                You are the AI career path and curriculum engine for SkillBridge.

                Generate a comprehensive, personalized learning roadmap tailored for a candidate aiming for the job role: %s.

                Currently Mastered Skills:
                %s

                Target Skill Gaps To Focus On:
                %s

                Candidate Profile Context:
                %s

                INSTRUCTIONS:
                - Create a structured progressive curriculum consisting of 4 distinct weekly modules (e.g. Module 1: Core Fundamentals, Module 2: Advanced Concepts & UI/Frameworks, Module 3: REST APIs & Data Persistence, Module 4: Testing & Capstone Project).
                - Heavily prioritize the candidate's actual skill gaps and missing skills.
                - Do NOT repeat beginner concepts if the candidate already mastered them.
                - Each module must have 2 to 4 concrete actionable tasks.
                - Return ONLY a single valid JSON object without markdown code blocks matching this schema:

                {
                  "careerPath": "%s",
                  "totalWeeks": 4,
                  "weeks": [
                    {
                      "week": 1,
                      "title": "Module 1 — Core Fundamentals",
                      "skillName": "Main Skill Focus",
                      "description": "Comprehensive explanation of what to learn and expected outcome.",
                      "skills": ["Skill 1", "Skill 2"],
                      "tasks": [
                        "Learn core syntax and fundamentals",
                        "Implement hands-on practice snippet",
                        "Build functional utility component"
                      ],
                      "estimatedHours": 6,
                      "resourceUrl": "https://developer.android.com"
                    }
                  ]
                }
                """.formatted(
                        roleName,
                        (detectedSkills != null && !detectedSkills.isEmpty()) ? String.join(", ", detectedSkills) : "None listed",
                        (missingSkills != null && !missingSkills.isEmpty()) ? String.join(", ", missingSkills) : "Core job requirements",
                        profileSummary != null ? profileSummary : "Candidate",
                        roleName
                );

        try {
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            prompt,
                            null
                    );

            String responseText = response.text();
            if (responseText != null) {
                responseText = responseText.trim();
                if (responseText.startsWith("```json")) {
                    responseText = responseText.substring(7);
                } else if (responseText.startsWith("```")) {
                    responseText = responseText.substring(3);
                }
                if (responseText.endsWith("```")) {
                    responseText = responseText.substring(0, responseText.length() - 3);
                }
                return responseText.trim();
            }
            return "";
        } catch (Exception e) {
            System.err.println("Gemini roadmap generation error: " + e.getMessage());
            throw new RuntimeException("AI roadmap generation failed: " + e.getMessage(), e);
        }
    }

    public String chatWithAssistant(String userMessage, String userContext) {
        String systemPrompt = """
                You are SkillBridge AI, a personal career-development and technical learning assistant.

                SYSTEM DIRECTIVES:
                - You help the user improve technical skills, follow their personalized learning roadmap, prepare for technical interviews, solve coding problems, and close skill gaps.
                - Always use the user's SkillBridge profile and learning context when relevant.
                - Be encouraging, concise, practical, technically accurate, and actionable.
                - Do not invent skills or credentials not supported by the user's context.
                - If user information is unavailable, state so clearly and provide general expert career guidance.
                - For code explanations or programming solutions, use clean markdown code blocks.

                USER PROFILE & CAREER CONTEXT:
                %s

                USER QUESTION / MESSAGE:
                %s
                """.formatted(
                        userContext != null ? userContext : "No prior user context available.",
                        userMessage
                );

        try {
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            systemPrompt,
                            null
                    );

            String responseText = response.text();
            return (responseText != null) ? responseText.trim() : "SkillBridge AI is temporarily unavailable. Please try again.";
        } catch (Exception e) {
            System.err.println("Gemini chat error: " + e.getMessage());
            throw new RuntimeException("SkillBridge AI is temporarily unavailable. Please try again.", e);
        }
    }

    public String generateMcqQuestions(java.util.List<String> skills, int totalQuestions) {
        String skillsStr = String.join(", ", skills);
        String prompt = """
                You are the AI Skill Verification assessment engine for SkillBridge.
                
                Generate a list of exactly %d multiple-choice questions to verify the student's actual understanding of the following skills:
                %s
                
                INSTRUCTIONS:
                - Do NOT ask trivial memorization questions (e.g. "What is the syntax?"). Focus on practical usage, conceptual understanding, and scenario-based problems.
                - Distribute questions across the skills list. For example, if there are 5 skills, generate approximately %d questions total distributed among them.
                - Use these three difficulty levels: EASY, MEDIUM, HARD.
                - Try to distribute the questions roughly as:
                  * 30%% EASY
                  * 50%% MEDIUM
                  * 20%% HARD
                - Each question must have exactly 4 options.
                - Provide a clear, short explanation for the correct answer.
                - Return ONLY a single valid JSON object without markdown code blocks matching this schema:
                
                {
                  "questions": [
                    {
                      "skill": "SkillName",
                      "difficulty": "EASY|MEDIUM|HARD",
                      "question": "The question text here?",
                      "options": [
                        "Option A",
                        "Option B",
                        "Option C",
                        "Option D"
                      ],
                      "correctAnswer": 1,
                      "explanation": "Brief explanation why Option B is correct."
                    }
                  ]
                }
                
                Note: correctAnswer must be a 0-indexed integer (0, 1, 2, or 3) corresponding to the index of the correct option in the options array.
                """.formatted(totalQuestions, skillsStr, totalQuestions);

        try {
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-2.5-flash",
                            prompt,
                            null
                    );

            String responseText = response.text();
            if (responseText != null) {
                responseText = responseText.trim();
                if (responseText.startsWith("```json")) {
                    responseText = responseText.substring(7);
                } else if (responseText.startsWith("```")) {
                    responseText = responseText.substring(3);
                }
                if (responseText.endsWith("```")) {
                    responseText = responseText.substring(0, responseText.length() - 3);
                }
                return responseText.trim();
            }
            throw new RuntimeException("Empty response from Gemini");
        } catch (Exception e) {
            System.err.println("Gemini MCQ generation error: " + e.getMessage());
            throw new RuntimeException("AI MCQ generation failed: " + e.getMessage(), e);
        }
    }

    public String generateMockInterviewQuestion(String role, String difficulty, String context) {
        String prompt = """
                You are an expert technical interviewer conducting a mock interview for a '%s' position.
                
                Generate a single %s difficulty technical question appropriate for this role.
                
                CONTEXT OF PREVIOUS QUESTIONS / HISTORY:
                %s
                
                Return ONLY the question text itself. Do not write any conversational preamble, introduction, or formatting.
                """.formatted(role, difficulty, context != null ? context : "This is the first question.");

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            return response.text() != null ? response.text().trim() : "Explain the concept of MVC architecture.";
        } catch (Exception e) {
            return "Explain the concept of MVC architecture.";
        }
    }

    public String evaluateInterviewAnswer(String question, String answer) {
        String prompt = """
                You are a technical interviewer evaluating a candidate's answer.
                
                QUESTION:
                %s
                
                CANDIDATE ANSWER:
                %s
                
                Evaluate this answer and return a JSON object with:
                1. "technicalAccuracy": Integer from 0 to 100
                2. "conceptUnderstanding": Integer from 0 to 100
                3. "communication": Integer from 0 to 100
                4. "score": Overall Integer score from 0 to 100
                5. "feedback": A 2-3 sentence constructive feedback explaining what was correct and what can be improved.
                
                Return ONLY the valid raw JSON object. Do not include markdown formatting or backticks.
                """.formatted(question, answer);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{\"technicalAccuracy\":70,\"conceptUnderstanding\":70,\"communication\":70,\"score\":70,\"feedback\":\"Good attempt. Explain details more thoroughly.\"}";
        } catch (Exception e) {
            return "{\"technicalAccuracy\":70,\"conceptUnderstanding\":70,\"communication\":70,\"score\":70,\"feedback\":\"Good attempt. Explain details more thoroughly.\"}";
        }
    }

    public String generateDailyChallenge(String skillName) {
        String prompt = """
                Generate a single Daily Challenge/Assessment task for the technical skill: '%s'.
                
                You can choose one of the following challenge types:
                - MCQ (Multiple Choice Question, with 4 choices)
                - SQL (Query coding task)
                - CODING (Short programming task)
                
                Format the response as a JSON object with:
                1. "challengeType": "MCQ", "SQL", or "CODING"
                2. "questionText": The problem statement or question.
                3. "options": For MCQ, return a comma-separated list of 4 options. For SQL or CODING, return an empty string.
                4. "correctAnswer": The correct option string for MCQ, or a sample correct code/SQL query for others.
                5. "explanation": A detailed, clear explanation of the solution.
                
                Return ONLY the valid raw JSON object. Do not include markdown code block formatting or backticks.
                """.formatted(skillName);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{\"challengeType\":\"MCQ\",\"questionText\":\"What does SQL stand for?\",\"options\":\"Structured Query Language,Simple Query Language,Standard Query List,None\",\"correctAnswer\":\"Structured Query Language\",\"explanation\":\"SQL stands for Structured Query Language.\"}";
        } catch (Exception e) {
            return "{\"challengeType\":\"MCQ\",\"questionText\":\"What does SQL stand for?\",\"options\":\"Structured Query Language,Simple Query Language,Standard Query List,None\",\"correctAnswer\":\"Structured Query Language\",\"explanation\":\"SQL stands for Structured Query Language.\"}";
        }
    }

    public String evaluateChallengeAnswer(String questionText, String type, String correctAnswer, String studentAnswer) {
        String prompt = """
                Evaluate a student's answer to a Daily Challenge task.
                
                CHALLENGE QUESTION:
                %s
                
                CHALLENGE TYPE:
                %s
                
                EXPECTED CORRECT ANSWER / REFERENCE:
                %s
                
                STUDENT ANSWER:
                %s
                
                Evaluate and return a JSON object with:
                1. "correct": boolean (true if correct/mostly correct, false otherwise)
                2. "explanation": 2-3 sentence description of the correct logic or code.
                3. "improvementSuggestion": 1-2 sentence recommendation for mastering this concept.
                
                Return ONLY the valid raw JSON object. Do not include markdown formatting or backticks.
                """.formatted(questionText, type, correctAnswer, studentAnswer);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{\"correct\":false,\"explanation\":\"Check matching criteria.\",\"improvementSuggestion\":\"Practice more similar exercises.\"}";
        } catch (Exception e) {
            return "{\"correct\":false,\"explanation\":\"Check matching criteria.\",\"improvementSuggestion\":\"Practice more similar exercises.\"}";
        }
    }

    public String generateCareerReadinessReport(String role, String summary) {
        String prompt = """
                You are an AI Career Coach.
                
                Generate a structured career readiness report for a candidate aiming to be a '%s'.
                
                CANDIDATE METRICS AND BACKGROUND:
                %s
                
                Provide a JSON response containing:
                1. "overallReadiness": Integer percentage (0 to 100) representing how ready they are for job applications based on the data.
                2. "strengths": A JSON array of 2-3 core strengths of the candidate.
                3. "priorityGaps": A JSON array of 2-3 technical areas they must address immediately.
                4. "recommendation": A short, actionable career roadmap/strategy (3-4 sentences).
                
                Return ONLY the valid raw JSON object. Do not include markdown formatting or backticks.
                """.formatted(role, summary);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{\"overallReadiness\":70,\"strengths\":[\"Basic programming\"],\"priorityGaps\":[\"Advanced systems\"],\"recommendation\":\"Practice database design and build complete micro-projects.\"}";
        } catch (Exception e) {
            return "{\"overallReadiness\":70,\"strengths\":[\"Basic programming\"],\"priorityGaps\":[\"Advanced systems\"],\"recommendation\":\"Practice database design and build complete micro-projects.\"}";
        }
    }

    public String generateMasteryQuestions(String topic, String taskDescription, String skillName, String studentInfo, int questionCount) {
        String prompt = """
                You are the AI Mastery Check Engine for SkillBridge.
                
                Generate a list of %d questions to evaluate the student's mastery of:
                Topic: %s
                Task Description: %s
                Associated Skill: %s
                
                Student Context:
                %s
                
                The evaluation should test multiple levels:
                - CONCEPTUAL (basic definitions or terms)
                - UNDERSTANDING (interpreting snippets, tracing, or conceptual outcomes)
                - APPLICATION (writing queries, syntax choice, or direct coding implementations)
                - DEBUGGING (finding errors, security vulnerabilities, or logical mistakes)
                - SCENARIO (architectural decisions, schemas, complex problem solving)
                
                Generate questions relevant to this topic. Do not generate generic questions.
                
                Format the response as a JSON array of objects. Each object in the array must contain:
                1. "questionText": The problem statement or question.
                2. "difficulty": "EASY", "MEDIUM", or "HARD".
                3. "concept": The specific sub-concept tested (e.g. "LEFT JOIN").
                4. "questionType": "CONCEPTUAL", "UNDERSTANDING", "APPLICATION", "DEBUGGING", or "SCENARIO".
                5. "options": A JSON array of exactly 4 choices (Strings).
                6. "correctAnswer": Integer (0 to 3) representing the index of the correct option.
                7. "explanation": A detailed, clear explanation of why that option is correct.
                
                Return ONLY the valid raw JSON array. Do not include markdown code block formatting or backticks.
                """.formatted(questionCount, topic, taskDescription, skillName, studentInfo != null ? studentInfo : "No prior attempts.");

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "[]";
        } catch (Exception e) {
            System.err.println("Gemini generateMasteryQuestions error: " + e.getMessage());
            return "[]";
        }
    }

    public String evaluateMasteryCheck(String topic, int score, String qaJson) {
        String prompt = """
                You are the AI Career Mentor for SkillBridge.
                
                Analyze a student's Skill Mastery Check for the topic: '%s'.
                Overall Score: %d%%
                
                Questions and Answers details:
                %s
                
                Evaluate the results and return a JSON object with:
                1. "masteryLevel": "MASTERED" (if score is 80 or above), "REINFORCE" (if score is 60 to 79), or "NEEDS_WORK" (if score is below 60).
                2. "strengths": A JSON array of 2-3 specific concepts they mastered well (e.g. "Understanding LEFT JOIN syntax").
                3. "weakConcepts": A JSON array of 2-3 priority weak concepts they struggled with (e.g. "JOIN with GROUP BY").
                4. "explanation": A supportive, clear 3-4 sentence summary explanation of their results, strengths, and priority improvements.
                5. "recommendedPractice": A JSON array of 2-3 targeted, practical reinforcement exercises, code prompts, or debug challenges (e.g. "Practice: Multi-table JOIN with 3 tables").
                6. "readyForNextTopic": boolean (true if masteryLevel is MASTERED or REINFORCE, false otherwise).
                
                Return ONLY the valid raw JSON object. Do not include markdown code block formatting or backticks.
                """.formatted(topic, score, qaJson);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{}";
        } catch (Exception e) {
            System.err.println("Gemini evaluateMasteryCheck error: " + e.getMessage());
            return "{}";
        }
    }

    public String generateTargetProfile(String jobTitle, String company) {
        String companyContext = (company == null || company.isBlank() || "Any company".equalsIgnoreCase(company)) 
                ? "General Industry standard preparation" 
                : "Target Company: " + company;

        String prompt = """
                You are the AI Career Architect for SkillBridge.
                
                Generate a target career profile for the target role: '%s'.
                Context: %s
                
                Analyze the requirements for this role. For the company-specific prep, generate recommended preparation areas and commonly expected technical focus topics. Do NOT claim these are official company requirements. Wording must be "Recommended preparation areas" or "Commonly expected areas".
                
                Format the response as a JSON object containing:
                1. "targetSkills": A JSON array of 6-10 primary technical skills required for this role (e.g. ["Java", "Spring Boot", "SQL"]).
                2. "recommendedSkills": A JSON array of 3-5 auxiliary recommended skills (e.g. ["Git", "System Design"]).
                3. "interviewTopics": A JSON array of 4-6 technical interview topics (e.g. ["Spring Bean Lifecycle", "Concurrency"]).
                4. "companyPreparation": A JSON array of 3-5 company-oriented interview rounds, expectations, or preparation focus areas (e.g. ["Round 1: Logical Reasoning and Core coding", "Common focus: Database schema design and query tuning"]).
                
                Return ONLY the valid raw JSON object. Do not include markdown code block formatting or backticks.
                """.formatted(jobTitle, companyContext);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{}";
        } catch (Exception e) {
            System.err.println("Gemini generateTargetProfile error: " + e.getMessage());
            return "{}";
        }
    }

    public String generateCareerMission(String targetJob, String userSkills, String skillGaps, String roadmapContext) {
        String prompt = """
                You are the AI Career Project Mission Designer for SkillBridge.

                Create a realistic, comprehensive software project assignment that a candidate aiming for the target career '%s' would build and publish to GitHub as a portfolio-grade project to prove their practical skills.

                CANDIDATE CONTEXT:
                - Target Career: %s
                - Current Skills: %s
                - Skill Gaps / Focus Areas: %s
                - Learning Roadmap Context: %s

                PROJECT MISSION SPECIFICATIONS:
                - Project MUST directly target '%s' and help close the candidate's skill gaps. For example:
                  * Android Developer: Build a complete Native Android app with Kotlin, Jetpack Compose, MVVM, Room, Retrofit, Coroutines/Flow, and Unit Tests.
                  * Backend Developer: Build a production-grade REST API with Java/Spring Boot or Node.js/Python, database persistence, authentication, validation, error handling, Dockerfile, and integration tests.
                  * Frontend Developer: Build a responsive web app with React/Next.js or Vue, state management, API consumption, component modularity, and testing.
                  * Data Scientist / Analyst: Build an exploratory data analysis & predictive pipeline with Jupyter/Python, Pandas, Scikit-learn, SQL, and interactive dashboards.
                - Provide 6 to 10 realistic, concrete project requirements.
                - Provide clear evaluation criteria (Requirements, Architecture, Code Quality, Technology Usage, Testing, Documentation, Career Relevance).

                Format the response as a valid raw JSON object matching this schema:
                {
                  "title": "Build a Task Management Android Application with Offline Sync",
                  "targetJob": "%s",
                  "description": "Comprehensive explanation of what the application does and why it is built.",
                  "scenario": "You are assigned to develop an industry-grade application for %s. The application must feature robust architecture, clean coding standards, proper data persistence, network communication, error handling, and comprehensive documentation in a public GitHub repository.",
                  "whyThisMission": "Based on your resume, target role and current skill gaps, SkillBridge recommends this project to help prove your practical skills.",
                  "difficulty": "Intermediate",
                  "estimatedTime": "3–5 days",
                  "skillsToDemonstrate": ["Kotlin", "Jetpack Compose", "Room", "Retrofit", "MVVM", "Git/GitHub"],
                  "requirements": [
                    "Implement user authentication and session management",
                    "Complete CRUD operations with responsive UI",
                    "Local data persistence with offline cache synchronization",
                    "REST API integration with error handling and retry mechanisms",
                    "Unidirectional data flow architecture (MVVM / MVI)",
                    "Defensive error handling and nullable safety",
                    "Automated unit tests for business logic",
                    "Professional README.md with architectural diagrams and setup guide"
                  ],
                  "evaluationCriteria": [
                    "Requirements Completion & Feature Implementation",
                    "Architecture & Clean Code Principles",
                    "Technology Stack & Framework Best Practices",
                    "Error Handling & Edge Case Resilience",
                    "Testing Coverage & Verification Evidence",
                    "Documentation & Repository Structure",
                    "Career Relevance"
                  ]
                }

                Return ONLY the valid raw JSON object without markdown code blocks or backticks.
                """.formatted(
                targetJob != null ? targetJob : "Software Developer",
                targetJob != null ? targetJob : "Software Developer",
                userSkills != null ? userSkills : "Core programming",
                skillGaps != null ? skillGaps : "Modern frameworks",
                roadmapContext != null ? roadmapContext : "General path",
                targetJob != null ? targetJob : "Software Developer",
                targetJob != null ? targetJob : "Software Developer",
                targetJob != null ? targetJob : "Software Developer"
        );

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{}";
        } catch (Exception e) {
            System.err.println("Gemini generateCareerMission error: " + e.getMessage());
            return "{}";
        }
    }

    public String evaluateGitHubProjectMission(String missionSpecJson, String githubProjectSummaryJson) {
        String prompt = """
                You are a Principal Software Architect and Senior Technical Evaluator at SkillBridge.
                You are evaluating a candidate's GitHub repository submitted for an AI Career Project Mission.

                ASSIGNED PROJECT MISSION:
                %s

                ACCESSIBLE GITHUB REPOSITORY ANALYSIS:
                %s

                EVALUATION INSTRUCTIONS:
                - Carefully evaluate the candidate's repository content against the assigned project requirements.
                - Analyze:
                  1. Requirements Completion (Did they implement the assigned features?)
                  2. Architecture (Project structure, modularity, Clean Architecture/MVVM/Layering)
                  3. Code Quality (Readability, language idiomatic usage, naming, error handling)
                  4. Technology Usage (Appropriate libraries, dependency configs, build files)
                  5. Testing (Presence of automated unit/integration tests)
                  6. Documentation & Git (Quality of README.md, repo cleanliness, commit hygiene)
                  7. Career Relevance (How well it demonstrates readiness for the target role)
                - Crucial: Distinguish between "IMPLEMENTED IN CODE", "VERIFIED BY TESTS", "NOT FOUND", and "CANNOT VERIFY". Do not claim runtime verification if only source code exists.
                - Compute scores from 0 to 100 for overall and all sub-categories.

                Format the response as a valid raw JSON object matching this schema:
                {
                  "overallScore": 82,
                  "requirementsScore": 88,
                  "architectureScore": 84,
                  "codeQualityScore": 79,
                  "technologyScore": 90,
                  "testingScore": 65,
                  "documentationScore": 90,
                  "careerRelevanceScore": 92,
                  "completedRequirements": [
                    "User authentication and session management (Implemented in code)",
                    "Complete CRUD operations with responsive UI (Implemented in code)",
                    "Local data persistence with Room database (Implemented in code)",
                    "REST API integration with error handling (Implemented in code)",
                    "MVVM Architecture and Clean separation (Implemented in code)"
                  ],
                  "missingRequirements": [
                    "Automated unit tests for repository layer (Not found)",
                    "Search and filter functionality (Cannot verify)"
                  ],
                  "strengths": [
                    "Clean architectural separation between UI, domain models, and data layer",
                    "Comprehensive README.md with clear setup steps and project overview",
                    "Robust REST API integration with error mapping"
                  ],
                  "weaknesses": [
                    "Limited automated test coverage for viewmodels and edge cases",
                    "Error handling could be made more resilient for unexpected network timeouts"
                  ],
                  "recommendations": [
                    "Add automated unit tests for your data repositories and ViewModel state hoisting.",
                    "Enhance defensive network timeout error handling.",
                    "Complete the missing search and filter features to meet full portfolio requirements."
                  ],
                  "feedback": "Outstanding project submission! The repository demonstrates strong structural proficiency and matches the required architectural patterns. Strengthening test coverage will make this an exceptional portfolio piece."
                }

                Return ONLY the valid raw JSON object without markdown code blocks or backticks.
                """.formatted(missionSpecJson, githubProjectSummaryJson);

        try {
            GenerateContentResponse response = client.models.generateContent("gemini-2.5-flash", prompt, null);
            String text = response.text();
            if (text != null) {
                text = text.trim();
                if (text.startsWith("```json")) text = text.substring(7);
                else if (text.startsWith("```")) text = text.substring(3);
                if (text.endsWith("```")) text = text.substring(0, text.length() - 3);
                return text.trim();
            }
            return "{}";
        } catch (Exception e) {
            System.err.println("Gemini evaluateGitHubProjectMission error: " + e.getMessage());
            return "{}";
        }
    }

    public String evaluateCareerMission(String missionDetailsJson, String studentResponse) {
        return evaluateGitHubProjectMission(missionDetailsJson, studentResponse);
    }
}