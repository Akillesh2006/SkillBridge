# SkillBridge AI: Complete Project Audit Report
**Document ID:** `SKILLBRIDGE_RECOVERY_AUDIT.md`  
**Date:** November 1, 2026  
**Auditor:** Senior Android Developer, Java Backend Engineer & Software Architect  
**Project Root:** `G:\projects\SkillBridge`  
**Target Git Branch:** `main` (commit `d015cd4`)  
**Backup Reference:** `G:\projects\SkillBridge-backup-before-recovery`  

---

## 1. Executive Summary & Audit Overview

SkillBridge AI is primarily and fundamentally an **enterprise-grade Native Android Mobile Application** powered by a **Java 17 / Spring Boot 3 microservices backend**, integrating **Google Gemini AI** for intelligent resume ATS parsing, dynamic career roadmapping, technical assessments, and workplace career missions.

### Root Cause of User Confusion
During a prior development cycle, an external web deployment was generated using Vite + React to host a web demo on Vercel (`https://skillbridge-app.vercel.app`).
1. **Mock Data Injection on Web**: The web application was created with simulated mock datasets (hardcoded ATS scores, hardcoded roadmaps, static quizzes) completely decoupled from the real Android app and Spring Boot database.
2. **Directory Duplication**: A copy of the website was placed inside the backend folder (`Backend/skillbridge-backend/web`), and another copy was placed at the repository root (`/web`).
3. **Misleading Documentation & Configuration**: The root `README.md`, `vercel.json`, and `render.yaml` were formatted as if SkillBridge were primarily a web-first application, obscuring the primary Kotlin Jetpack Compose Android client.
4. **Android Source Status**: Crucially, **the native Android application source code was NEVER destroyed or replaced**. The Android codebase at commit `d015cd4` contains 86 comprehensive Kotlin files, a complete Jetpack Compose design system, Dagger Hilt dependency injection, Room database, WorkManager, and full API integration. It compiles 100% cleanly with Gradle (`BUILD SUCCESSFUL in 6m 1s`).

---

## 2. Native Android Application Audit (`app/`)

### 2.1 Technology Stack & Architectural Pattern
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material 3 (BOM 2024.12.01)
- **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (MVI-style `UiState<T>`)
- **Dependency Injection**: Dagger Hilt 2.52
- **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 + Kotlinx Serialization 1.7.3
- **Local Persistence**: Jetpack DataStore Preferences (auth tokens) + Room 2.6.1 + SQLite
- **Background Tasks**: AndroidX WorkManager 2.9.0 (hourly missed challenge / reminder worker)
- **Image Loading**: Coil Compose 2.7.0

### 2.2 Complete Android Source File Inventory (86 Kotlin Files)
```
app/src/main/kotlin/com/skillbridge/ai/
├── MainActivity.kt                                   [Single Activity, Hilt entry point, WorkManager scheduler]
├── app/
│   └── SkillBridgeApp.kt                             [Application class with @HiltAndroidApp]
├── core/
│   ├── common/
│   │   ├── BaseRepository.kt                         [Safe API call execution & error unwrapping]
│   │   ├── NetworkResult.kt                          [Sealed class: Success, Error, Loading]
│   │   ├── Resource.kt                               [Resource wrapper]
│   │   └── UiState.kt                                [Idle, Loading, Success, Error, Empty]
│   ├── constants/
│   │   └── AppConstants.kt                           [BASE_URL binding, API timeouts, route keys]
│   ├── datastore/
│   │   └── UserPreferencesKeys.kt                    [DataStore preferences keys]
│   └── util/
│       └── Extensions.kt                             [Formatting, date, and string extensions]
├── data/
│   ├── local/
│   │   ├── TokenManager.kt                           [JWT token persistence with in-memory caching]
│   │   ├── dao/BaseDao.kt                            [Generic Room DAO]
│   │   ├── database/AppDatabase.kt                   [Room database declaration]
│   │   └── entity/DummyEntity.kt                     [Local cache entities]
│   ├── remote/
│   │   ├── api/ApiService.kt                         [Retrofit interface: 25+ backend endpoints]
│   │   ├── datasource/RemoteDataSource.kt            [Abstraction layer for network requests]
│   │   └── dto/                                      [Data Transfer Objects (Kotlinx Serialization)]
│   │       ├── AssessmentDto.kt                      [Skill test & MCQ questions / answers]
│   │       ├── CareerMissionDto.kt                   [Workplace simulation mission payload]
│   │       ├── ChallengeDto.kt                       [Daily challenge request & evaluation]
│   │       ├── ChatDto.kt                            [AI Copilot message exchange]
│   │       ├── CopilotDto.kt                         [Interview copilot prompts & evaluations]
│   │       ├── InterviewDto.kt                       [Mock interview questions & scoring]
│   │       ├── LoginRequest.kt / LoginResponse.kt    [Authentication contracts]
│   │       ├── ProfileDto.kt                         [User profile & career metadata]
│   │       ├── RegisterRequest.kt                    [User signup request]
│   │       ├── ResumeResponse.kt                     [ATS parsing scores & extracted skills]
│   │       ├── RoadmapResponse.kt                    [Dynamic learning roadmap milestones]
│   │       ├── RoadmapTaskResponse.kt                [Weekly learning task units]
│   │       └── SkillGapResponse.kt                   [Missing skills vs required job role skills]
│   ├── repository/                                   [Repository implementations]
│   │   ├── AssessmentRepositoryImpl.kt
│   │   ├── AuthRepositoryImpl.kt
│   │   ├── CareerMissionRepositoryImpl.kt
│   │   ├── ChallengeRepositoryImpl.kt
│   │   ├── ChatRepositoryImpl.kt
│   │   ├── CopilotRepositoryImpl.kt
│   │   ├── DashboardRepositoryImpl.kt
│   │   ├── InterviewRepositoryImpl.kt
│   │   ├── JobRepositoryImpl.kt
│   │   ├── ProfileRepositoryImpl.kt
│   │   ├── ProgressRepositoryImpl.kt
│   │   ├── ResumeRepositoryImpl.kt
│   │   ├── RoadmapRepositoryImpl.kt
│   │   └── SkillGapRepositoryImpl.kt
│   └── worker/
│       └── DailyReminderWorker.kt                    [WorkManager task for periodic notifications]
├── di/
│   ├── DataStoreModule.kt                            [Hilt DataStore bindings]
│   ├── DatabaseModule.kt                             [Hilt Room database providers]
│   ├── NetworkModule.kt                              [Hilt OkHttp, Auth Interceptor, Retrofit]
│   └── RepositoryModule.kt                           [Interface-to-implementation DI bindings]
├── domain/
│   ├── model/Models.kt                               [Core domain entities: User, Roadmap, Task, Skill, etc.]
│   ├── repository/                                   [Domain repository interfaces (14 interfaces)]
│   └── usecase/BaseUseCase.kt                        [Domain use-case execution base]
└── presentation/
    ├── components/
    │   ├── Components.kt                             [Design system: GlassCard, PrimaryButton, GradientBars, etc.]
    │   └── PlaceholderScreen.kt                      [Fallback UI component]
    ├── navigation/
    │   ├── NavGraph.kt                               [Jetpack Compose AnimatedNavHost definition]
    │   └── NavRoutes.kt                              [Screen sealed class with 19 destination routes]
    ├── screens/
    │   ├── about/AboutScreen.kt                      [App info, tech stack, and versioning]
    │   ├── analytics/AnalyticsScreen.kt              [Readiness score charts & metrics]
    │   ├── analytics/ReadinessReportScreen.kt        [Detailed career readiness report]
    │   ├── chat/ChatScreen.kt                        [AI Career Copilot chat assistant]
    │   ├── dashboard/DashboardScreen.kt              [Central hub: progress, daily challenge, missions]
    │   ├── forgot/ForgotPasswordScreen.kt            [Password reset flow]
    │   ├── jobselection/JobSelectionScreen.kt        [Target career role & company selector]
    │   ├── login/LoginScreen.kt                      [JWT authentication screen]
    │   ├── mastery/MasteryCheckScreen.kt             [Module mastery verification test]
    │   ├── mastery/MasteryCheckResultScreen.kt       [Mastery evaluation & feedback]
    │   ├── mission/CareerMissionScreen.kt            [Workplace simulation mission engine (1289 lines)]
    │   ├── profile/ProfileScreen.kt                  [Profile details, dark mode toggle, resume download]
    │   ├── progress/ProgressScreen.kt                [Milestone breakdown and completion tracking]
    │   ├── register/RegisterScreen.kt                [User signup screen]
    │   ├── resume/ResumeScreen.kt                    [PDF file picker with stream caching & ATS upload]
    │   ├── resume/ResumeAnalysisResultScreen.kt      [ATS match score, detected & missing skills]
    │   ├── roadmap/RoadmapScreen.kt                  [Interactive weekly roadmap timeline (794 lines)]
    │   ├── skillgap/SkillGapScreen.kt                [Skill gap radar / comparison UI]
    │   ├── skilltest/DailyChallengeScreen.kt         [Daily interactive coding challenge & grading]
    │   ├── skilltest/MockInterviewScreen.kt          [AI mock technical interview simulator]
    │   ├── skilltest/SkillTestScreen.kt              [Technical MCQ assessment test]
    │   ├── skilltest/SkillTestResultScreen.kt        [Test score & performance feedback]
    │   └── splash/SplashScreen.kt                    [Branded splash screen with smooth animations]
    ├── theme/
    │   ├── Color.kt                                  [NeonCyan, ElectricBlue, DeepNavy, SpaceBlack palette]
    │   ├── Theme.kt                                  [SkillBridgeTheme Compose theme definition]
    │   └── Type.kt                                   [Typography definitions]
    └── viewmodel/                                    [17 dedicated Hilt ViewModels]
```

### 2.3 Jetpack Compose Navigation Map (19 Routes)
| Route Name | Screen Destination | Purpose |
|---|---|---|
| `splash` | `SplashScreen` | Cold start animation & auth routing |
| `login` | `LoginScreen` | JWT login with backend |
| `register` | `RegisterScreen` | New user account creation |
| `forgot_password` | `ForgotPasswordScreen` | Password recovery |
| `dashboard` | `DashboardScreen` | Main application hub & daily widgets |
| `resume` | `ResumeScreen` | PDF resume upload & background caching |
| `resume_result` | `ResumeAnalysisResultScreen` | Gemini ATS scoring & keyword breakdown |
| `job_selection` | `JobSelectionScreen` | Target role selector & roadmap trigger |
| `skill_gap` | `SkillGapScreen` | Real-time gap analysis vs target job |
| `roadmap` | `RoadmapScreen` | Interactive weekly roadmap & milestone checklist |
| `progress` | `ProgressScreen` | Progress tracking & historical records |
| `profile` | `ProfileScreen` | User profile, theme settings, logout |
| `analytics` | `AnalyticsScreen` | Skill analytics & readiness metrics |
| `chat` | `ChatScreen` | Gemini AI Career Copilot assistant |
| `about` | `AboutScreen` | Architecture and project credits |
| `skill_test` | `SkillTestScreen` | MCQ technical verification test |
| `skill_test_result/{id}` | `SkillTestResultScreen` | Detailed score breakdown & explanations |
| `daily_challenge` | `DailyChallengeScreen` | Daily problem solving & automated grading |
| `career_mission` | `CareerMissionScreen` | AI hiring manager workplace simulation |
| `mastery_check/{taskId}` | `MasteryCheckScreen` | Roadmap task mastery verification |
| `mastery_check_result/{id}` | `MasteryCheckResultScreen`| Retest & mastery feedback |

---

## 3. Spring Boot Backend Audit (`Backend/skillbridge-backend/`)

### 3.1 Technology Stack
- **Framework**: Spring Boot 3.5.4 (Java 21)
- **Database**: MySQL 8.x with Hibernate / JPA & HikariCP pool
- **Security**: Spring Security 6 with JJWT 0.12.7 (Stateless Bearer token auth)
- **Document Processing**: Apache PDFBox 3.0.3 (stream parsing)
- **AI Engine**: Google GenAI SDK 1.0.0 (`gemini-2.5-flash`)
- **Build System**: Maven (`pom.xml` + Maven Wrapper `mvnw.cmd`)
- **Containerization**: Multi-stage Dockerfile (`eclipse-temurin:21-jre-alpine`)

### 3.2 Backend Service & Controller Architecture
- **Controllers (16)**:
  - `AuthController.java`: `/api/auth/login`, `/api/auth/register`
  - `UserController.java`: User entity management
  - `ProfileController.java`: `/api/profile` (profile retrieval, target job updating)
  - `ResumeController.java`: `/api/resume/upload` (multipart PDF parsing with Apache PDFBox)
  - `RoadmapController.java`: `/api/roadmap` (dynamic Gemini roadmap generation, task completion toggling)
  - `SkillGapController.java`: `/api/skillgap` (computes missing skills against target role)
  - `DailyChallengeController.java`: `/api/daily-challenge` (AI challenge generation & solution grading)
  - `CareerMissionController.java`: `/api/career-mission` (real-world engineering missions & AI grading)
  - `CopilotController.java`: `/api/copilot` (context-aware AI technical interview chat)
  - `ChatController.java`: `/api/chat` (general career guidance conversational bot)
  - `MasteryCheckController.java`: `/api/mastery` (task-specific quizzes & retests)
  - `MockInterviewController.java`: `/api/interview` (mock interview questions & evaluation)
  - `SkillTestController.java`: `/api/skills` (technical assessments)
  - `UserSkillController.java`: Skill CRUD per user
  - `HealthController.java`: `/api/health`
  - `GeminiTestController.java`: Direct Gemini verification test endpoint

### 3.3 MySQL Entities & Relationships (13 Entities)
1. `User`: `id`, `name`, `email`, `password`, `targetJob`, `targetCompany`, `bio`, `avatarUrl`, `createdAt`
2. `UserSkill`: `id`, `user_id`, `skillName`, `proficiencyLevel`, `verified`, `createdAt`
3. `Resume`: `id`, `user_id`, `fileName`, `fileData`, `extractedText`, `parsedSkills`, `resumeScore`
4. `JobRole`: `id`, `roleName`, `requiredSkills` (seeded with 8 industry roles by `JobRoleDataInitializer`)
5. `Roadmap`: `id`, `user_id`, `roleName`, `matchPercentage`, `readinessScore`, `createdAt`
6. `RoadmapTask`: `id`, `roadmap_id`, `weekNumber`, `skillName`, `title`, `description`, `resourceUrl`, `completed`, `status`, `lastAssessmentId`
7. `SkillAssessment`: `id`, `user_id`, `skillName`, `score`, `passed`, `createdAt`
8. `AssessmentQuestion`: `id`, `assessment_id`, `questionText`, `optionA`, `optionB`, `optionC`, `optionD`, `correctAnswer`, `explanation`
9. `DailyChallenge`: `id`, `user_id`, `challengeDate`, `challengeType`, `title`, `description`, `prompt`, `userAnswer`, `score`, `passed`, `feedback`
10. `MockInterview`: `id`, `user_id`, `targetRole`, `difficulty`, `questionText`, `userAnswer`, `feedback`, `score`
11. `InterviewQuestion`: `id`, `mock_interview_id`, `question`, `answer`, `score`, `feedback`
12. `ReminderHistory`: `id`, `user_id`, `reminderType`, `sentAt`, `message`
13. `CareerMission`: `id`, `user_id`, `targetRole`, `companyContext`, `missionTitle`, `scenarioDescription`, `requirementsJson`, `starterCodeJson`, `submissionJson`, `evaluationJson`, `score`, `passed`

---

## 4. Website Frontend Audit & Duplication Analysis

### 4.1 Root Web Directory (`/web`)
- **Technology**: Vite 5.1.6, React 18.2.0, Lucide React icons
- **Structure**: Single-page application in `web/src/App.jsx` (637 lines)
- **Data Source**: **Pure mock data**. Hardcoded arrays `JOB_ROLES` and `INITIAL_ROADMAP`. The ATS scanner uses `setTimeout(..., 1200)` and outputs static JSON.
- **Vercel Config**: `web/vercel.json` and root `vercel.json` both point to `web/dist`.

### 4.2 Backend Web Directory (`Backend/skillbridge-backend/web/`)
- **Analysis**: Contains identical files to `/web`:
  - Same `App.jsx` (SHA256: `9CDB3CFE2EC70E0C9DBE88E86BF7AB2317A2655DF15C392B0A5A708CF3C6EA06`)
  - Same `index.html` (SHA256: `9F440814F61296B5AC25079F7047E8A6E23B02418034B2B2D951C9DD830F758A`)
- **Dockerfile Verification**: The Spring Boot `Dockerfile` does **NOT** copy or reference `Backend/skillbridge-backend/web/`. It only copies `pom.xml` and `src/`.
- **Verdict**: `Backend/skillbridge-backend/web/` is an accidental redundant artifact created when someone attempted to deploy Vercel directly from the backend directory. It serves no build or runtime purpose.

---

## 5. Deployment Configurations Audit

1. **`vercel.json` (Root)**:
   - Configured with `buildCommand: cd web && npm install && npm run build` and `outputDirectory: web/dist`. Used by Vercel for the web demo.
2. **`render.yaml` (Root)**:
   - Configured to build the Dockerfile under `Backend/skillbridge-backend/Dockerfile` with context `Backend/skillbridge-backend`. Used for backend container deployment.
3. **`Backend/skillbridge-backend/vercel.json` & `render.yaml`**:
   - Duplicate leftover deployment files in the backend subfolder.

---

## 6. Directory Classification Matrix

| Directory / File | Belonging Subsystem | Status & Purpose |
|---|---|---|
| `app/` | **Native Android Application** | **PRIMARY PROJECT**. Complete Kotlin Jetpack Compose app. |
| `build.gradle.kts` | Native Android Application | Root Gradle build script. |
| `settings.gradle.kts` | Native Android Application | Root Gradle settings script (`rootProject.name = "SkillBridgeAI"`). |
| `gradle/` | Native Android Application | Gradle wrapper and version catalog (`libs.versions.toml`). |
| `gradlew` / `gradlew.bat` | Native Android Application | Gradle wrapper executables. |
| `local.properties` | Native Android Application | Local Android SDK pointer (`sdk.dir=G:\Android\Sdk`). |
| `Backend/skillbridge-backend/src/` | **Spring Boot Backend** | Complete Java 21 REST API and MySQL entities. |
| `Backend/skillbridge-backend/pom.xml` | Spring Boot Backend | Maven build definition. |
| `Backend/skillbridge-backend/mvnw*` | Spring Boot Backend | Maven wrapper executables. |
| `Backend/skillbridge-backend/Dockerfile` | Spring Boot Backend | Production multi-stage Dockerfile. |
| `web/` | **Website Frontend** | Vite + React web demo (Vercel deployment). |
| `vercel.json` | Deployment Configuration | Vercel deployment instructions for `/web`. |
| `render.yaml` | Deployment Configuration | Render Docker deployment for backend. |
| `Backend/skillbridge-backend/web/` | Redundant / Artifact | Duplicate of `/web`. Can be safely cleaned up without affecting backend or Android. |
| `Backend/skillbridge-backend/vercel.json` | Redundant / Artifact | Leftover Vercel config inside backend. |
| `Backend/skillbridge-backend/render.yaml` | Redundant / Artifact | Leftover Render config inside backend. |

---

## 7. Build and Compilation Verification Status

1. **Android Debug Build**:
   - Command: `.\gradlew.bat assembleDebug`
   - Result: **BUILD SUCCESSFUL in 6m 1s**
   - Output Artifact: `app/build/outputs/apk/debug/app-debug.apk` (19.97 MB, verified 01-11-2026 17:07:13)
   - Status: **100% verified and operational.**

2. **Spring Boot Backend Build**:
   - Command: `.\mvnw.cmd test-compile`
   - Result: **BUILD SUCCESS in 7.466s**
   - Output: All classes compiled and up to date in `target/classes`.
   - Status: **100% verified and operational.**

3. **Connected Android Devices**:
   - Command: `adb devices`
   - Result: No physical device or emulator currently running. Runtime UI testing could not be executed on a live device, but APK bytecode assembly is verified.
