package com.skillbridge.ai.presentation.screens.skillgap

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.SkillGapViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SkillGapScreen(
    navController: NavController,
    viewModel: SkillGapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Skill Gap Analysis",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SkillBridgeBottomNavBar(
                currentRoute = com.skillbridge.ai.presentation.navigation.Screen.SkillGap.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                LoadingScreenComponent()
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${state.message}", color = NeonRed)
                }
            }
            is UiState.Empty, is UiState.Success -> {
                val data = (state as? UiState.Success)?.data ?: return@Scaffold

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(DeepNavy)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Neon Readiness Gauge ──────────────────────────────────
                    item {
                        GlassCard(
                            modifier = Modifier.padding(vertical = 8.dp),
                            glowColor = NeonCyan
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Target Job Readiness",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(20.dp))

                                // Pulsing neon glow effect
                                val infiniteTransition = rememberInfiniteTransition(label = "gauge_glow")
                                val glowAlpha by infiniteTransition.animateFloat(
                                    initialValue = 0.3f,
                                    targetValue = 0.7f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "gauge_glow_alpha"
                                )

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(180.dp)
                                ) {
                                    // Outer glow ring
                                    Canvas(modifier = Modifier.size(180.dp)) {
                                        drawArc(
                                            color = NeonCyan.copy(alpha = glowAlpha * 0.2f),
                                            startAngle = 140f,
                                            sweepAngle = 260f,
                                            useCenter = false,
                                            style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    // Track
                                    Canvas(modifier = Modifier.size(160.dp)) {
                                        drawArc(
                                            color = GlassWhite8,
                                            startAngle = 140f,
                                            sweepAngle = 260f,
                                            useCenter = false,
                                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        // Neon arc
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(ElectricBlue, NeonCyan, NeonGreen)
                                            ),
                                            startAngle = 140f,
                                            sweepAngle = (260f * data.readinessScore) / 100f,
                                            useCenter = false,
                                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${data.readinessScore}%",
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Ready",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Based on your parsed resume skills, your readiness score is ${data.readinessScore}% for your selected target role.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // ── Existing Competencies ────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Existing Competencies")
                    }
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            data.currentSkills.forEach { skill ->
                                SkillBridgeSkillChip(skillName = skill, type = "current")
                            }
                        }
                    }

                    // ── Missing Skills ────────────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Missing Skills (Critical Gaps)")
                    }
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            data.missingSkills.forEach { skill ->
                                SkillBridgeSkillChip(skillName = skill, type = "missing")
                            }
                        }
                    }

                    // ── Recommended Skills ────────────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Recommended to Stand Out")
                    }
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            data.recommendedSkills.forEach { skill ->
                                SkillBridgeSkillChip(skillName = skill, type = "recommended")
                            }
                        }
                    }

                    // ── Skill Verification Details ────────────────────────────
                    item {
                        SkillBridgeSectionHeader(title = "Skill Verification Details")
                    }

                    items(data.skillDetails) { detail ->
                        val statusColor = when (detail.status) {
                            "VERIFIED" -> NeonGreen
                            "PARTIALLY_VERIFIED" -> NeonAmber
                            "NEEDS_IMPROVEMENT" -> NeonRed
                            "DETECTED" -> ElectricBlue
                            else -> TextMuted
                        }
                        val statusText = when (detail.status) {
                            "VERIFIED" -> "Verified"
                            "PARTIALLY_VERIFIED" -> "Partially Verified"
                            "NEEDS_IMPROVEMENT" -> "Needs Improvement"
                            "DETECTED" -> "Resume Detected"
                            else -> "Target Skill"
                        }
                        val statusIcon = when (detail.status) {
                            "VERIFIED" -> Icons.Rounded.CheckCircle
                            "PARTIALLY_VERIFIED" -> Icons.Rounded.Warning
                            "NEEDS_IMPROVEMENT" -> Icons.Rounded.Error
                            "DETECTED" -> Icons.Rounded.Description
                            else -> Icons.Rounded.RadioButtonUnchecked
                        }

                        GlassCard(
                            modifier = Modifier.padding(vertical = 4.dp),
                            glowColor = statusColor
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = detail.skillName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.12f),
                                    modifier = Modifier.border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                ) {
                                    Text(
                                        text = statusText.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = statusColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (detail.score != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                NeonProgressBar(
                                    progress = detail.score / 100f,
                                    gradientStart = statusColor,
                                    gradientEnd = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Verification Score: ${detail.score}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
