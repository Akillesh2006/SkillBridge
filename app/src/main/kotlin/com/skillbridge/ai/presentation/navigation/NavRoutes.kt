package com.skillbridge.ai.presentation.navigation

/**
 * Sealed class representing every navigation destination in the application.
 *
 * Each object holds a unique [route] string used by [androidx.navigation.NavHost].
 *
 * Convention:
 * - Static routes: lowercase snake_case string literal.
 * - Routes with arguments: include `/{argName}` segments and define [requiredArgs].
 */
sealed class Screen(val route: String) {

    // ── Authentication Flow ────────────────────────────────────────────────────

    /** App entry point — shown once on cold start. */
    data object Splash : Screen("splash")

    /** Login / sign-in screen. */
    data object Login : Screen("login")

    /** New account registration screen. */
    data object Register : Screen("register")

    /** Forgot password recovery screen. */
    data object ForgotPassword : Screen("forgot_password")

    // ── Main App Flow ─────────────────────────────────────────────────────────

    /** Home dashboard — overview of progress and quick actions. */
    data object Dashboard : Screen("dashboard")

    /** Resume upload and management screen. */
    data object Resume : Screen("resume")

    /** AI Analysis result of the resume. */
    data object ResumeResult : Screen("resume_result")

    /** Target job role selection screen. */
    data object JobSelection : Screen("job_selection")

    /** Skill gap analysis results screen. */
    data object SkillGap : Screen("skill_gap")

    /** Personalised learning roadmap screen. */
    data object Roadmap : Screen("roadmap")

    /** Learning progress tracking screen. */
    data object Progress : Screen("progress")

    /** User profile and settings screen. */
    data object Profile : Screen("profile")

    /** Analytics dashboard for career readiness. */
    data object Analytics : Screen("analytics")

    /** AI Career Assistant chat screen. */
    data object Chat : Screen("chat")

    /** About SkillBridge AI screen. */
    data object About : Screen("about")

    /** Skill verification MCQ test screen. */
    data object SkillTest : Screen("skill_test")

    /** Skill verification test results screen. */
    data object SkillTestResult : Screen("skill_test_result/{assessmentId}")

    /** Skill mastery check screen. */
    data object MasteryCheck : Screen("mastery_check/{taskId}?retestId={retestId}")

    /** Skill mastery check results screen. */
    data object MasteryCheckResult : Screen("mastery_check_result/{assessmentId}")

    /** Daily technical challenge screen. */
    data object DailyChallenge : Screen("daily_challenge")

    /** Real-world AI career mission simulator screen. */
    data object CareerMission : Screen("career_mission")
}
