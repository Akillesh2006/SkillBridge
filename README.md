---
title: SkillBridge
emoji: 🚀
colorFrom: indigo
colorTo: purple
sdk: docker
app_port: 3000
pinned: true
---

<div align="center">

# SkillBridge

### An AI-powered career roadmap & skill acceleration platform that analyzes resumes, detects ATS skill gaps, generates dynamic career pathways, and conducts interactive AI mock interviews — in seconds.

[![Live Demo on Vercel](https://img.shields.io/badge/%F0%9F%9A%80%20Live%20Demo-Vercel%20Web%20App-6366f1?style=for-the-badge&logo=vercel&logoColor=white)](https://skillbridge-app.vercel.app)
[![Vercel Projects](https://img.shields.io/badge/Vercel-akillesh2006s--projects-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vercel.com/akillesh2006s-projects)
[![Render Docker API](https://img.shields.io/badge/%E2%9A%A1%20Backend-Render%20Docker-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://render.com)

[![React](https://img.shields.io/badge/React-18.2-61DAFB?style=flat-square&logo=react&logoColor=black)](https://reactjs.org)
[![Vite](https://img.shields.io/badge/Vite-5.1-646CFF?style=flat-square&logo=vite&logoColor=white)](https://vitejs.dev)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](./LICENSE)

</div>

---

> ## 🚀 Quick Links & Live Demonstrations
>
> - **🌐 Live Web App (Vercel)**: [https://skillbridge-app.vercel.app](https://skillbridge-app.vercel.app)
> - **☁️ Vercel Projects Dashboard**: [https://vercel.com/akillesh2006s-projects](https://vercel.com/akillesh2006s-projects)
> - **📦 GitHub Repository**: [https://github.com/Akillesh2006/SkillBridge](https://github.com/Akillesh2006/SkillBridge)

---

## 🎯 The problem

Millions of aspiring developers, engineers, and career switchers struggle to land tech roles due to unoptimized resumes, hidden ATS (Applicant Tracking System) rejections, and unclear learning pathways. 

- **ATS Rejection Loop**: Resumes fail initial automated screeners due to missing target keywords and metrics.
- **Fragmented Roadmaps**: Self-learners spend hours hunting for structured, role-specific learning steps.
- **Lack of Realistic Practice**: Candidates enter technical interviews without actionable feedback on system design or coding concepts.

**SkillBridge** solves this with an end-to-end AI acceleration platform combining automated resume parsing, ATS scoring, dynamic career roadmap generation, competency quizzes, and an AI mock interview copilot.

---

## ✨ What makes it different

Key architectural innovations built into SkillBridge:

| | Innovation | Why it matters |
|---|---|---|
| 📄 | **Graph & PDF Keyword Extractor** — Apache PDFBox + LLM parsing | Extracts verified technical skills directly from resumes and compares them against target job profile requirements with zero manual input. |
| 🗺️ | **Dynamic Milestone Roadmap Visualizer** | Transforms target job roles (*Full Stack, Android, AI/ML, Cloud Architect*) into multi-phase milestone paths with real-time percentage tracking. |
| 🎯 | **Competency Assessment Engine** | Interactive technical quizzes that score system architecture readiness and provide immediate explanatory feedback. |
| 🤖 | **AI Mock Interview Copilot** | Conversational technical assistant that conducts realistic interview simulations, reviews architecture choices, and offers ATS resume optimization tips. |

---

## 🏗️ Architecture

```mermaid
flowchart TD
    User([👤 Candidate / Student])
    
    subgraph Clients ["Client Interfaces"]
        WebApp[🌐 SkillBridge Web App<br/><i>React 18 · Vite · HSL Glassmorphism</i><br/><b>Deployed on Vercel Cloud</b>]
        AndroidApp[📱 Native Android App<br/><i>Kotlin · Jetpack Compose · Hilt · Room</i>]
    end

    User -->|web browser| WebApp
    User -->|mobile device| AndroidApp

    subgraph BackendAPI ["Java Backend Microservice"]
        direction TB
        BootAPI[⚡ Spring Boot 3 REST API<br/><b>Docker Container on Render/Railway</b>]
        PDFEngine[📄 Apache PDFBox Parser<br/><i>Document text extraction</i>]
        SkillGapEngine[🎯 Skill Gap Matcher<br/><i>Target role keyword comparator</i>]
        RoadmapEngine[🗺️ Dynamic Roadmap Builder<br/><i>Milestone sequencing engine</i>]
        AICopilot[🤖 AI Interview Copilot<br/><i>Prompt & conversational agent</i>]
    end

    WebApp -->|REST HTTP / JSON| BootAPI
    AndroidApp -->|Retrofit / REST| BootAPI

    BootAPI --> PDFEngine
    BootAPI --> SkillGapEngine
    BootAPI --> RoadmapEngine
    BootAPI --> AICopilot

    subgraph DataStore ["Data & Storage Layer"]
        Database[(SQL Database<br/>Users · Resumes · Tasks)]
        Uploads[📁 Secure File Storage]
    end

    BootAPI --> Database
    BootAPI --> Uploads

    classDef client fill:#6366f1,stroke:#4f46e5,color:#fff,stroke-width:2px
    classDef backend fill:#10b981,stroke:#059669,color:#fff
    classDef data fill:#f59e0b,stroke:#d97706,color:#fff
    class WebApp,AndroidApp client
    class BootAPI,PDFEngine,SkillGapEngine,RoadmapEngine,AICopilot backend
    class Database,Uploads data
```

---

## 🔁 User & Execution Sequence

Trace of a candidate preparing for a **Full Stack Java + React Engineer** position:

```mermaid
sequenceDiagram
    autonumber
    participant U as Candidate / User
    participant W as Web App (Vercel)
    participant B as Spring Boot API (Render)
    participant PDF as Apache PDFBox Engine
    participant LLM as AI Copilot Agent

    U->>W: Upload / Paste Resume Content
    W->>B: POST /api/resume/upload
    B->>PDF: Extract text & parse skill tokens
    PDF-->>B: Extracted Skills (Java, Spring, Docker, Git...)
    B->>B: Compute ATS Match % against Target Job Role
    B-->>W: ATS Score (86%) + Missing Keywords (GraphQL, Redis, System Design)

    U->>W: Request AI Career Roadmap
    W->>B: GET /api/roadmap?role=FullStack
    B-->>W: Multi-Phase Milestones (Foundations -> Containerization -> System Design)

    U->>W: Click Step Checkboxes
    W->>W: Update Progress Bar Real-Time (60%)

    U->>W: Launch AI Mock Interview Copilot
    W->>LLM: Send Technical Question / Answer
    LLM-->>W: Immediate Architectural & System Design Feedback
```

---

## 💻 Tech Stack Overview

### 1. Web Application (`/web`)
- **Framework**: React 18 with Vite 5
- **Styling**: Custom Glassmorphism UI Design System with HSL tailwinds & Outfit/Inter typography
- **Icons & Visuals**: Lucide Icons & Responsive Grid layout
- **Deployment**: **Vercel Cloud** (`vercel.json` SPA build configuration)

### 2. Backend API (`/Backend/skillbridge-backend`)
- **Framework**: Java 17 with Spring Boot 3.x
- **Parsing**: Apache PDFBox for PDF resume document extraction
- **Security & Auth**: Spring Security with JWT (JSON Web Tokens)
- **Containerization**: Multi-stage `Dockerfile` ready for Render / Railway / Fly.io

### 3. Native Android App (`/app`)
- **Language & Framework**: Kotlin with Jetpack Compose
- **Architecture**: MVVM Clean Architecture
- **Dependency Injection**: Dagger Hilt
- **Networking**: Retrofit 2 + OkHttp 3

---

## 🛠️ Quick Start & Local Setup

### Prerequisites
- **Node.js** v18+ and `npm`
- **JDK 17+** and Maven
- **Android Studio** (for mobile app compilation)

### 1. Run Web Application (Vite / React)
```bash
# Navigate to web directory
cd web

# Install dependencies
npm install

# Start development server
npm run dev
# 🚀 Web app running at http://localhost:3000
```

### 2. Run Java Spring Boot Backend
```bash
# Navigate to backend directory
cd Backend/skillbridge-backend

# Build & run Spring Boot application
./mvnw spring-boot:run
# ⚡ REST API active at http://localhost:8080
```

### 3. Run with Docker
```bash
cd Backend/skillbridge-backend
docker build -t skillbridge-backend .
docker run -p 8080:8080 skillbridge-backend
```

---

## ⚡ 1-Click Cloud Deployment

### Deploy Web App to Vercel
Click the button below to deploy your own instance of SkillBridge to Vercel:

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https%3A%2F%2Fgithub.com%2FAkillesh2006%2FSkillBridge)

Or import this repository directly in your [Vercel Dashboard](https://vercel.com/akillesh2006s-projects):
1. Go to [https://vercel.com/new](https://vercel.com/new).
2. Connect your GitHub repository **`Akillesh2006/SkillBridge`**.
3. Vercel automatically detects `vercel.json` and builds the `/web` frontend!

### Deploy Backend to Render
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/Akillesh2006/SkillBridge)

---

## 📁 Repository Structure

```
SkillBridge/
├── README.md                      # Primary GitHub README (CivicMesh inspired layout)
├── vercel.json                    # Root Vercel cloud deployment routing
├── render.yaml                    # Render 1-click Docker backend deployment
├── web/                           # SkillBridge Web Application (Vite/React)
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   ├── vercel.json
│   └── src/
│       ├── App.jsx                # Feature-complete SkillBridge web dashboard
│       ├── main.jsx
│       └── index.css              # Custom Glassmorphism design system
├── Backend/                       # Java 17 Spring Boot Backend API
│   └── skillbridge-backend/
│       ├── Dockerfile
│       ├── pom.xml
│       └── src/main/java/com/skillbridge/
└── app/                           # Native Android Application (Kotlin)
    ├── build.gradle.kts
    └── src/main/
```

---

## 📄 License

Created by **[Akillesh K P](https://github.com/Akillesh2006)**. Distributed under the **MIT License**.
