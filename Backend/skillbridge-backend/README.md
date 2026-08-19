# 🚀 SkillBridge - AI-Powered Career Roadmap & Skill Acceleration Platform

[![Vercel Deployment](https://img.shields.io/badge/Vercel-Web_App_Deployed-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://skillbridge-app.vercel.app)
[![Vercel Account](https://img.shields.io/badge/Vercel_Projects-akillesh2006s--projects-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vercel.com/akillesh2006s-projects)
[![Backend](https://img.shields.io/badge/Backend-Java_17_Spring_Boot_3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](file:///g:/projects/SkillBridge/Backend/skillbridge-backend)
[![Android App](https://img.shields.io/badge/Android-Kotlin_Jetpack_Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white)](file:///g:/projects/SkillBridge/app)
[![Docker](https://img.shields.io/badge/Docker-Multi_Stage_Build-2496ED?style=for-the-badge&logo=docker&logoColor=white)](file:///g:/projects/SkillBridge/Backend/skillbridge-backend/Dockerfile)

---

## 🌐 Live Web Application & Vercel Deployment

- **Live Web App URL**: [https://skillbridge-app.vercel.app](https://skillbridge-app.vercel.app)
- **Vercel Account Dashboard**: [https://vercel.com/akillesh2006s-projects](https://vercel.com/akillesh2006s-projects)
- **GitHub Repository**: [https://github.com/Akillesh2006/SkillBridge](https://github.com/Akillesh2006/SkillBridge)

---

## 🌟 Key Platform Features

### 1. 📄 Resume ATS Analyzer Engine
- Automated text & PDF parsing (powered by Apache PDFBox on backend).
- Real-time ATS match percentage scoring against target job roles.
- Instant extraction of verified technical skills vs. missing critical keywords.

### 2. 🗺️ Interactive AI Career Roadmap Visualizer
- Dynamic milestone tracking (Foundations, Advanced Architecture, System Design, Certifications).
- Milestone checkboxes with real-time percentage progress bar updates.
- Target role presets: Full Stack Java/React Engineer, Android Engineer, AI/ML Developer, Cloud DevOps Architect.

### 3. 🎯 Skill Gap Assessment & Competency Tests
- Interactive multiple-choice engineering quizzes.
- Instant score updates and detailed architectural explanations.

### 4. 🤖 AI Mock Interview Copilot
- Real-time interactive technical interview simulator.
- Answers questions on System Design, Spring Boot microservices, ATS optimization, and Docker cloud deployment.

---

## 🏗️ Platform Architecture

```
                                  +---------------------------------------+
                                  |    SkillBridge Web App (Vite/React)  |
                                  |       Deployed on Vercel Cloud        |
                                  +-------------------+-------------------+
                                                      |
                                                      v
                                  +-------------------+-------------------+
                                  |    SkillBridge Java Backend API       |
                                  |    Spring Boot 3 + REST Controllers   |
                                  |     Hosted on Render / Railway        |
                                  +-------------------+-------------------+
                                                      |
                                                      v
                                  +-------------------+-------------------+
                                  |    Native Android Mobile App          |
                                  |  Kotlin + Compose + Hilt + Room       |
                                  +---------------------------------------+
```

---

## ⚡ 1-Click Cloud Deployment

### Deploy Web App to Vercel
Click the button below to deploy your own instance of the SkillBridge Web App to Vercel:

[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=https%3A%2F%2Fgithub.com%2FAkillesh2006%2FSkillBridge)

Or import this repository directly inside [Vercel Dashboard](https://vercel.com/akillesh2006s-projects):
1. Go to [https://vercel.com/new](https://vercel.com/new).
2. Select your `SkillBridge` repository.
3. Vercel automatically detects `vercel.json` and builds the `/web` frontend application!

### Deploy Backend to Render (Docker)
[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/Akillesh2006/SkillBridge)

---

## 🛠️ Local Development Setup

### 1. Web Application (React / Vite)
```bash
cd web
npm install
npm run dev
# Opens local web app at http://localhost:3000
```

### 2. Java Backend API (Spring Boot)
```bash
cd Backend/skillbridge-backend
./mvnw spring-boot:run
# Backend REST server runs at http://localhost:8080
```

### 3. Native Android App (Kotlin / Gradle)
Open the root directory in **Android Studio** and run on an emulator or physical Android device.

---

## 📄 License
Created by Akillesh K P. Distributed under the MIT License.
