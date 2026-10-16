package com.skillbridge.ai.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skillbridge.ai.presentation.screens.dashboard.DashboardScreen
import com.skillbridge.ai.presentation.screens.analytics.AnalyticsScreen
import com.skillbridge.ai.presentation.screens.about.AboutScreen
import com.skillbridge.ai.presentation.screens.chat.ChatScreen
import com.skillbridge.ai.presentation.screens.forgot.ForgotPasswordScreen
import com.skillbridge.ai.presentation.screens.jobselection.JobSelectionScreen
import com.skillbridge.ai.presentation.screens.login.LoginScreen
import com.skillbridge.ai.presentation.screens.profile.ProfileScreen
import com.skillbridge.ai.presentation.screens.progress.ProgressScreen
import com.skillbridge.ai.presentation.screens.register.RegisterScreen
import com.skillbridge.ai.presentation.screens.resume.ResumeAnalysisResultScreen
import com.skillbridge.ai.presentation.screens.resume.ResumeScreen
import com.skillbridge.ai.presentation.screens.roadmap.RoadmapScreen
import com.skillbridge.ai.presentation.screens.skillgap.SkillGapScreen
import com.skillbridge.ai.presentation.screens.splash.SplashScreen
import com.skillbridge.ai.presentation.screens.skilltest.SkillTestScreen
import com.skillbridge.ai.presentation.screens.skilltest.SkillTestResultScreen
import com.skillbridge.ai.presentation.screens.skilltest.MockInterviewScreen
import com.skillbridge.ai.presentation.screens.skilltest.DailyChallengeScreen
import com.skillbridge.ai.presentation.screens.mission.CareerMissionScreen
import com.skillbridge.ai.presentation.screens.analytics.ReadinessReportScreen
import com.skillbridge.ai.presentation.screens.mastery.MasteryCheckScreen
import com.skillbridge.ai.presentation.screens.mastery.MasteryCheckResultScreen

/**
 * Central Navigation Graph for SkillBridge AI.
 *
 * Registers every [Screen] destination and wires up the [NavHost].
 * The start destination is [Screen.Splash].
 *
 * Navigation actions will be added to each screen composable as the
 * feature milestones are implemented.
 *
 * @param navController The [NavHostController] driving navigation.
 *   Defaults to a remembered instance scoped to this composable.
 */
@Composable
fun SkillBridgeNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        }
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        composable(route = Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(route = Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        composable(route = Screen.Resume.route) {
            ResumeScreen(navController = navController)
        }

        composable(route = Screen.ResumeResult.route) {
            ResumeAnalysisResultScreen(navController = navController)
        }

        composable(route = Screen.JobSelection.route) {
            JobSelectionScreen(navController = navController)
        }

        composable(route = Screen.SkillGap.route) {
            SkillGapScreen(navController = navController)
        }

        composable(route = Screen.Roadmap.route) {
            RoadmapScreen(navController = navController)
        }

        composable(route = Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }

        composable(route = Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }

        composable(route = Screen.Chat.route) {
            ChatScreen(navController = navController)
        }

        composable(route = Screen.About.route) {
            AboutScreen(navController = navController)
        }

        composable(route = Screen.SkillTest.route) {
            SkillTestScreen(navController = navController)
        }

        composable(
            route = Screen.SkillTestResult.route,
            arguments = listOf(
                androidx.navigation.navArgument("assessmentId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val assessmentId = backStackEntry.arguments?.getLong("assessmentId") ?: -1L
            SkillTestResultScreen(navController = navController, assessmentId = assessmentId)
        }

        composable("mock_interview") {
            MockInterviewScreen(navController = navController)
        }

        composable(route = Screen.DailyChallenge.route) {
            DailyChallengeScreen(navController = navController)
        }

        composable(route = Screen.CareerMission.route) {
            CareerMissionScreen(navController = navController)
        }

        composable("career_readiness_report") {
            ReadinessReportScreen(navController = navController)
        }

        composable(route = Screen.JobSelection.route) {
            JobSelectionScreen(navController = navController)
        }

        composable(
            route = Screen.MasteryCheck.route,
            arguments = listOf(
                androidx.navigation.navArgument("taskId") {
                    type = androidx.navigation.NavType.LongType
                },
                androidx.navigation.navArgument("retestId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: -1L
            val retestIdStr = backStackEntry.arguments?.getString("retestId")
            val retestId = retestIdStr?.toLongOrNull()
            MasteryCheckScreen(navController = navController, taskId = taskId, retestId = retestId)
        }

        composable(
            route = Screen.MasteryCheckResult.route,
            arguments = listOf(
                androidx.navigation.navArgument("assessmentId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val assessmentId = backStackEntry.arguments?.getLong("assessmentId") ?: -1L
            MasteryCheckResultScreen(navController = navController, assessmentId = assessmentId)
        }
    }
}

