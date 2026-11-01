# ⚡ SkillBridge AI — Spring Boot Backend Service

[![Backend](https://img.shields.io/badge/Backend-Java_21_Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](.)
[![MySQL](https://img.shields.io/badge/Database-MySQL_8.x-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](.)
[![Gemini AI](https://img.shields.io/badge/AI-Google_Gemini_2.5_Flash-4285F4?style=for-the-badge&logo=google&logoColor=white)](.)
[![Docker](https://img.shields.io/badge/Docker-Multi_Stage_Build-2496ED?style=for-the-badge&logo=docker&logoColor=white)](./Dockerfile)

The enterprise backend service powering the **SkillBridge AI** platform. Built with Java 21 and Spring Boot 3, providing high-performance REST APIs, PDF resume extraction via Apache PDFBox, JWT authentication, and direct integration with Google Gemini AI (`gemini-2.5-flash`).

---

## 🌟 Core Backend Capabilities

1. **📄 Resume ATS Parsing Engine**:
   - Parses uploaded PDF resumes via Apache PDFBox.
   - Leverages Google Gemini AI to extract verified technical skills, calculate ATS match score (0-100), identify missing keywords, and suggest candidate strengths.

2. **🗺️ Dynamic Career Roadmap Generator**:
   - Generates personalized, milestone-sequenced weekly learning paths for 8+ tech roles (Android, Java Backend, Full Stack, Data Science, AI/ML, DevOps, Cloud, etc.).
   - Persists tasks to MySQL and tracks completion statuses.

3. **🎯 Skill Gap & Mastery Check Engine**:
   - Compares user skills against industry standards.
   - Dynamically generates technical MCQs, open problem challenges, and mastery verification assessments.

4. **🚀 AI Career Project Mission Engine**:
   - Generates realistic workplace scenarios and engineering missions tailored to candidate target roles.
   - Evaluates code submissions and architectural proposals with simulated hiring manager feedback.

5. **🤖 AI Copilot & Mock Interview Agent**:
   - Context-aware technical conversational agent with user profile and roadmap memory.
   - Conducts multi-turn mock technical interviews with instant feedback.

---

## 🏗️ Architecture & Component Overview

```
src/main/java/com/skillbridge/
├── SkillbridgeBackendApplication.java    [Spring Boot main entry point]
├── config/
│   └── JobRoleDataInitializer.java      [Seeds 8 standard tech roles and skill taxonomies]
├── controller/                           [16 REST Controllers]
│   ├── AuthController.java              [/api/auth/login, /api/auth/register]
│   ├── ProfileController.java           [/api/profile - user career goals and profile]
│   ├── ResumeController.java            [/api/resume/upload - multipart PDF ATS extraction]
│   ├── RoadmapController.java           [/api/roadmap - dynamic roadmap generation & tasks]
│   ├── SkillGapController.java          [/api/skillgap - missing skills analysis]
│   ├── DailyChallengeController.java    [/api/daily-challenge - daily problem solving]
│   ├── CareerMissionController.java     [/api/career-mission - workplace project simulations]
│   ├── CopilotController.java           [/api/copilot - AI interview copilot]
│   ├── ChatController.java              [/api/chat - AI career assistant]
│   ├── MasteryCheckController.java      [/api/mastery - module retests & mastery checks]
│   ├── MockInterviewController.java     [/api/interview - AI technical mock interviews]
│   ├── SkillTestController.java         [/api/skills - technical assessments]
│   └── HealthController.java            [/api/health - server status]
├── entity/                              [13 JPA MySQL Entities]
│   ├── User.java, UserSkill.java, Resume.java, JobRole.java
│   ├── Roadmap.java, RoadmapTask.java
│   ├── SkillAssessment.java, AssessmentQuestion.java
│   ├── DailyChallenge.java, MockInterview.java, InterviewQuestion.java
│   ├── ReminderHistory.java, CareerMission.java
├── repository/                          [12 Spring Data JPA Repositories]
├── security/                            [Stateless JWT Auth, Filters & SecurityConfig]
└── service/                             [Business Logic & AI Integration]
    ├── GeminiService.java               [Google Gemini 2.5 Flash API connector]
    ├── ResumeService.java               [PDFBox document extraction & ATS scoring]
    ├── RoadmapService.java              [Roadmap generation & task management]
    ├── GitHubService.java               [GitHub profile and repository analysis]
    ├── SkillGapAnalyzerService.java     [Skill gap computation against target role]
    └── AuthService.java / UserService.java
```

---

## ⚙️ Configuration & Environment Variables

Key parameters in `src/main/resources/application.properties`:

| Variable | Description | Default Value |
|---|---|---|
| `PORT` | HTTP Server Port | `8080` |
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://${MYSQLHOST:localhost}:${MYSQLPORT:3306}/${MYSQLDATABASE:skillbridge_db}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | Database Username | `${SPRING_DATASOURCE_USERNAME:${MYSQLUSER:root}}` |
| `SPRING_DATASOURCE_PASSWORD` | Database Password | `${SPRING_DATASOURCE_PASSWORD:${MYSQLPASSWORD:<your_db_password>}}` |
| `GEMINI_API_KEY` | Google Gemini API Key | Server-side only (Set via environment variable `GEMINI_API_KEY`) |
| `JWT_SECRET` | Secret key for signing JWTs | `${JWT_SECRET:<your_jwt_secret_key>}` |
| `FILE_UPLOAD_DIR` | Directory for uploaded resumes | `uploads` |

---

## 🛠️ Local Development & Running

### Prerequisites
- **JDK 21**
- **MySQL 8.x** running locally with database `skillbridge_db` created

### Run with Maven Wrapper
```bash
# Navigate to backend directory
cd Backend/skillbridge-backend

# Compile and run
./mvnw.cmd spring-boot:run
```
The REST API will be available at `http://localhost:8080/`.

### Run via Docker
```bash
docker build -t skillbridge-backend .
docker run -p 8080:8080 -e GEMINI_API_KEY="your_api_key" skillbridge-backend
```
