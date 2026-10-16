package com.skillbridge.ai.presentation.screens.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.DashboardViewModel

import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Career Dashboard",
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = "Profile",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            SkillBridgeBottomNavBar(
                currentRoute = Screen.Dashboard.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LoadingScreenComponent()
            }
            is UiState.Error -> {
                ErrorStateComponent(
                    message = state.message,
                    onRetry = { viewModel.loadDashboardData() }
                )
            }
            is UiState.Empty -> {
                EmptyStateComponent(message = "No dashboard information found.")
            }
            is UiState.Success -> {
                val data = state.data
                val studentName = data.studentName.ifBlank { "Akillesh" }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(DeepNavy)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Top Greeting Section ──────────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Good day, $studentName 👋",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    AIStatusIndicator()
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = ElectricBlue.copy(alpha = 0.12f),
                                        modifier = Modifier.border(
                                            1.dp,
                                            ElectricBlue.copy(alpha = 0.2f),
                                            RoundedCornerShape(6.dp)
                                        )
                                    ) {
                                        Text(
                                            text = data.targetJob,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                             }
                         }
                     }

                     // ── Target Career Card ────────────────────────────────────
                     item {
                         GlassCard(
                             modifier = Modifier
                                 .fillMaxWidth()
                                 .clickable { navController.navigate("job_selection") }
                         ) {
                             Column(modifier = Modifier.padding(20.dp)) {
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.SpaceBetween,
                                     verticalAlignment = Alignment.CenterVertically
                                 ) {
                                     Column(modifier = Modifier.weight(1.0f)) {
                                         Text(
                                             text = "🎯 YOUR CAREER TARGET",
                                             style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                             color = ElectricBlue
                                         )
                                         Spacer(modifier = Modifier.height(4.dp))
                                         Text(
                                             text = data.targetJob,
                                             style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                             color = TextPrimary
                                         )
                                         Spacer(modifier = Modifier.height(2.dp))
                                         Text(
                                             text = "🏢 " + data.targetCompany,
                                             style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                             color = NeonGreen
                                         )
                                     }
                                     
                                     Column(horizontalAlignment = Alignment.End) {
                                         Text(
                                             text = "${data.preparationMatch}%",
                                             style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                             color = NeonGreen
                                         )
                                         Text(
                                             text = "Prep Match",
                                             style = MaterialTheme.typography.labelSmall,
                                             color = TextMuted
                                         )
                                     }
                                 }
                                 
                                 Spacer(modifier = Modifier.height(14.dp))
                                 LinearProgressIndicator(
                                     progress = data.preparationMatch.toFloat() / 100f,
                                     color = NeonGreen,
                                     trackColor = GlassWhite10,
                                     modifier = Modifier.fillMaxWidth().height(4.dp)
                                 )
                                 Spacer(modifier = Modifier.height(10.dp))
                                 Row(
                                     modifier = Modifier.fillMaxWidth(),
                                     horizontalArrangement = Arrangement.End
                                 ) {
                                     Text(
                                         text = "Configure Target →",
                                         style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                         color = ElectricBlue
                                     )
                                 }
                             }
                         }
                     }

                     // ── Career Readiness Hero Card ────────────────────────────
                     item {
                         CareerReadinessHeroCard(
                             overallReadiness = data.preparationMatch,
                             onClick = { navController.navigate(Screen.Analytics.route) }
                         )
                     }

                    // ── Metric Grid (2x2) ─────────────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricGlassCard(
                                label = "Resume Score",
                                value = "87%",
                                icon = Icons.Rounded.Description,
                                accentColor = ElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            MetricGlassCard(
                                label = "Skill Match",
                                value = "74%",
                                icon = Icons.Rounded.Star,
                                accentColor = NeonViolet,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricGlassCard(
                                label = "Roadmap",
                                value = "65%",
                                icon = Icons.Rounded.Timeline,
                                accentColor = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            MetricGlassCard(
                                label = "Interview",
                                value = "50%",
                                icon = Icons.Rounded.QuestionAnswer,
                                accentColor = NeonAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // ── Today's AI Mission Section ─────────────────────────────
                    item {
                        val missionFocus = data.dailyMissionFocus
                        if (missionFocus != null) {
                            SkillBridgeSectionHeader(title = "Today's AI Mission")
                            GlassCard(glowColor = NeonCyan) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "🎯 Focus: $missionFocus",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = data.dailyMissionReason ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Tasks:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    data.dailyMissionTasks.forEach { task ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "□  $task",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Estimated Time: ${data.dailyMissionTime} minutes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    SkillBridgePrimaryButton(
                                        text = "START MISSION",
                                        onClick = { navController.navigate(Screen.Roadmap.route) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // ── Skill Verification Section ────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Skill Verification")
                        val score = data.verificationScore
                        val completed = score != null

                        GlassCard(
                            glowColor = if (completed) NeonCyan else NeonRed
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (completed) "Verification Score: $score%" else "Verify Your Skills",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (completed) {
                                            "Verified Skills: ${data.verifiedSkillsCount} / ${data.totalSkillsCount}"
                                        } else {
                                            "Take an AI MCQ test to verify your resume skills."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Icon(
                                        imageVector = if (completed) Icons.Rounded.VerifiedUser else Icons.Rounded.Rule,
                                        contentDescription = null,
                                        tint = if (completed) NeonCyan else NeonRed,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            SkillBridgePrimaryButton(
                                text = if (completed) "View Verification Results" else "Verify Your Skills",
                                onClick = {
                                    if (completed) {
                                        navController.navigate("skill_test_result/${data.latestAssessmentId}")
                                    } else {
                                        navController.navigate(Screen.SkillTest.route)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── Priority Skill Gaps ───────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Priority Skill Gaps")
                        GlassCard(
                            modifier = Modifier.clickable { navController.navigate(Screen.SkillGap.route) },
                            glowColor = NeonRed
                        ) {
                            PrioritySkillRow("Spring Boot", "HIGH")
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = GlassWhite8
                            )
                            PrioritySkillRow("REST APIs", "HIGH")
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = GlassWhite8
                            )
                            PrioritySkillRow("Docker Containers", "MEDIUM")
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = GlassWhite8
                            )
                            PrioritySkillRow("Cloud Deployment", "LOW")
                        }
                    }

                    // ── Continue Learning ─────────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Continue Learning")
                        GlassCard(glowColor = NeonCyan) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricBlue.copy(alpha = 0.15f),
                                    modifier = Modifier.border(
                                        1.dp,
                                        ElectricBlue.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text(
                                        text = "WEEK 2",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricBlue,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = "Progress: 65%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Spring Boot & REST API Architecture",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NeonProgressBar(
                                progress = 0.65f,
                                gradientStart = ElectricBlue,
                                gradientEnd = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            SkillBridgePrimaryButton(
                                text = "Continue Learning →",
                                onClick = { navController.navigate(Screen.Roadmap.route) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── Daily Challenge ────────────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Daily Challenge")
                        GlassCard(glowColor = NeonAmber) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ TODAY'S CHALLENGE",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonAmber
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonAmber.copy(alpha = 0.12f),
                                    modifier = Modifier.border(
                                        1.dp,
                                        NeonAmber.copy(alpha = 0.2f),
                                        RoundedCornerShape(6.dp)
                                    )
                                ) {
                                    Text(
                                        text = "Medium • 20 min",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonAmber,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Build a REST API endpoint that extracts user skills and updates readiness metrics.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            SkillBridgeOutlinedButton(
                                text = "Start Challenge",
                                onClick = { navController.navigate(Screen.DailyChallenge.route) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── AI Career Mission Simulator ─────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "AI Career Mission")
                        val isMissionDone = data.isCareerMissionCompleted
                        val missionScore = data.careerMissionScore

                        GlassCard(glowColor = if (isMissionDone) NeonGreen else NeonViolet) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🚀 REAL-WORLD WORKPLACE SIMULATION",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isMissionDone) NeonGreen else NeonViolet
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isMissionDone) NeonGreen.copy(alpha = 0.12f) else NeonViolet.copy(alpha = 0.12f),
                                        modifier = Modifier.border(
                                            1.dp,
                                            if (isMissionDone) NeonGreen.copy(alpha = 0.25f) else NeonViolet.copy(alpha = 0.25f),
                                            RoundedCornerShape(6.dp)
                                        )
                                    ) {
                                        Text(
                                            text = if (isMissionDone) "Score: $missionScore/100" else "${data.targetJob}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isMissionDone) NeonGreen else NeonCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = data.careerMissionTitle ?: "Prove your skills in a real-world ${data.targetJob} job scenario.",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (isMissionDone) {
                                        "Mission evaluated by AI Hiring Manager. View breakdown, technical strengths, and next recommended simulation."
                                    } else {
                                        data.careerMissionScenario ?: "Simulate solving production-level engineering challenges with AI mentor evaluation."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(16.dp))
                                if (isMissionDone) {
                                    SkillBridgePrimaryButton(
                                        text = "VIEW EVALUATION & NEXT MISSION →",
                                        onClick = { navController.navigate(Screen.CareerMission.route) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    SkillBridgePrimaryButton(
                                        text = "START MISSION →",
                                        onClick = { navController.navigate(Screen.CareerMission.route) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    // ── AI Assistant Card ──────────────────────────────────────
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            NeonViolet.copy(alpha = 0.3f),
                                            NeonCyan.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { navController.navigate(Screen.Chat.route) },
                            shape = RoundedCornerShape(20.dp),
                            color = NeonViolet.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "✨ SkillBridge AI Assistant",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        AIStatusIndicator()
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Ask about your career, personalized roadmap, or skill gaps.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextMuted
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Ask SkillBridge AI →",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeonViolet
                                    )
                                }
                            }
                        }
                    }

                    // ── Quick Actions Grid ────────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Quick Actions")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                icon = Icons.Rounded.Description,
                                label = "Resume",
                                accentColor = ElectricBlue,
                                onClick = { navController.navigate(Screen.Resume.route) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                icon = Icons.Rounded.Timeline,
                                label = "Roadmap",
                                accentColor = NeonCyan,
                                onClick = { navController.navigate(Screen.Roadmap.route) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                icon = Icons.Rounded.Analytics,
                                label = "Skills",
                                accentColor = NeonGreen,
                                onClick = { navController.navigate(Screen.SkillGap.route) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickActionCard(
                                icon = Icons.Rounded.TrendingUp,
                                label = "Progress",
                                accentColor = NeonAmber,
                                onClick = { navController.navigate(Screen.Progress.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── SUPPORTING COMPOSABLES ────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PrioritySkillRow(skill: String, priority: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = skill,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        PriorityBadge(priority = priority)
    }
}

@Composable
fun CareerReadinessHeroCard(
    overallReadiness: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.3f),
                        NeonCyan.copy(alpha = 0.2f),
                        NeonViolet.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GradientBlueStart,
                            GradientCyanStart.copy(alpha = 0.8f),
                            GradientVioletStart.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Career Readiness",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f),
                            modifier = Modifier.border(
                                1.dp,
                                NeonGreen.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = "High Potential 🚀",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Neon circular gauge
                    Box(contentAlignment = Alignment.Center) {
                        val arcColor = NeonCyan
                        val trackColor = GlassWhite10
                        Canvas(modifier = Modifier.size(80.dp)) {
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = arcColor,
                                startAngle = -90f,
                                sweepAngle = 360f * overallReadiness / 100f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = "$overallReadiness%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = NeonCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sub-metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniMetric(label = "Resume", value = "87%", color = ElectricBlue, modifier = Modifier.weight(1f))
                    MiniMetric(label = "Skills", value = "74%", color = NeonViolet, modifier = Modifier.weight(1f))
                    MiniMetric(label = "Roadmap", value = "65%", color = NeonCyan, modifier = Modifier.weight(1f))
                    MiniMetric(label = "Interview", value = "50%", color = NeonAmber, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MiniMetric(label: String, value: String, color: Color = TextPrimary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
fun MetricGlassCard(
    label: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = GlassWhite6
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accentColor
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = GlassWhite4
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
        }
    }
}
