package com.skillbridge.ai.presentation.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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
import com.skillbridge.ai.presentation.viewmodel.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Progress Tracking",
                onBackClick = { navController.popBackStack() }
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
                    // Highlights Section: Streak, Tasks Completed
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Streak Card
                            GlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp),
                                glowColor = NeonAmber
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = "Streak",
                                        tint = NeonAmber,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${data.streakDays} Days",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeonAmber
                                        )
                                        Text(
                                            text = "Current Streak",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            // Tasks Completed Card
                            GlassCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp),
                                glowColor = NeonGreen
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "${data.tasksCompleted}/${data.totalTasks}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = NeonGreen
                                        )
                                        Text(
                                            text = "Tasks Completed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Circular Progress Gauge
                    item {
                        GlassCard(
                            glowColor = ElectricBlue
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Target Readiness Progress",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Your skills coverage has increased by 15% this week.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Canvas(modifier = Modifier.size(80.dp)) {
                                        drawArc(
                                            color = GlassWhite8,
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = ElectricBlue,
                                            startAngle = -90f,
                                            sweepAngle = 360f * (data.overallCompletion / 100f),
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = "${data.overallCompletion}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ElectricBlue,
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Weekly study hours custom Canvas graph
                    item {
                        SkillBridgeSectionHeader(title = "Study Hours (Weekly)")
                    }

                    item {
                        GlassCard(
                            glowColor = NeonCyan
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Total: 27.5 Hours",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "Mon - Sun",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Weekly bar chart Canvas
                                val barColor = NeonCyan
                                val labels = listOf("M", "T", "W", "T", "F", "S", "S")
                                val studyHours = data.weeklyStudyHours

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                ) {
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height
                                    val spacing = canvasWidth / (labels.size + 1)
                                    val barWidth = 20.dp.toPx()
                                    val maxHours = 8f // Set max bounds for relative scale

                                    studyHours.forEachIndexed { index, hours ->
                                        val x = spacing * (index + 1)
                                        val barHeight = (hours / maxHours) * (canvasHeight - 30.dp.toPx())
                                        val y = canvasHeight - 20.dp.toPx() - barHeight

                                        // Draw the rounded bar
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(NeonCyan, ElectricBlue.copy(alpha = 0.5f))
                                            ),
                                            topLeft = Offset(x - barWidth / 2, y),
                                            size = Size(barWidth, barHeight),
                                            cornerRadius = CornerRadius(12f, 12f)
                                        )
                                    }
                                }

                                // Day Labels below chart
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    labels.forEachIndexed { idx, label ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = label,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${studyHours[idx]}h",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
