package com.skillbridge.ai.presentation.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.CopilotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadinessReportScreen(
    navController: NavController,
    viewModel: CopilotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCareerReadiness()
    }

    Scaffold(
        topBar = {
            SkillBridgeTopAppBar(
                title = "Career Readiness Report",
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
                .padding(20.dp)
        ) {
            when {
                uiState.isThinking && uiState.readiness == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan)
                    }
                }
                uiState.readiness != null -> {
                    val report = uiState.readiness!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        
                        // 1. Overall Score Ring
                        GlassCard(glowColor = NeonCyan) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "OVERALL JOB READINESS",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "${report.overallScore}%",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                NeonProgressBar(
                                    progress = report.overallScore / 100f,
                                    modifier = Modifier.fillMaxWidth(),
                                    gradientEnd = NeonCyan
                                )
                            }
                        }

                        // 2. Metrics Grid
                        Text(
                            text = "Performance Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricItem("Technical / Skill Score", report.technicalScore, NeonCyan)
                            MetricItem("Verified Assessment Score", report.verifiedScore, NeonGreen)
                            MetricItem("Roadmap Progress", report.roadmapProgress, ElectricBlue)
                            MetricItem("Mock Interview Average", report.interviewScore, ElectricBlue)
                            MetricItem("Daily Challenges Score", report.challengeScore, NeonRed)
                        }

                        // 3. Gemini Advisory Report Text
                        Text(
                            text = "AI Copilot Career Advisory",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        GlassCard(glowColor = GlassWhite8) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = sanitizeReport(report.reportJson),
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        SkillBridgePrimaryButton(
                            text = "BACK TO COPILOT",
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No readiness data available.", color = TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, score: Int, color: Color) {
    GlassCard(glowColor = color.copy(alpha = 0.3f)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                Text(text = "$score%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            NeonProgressBar(
                progress = score / 100f,
                modifier = Modifier.fillMaxWidth(),
                gradientStart = color.copy(alpha = 0.5f),
                gradientEnd = color
            )
        }
    }
}

fun sanitizeReport(raw: String): String {
    // If the backend returns a JSON or Markdown, clean any syntax tags
    var clean = raw.trim()
    if (clean.startsWith("```json")) {
        clean = clean.substring(7)
    } else if (clean.startsWith("```")) {
        clean = clean.substring(3)
    }
    if (clean.endsWith("```")) {
        clean = clean.substring(0, clean.length - 3)
    }
    return clean.replace("\"", "").replace("{", "").replace("}", "").trim()
}
