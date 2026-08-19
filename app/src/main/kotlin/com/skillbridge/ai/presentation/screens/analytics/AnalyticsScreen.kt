package com.skillbridge.ai.presentation.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.GlassCard
import com.skillbridge.ai.presentation.components.LoadingScreenComponent
import com.skillbridge.ai.presentation.components.NeonProgressBar
import com.skillbridge.ai.presentation.components.SkillBridgePrimaryButton
import com.skillbridge.ai.presentation.components.SkillBridgeSectionHeader
import com.skillbridge.ai.presentation.components.SkillBridgeTopAppBar
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Career Analytics",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SpaceBlack, DeepNavy, DarkNavy)
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    LoadingScreenComponent()
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = NeonRed, fontWeight = FontWeight.Bold)
                    }
                }
                is UiState.Empty, is UiState.Success -> {
                    val data = (state as? UiState.Success)?.data ?: return@Scaffold

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Target Career Match",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${data.targetJob} @ ${data.targetCompany}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = NeonGreen
                            )
                        }

                        item {
                            GlassCard(glowColor = NeonCyan) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Target Preparation Match",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = NeonCyan
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                        Canvas(modifier = Modifier.size(120.dp)) {
                                            drawArc(
                                                color = GlassWhite8,
                                                startAngle = -90f,
                                                sweepAngle = 360f,
                                                useCenter = false,
                                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                            drawArc(
                                                color = NeonCyan,
                                                startAngle = -90f,
                                                sweepAngle = 360f * data.overallScore / 100f,
                                                useCenter = false,
                                                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                            )
                                        }
                                        Text(
                                            text = "${data.overallScore}%",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = NeonCyan
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Your match level is dynamically calculated against required target job skills and verified roadmap completions.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        item {
                            SkillBridgeSectionHeader(title = "Readiness Metrics")
                        }

                        item {
                            AnalyticsMetricCard(title = "Resume Match", progress = data.resumeMatch / 100f, color = ElectricBlue, icon = Icons.Rounded.Description)
                        }
                        item {
                            AnalyticsMetricCard(title = "Verified Skills", progress = data.verifiedScore / 100f, color = NeonViolet, icon = Icons.Rounded.CheckCircle)
                        }
                        item {
                            AnalyticsMetricCard(title = "Roadmap Progress", progress = data.roadmapProgress / 100f, color = NeonCyan, icon = Icons.Rounded.TrendingUp)
                        }
                        item {
                            AnalyticsMetricCard(title = "Skill Mastery", progress = data.masteryScore / 100f, color = NeonGreen, icon = Icons.Rounded.Star)
                        }
                        item {
                            AnalyticsMetricCard(title = "Mock Interview", progress = data.interviewScore / 100f, color = NeonAmber, icon = Icons.Rounded.QuestionAnswer)
                        }
                        item {
                            AnalyticsMetricCard(title = "Daily Challenges", progress = data.challengeScore / 100f, color = NeonRed, icon = Icons.Rounded.Assignment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricCard(title: String, progress: Float, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GlassCard(glowColor = color) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                color = color.copy(alpha = 0.12f),
                shape = CircleShape,
                modifier = Modifier
                    .size(44.dp)
                    .border(1.dp, color.copy(alpha = 0.3f), CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontWeight = FontWeight.ExtraBold,
                        color = color,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                NeonProgressBar(
                    progress = progress,
                    gradientStart = color,
                    gradientEnd = color.copy(alpha = 0.6f)
                )
            }
        }
    }
}
