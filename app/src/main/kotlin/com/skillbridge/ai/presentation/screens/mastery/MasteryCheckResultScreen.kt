package com.skillbridge.ai.presentation.screens.mastery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.domain.model.Assessment
import com.skillbridge.ai.presentation.components.GlassCard
import com.skillbridge.ai.presentation.components.SkillBridgeTopAppBar
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.MasteryCheckViewModel

@Composable
fun MasteryCheckResultScreen(
    navController: NavController,
    assessmentId: Long,
    viewModel: MasteryCheckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiData = (uiState as? UiState.Success)?.data

    LaunchedEffect(assessmentId) {
        viewModel.loadMasteryCheckResult(assessmentId)
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "AI Evaluation Result",
                onBackClick = {
                    viewModel.clearActiveAssessment()
                    navController.popBackStack()
                }
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
            when (uiState) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = ElectricBlue, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("COMPUTING MASTERY METRICS...", style = MaterialTheme.typography.titleMedium, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Parsing category sub-scores...", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as UiState.Error).message ?: "Failed to load result",
                            color = NeonRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.loadMasteryCheckResult(assessmentId) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("Retry loading", color = TextPrimary)
                        }
                    }
                }
                is UiState.Success -> {
                    val assessment = uiData?.assessment
                    if (assessment != null) {
                        MasteryResultContent(
                            assessment = assessment,
                            onCloseClick = {
                                viewModel.clearActiveAssessment()
                                navController.popBackStack()
                            },
                            onRetestClick = {
                                navController.navigate("mastery_check/${assessment.questions.firstOrNull()?.id ?: 0L}?retestId=${assessment.id}") {
                                    popUpTo("roadmap") // Clean navigation backstack
                                }
                            }
                        )
                    }
                }
                else -> {
                    // Empty/fallback state
                }
            }
        }
    }
}

@Composable
fun MasteryResultContent(
    assessment: Assessment,
    onCloseClick: () -> Unit,
    onRetestClick: () -> Unit
) {
    val level = assessment.masteryLevel ?: "REINFORCE"
    val levelColor = when (level.uppercase()) {
        "MASTERED" -> NeonGreen
        "REINFORCE" -> NeonAmber
        else -> NeonRed
    }

    val icon = when (level.uppercase()) {
        "MASTERED" -> Icons.Rounded.CheckCircle
        "REINFORCE" -> Icons.Rounded.Warning
        else -> Icons.Rounded.ErrorOutline
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // 1. Overall Score Ring & Status Header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = level,
                        tint = levelColor,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = level.replace("_", " "),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Overall Score: ${assessment.score}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Sub-category scores metrics
            Text(
                text = "Performance Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ScoreBar(label = "Conceptual Understanding", score = assessment.conceptualScore ?: 0, color = NeonGreen)
                    ScoreBar(label = "Application", score = assessment.applicationScore ?: 0, color = ElectricBlue)
                    ScoreBar(label = "Problem Solving", score = assessment.problemSolvingScore ?: 0, color = NeonAmber)
                    ScoreBar(label = "Debugging", score = assessment.debuggingScore ?: 0, color = NeonRed)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. AI Mentor Feedback
            Text(
                text = "AI Mentor Feedback",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = assessment.aiFeedback ?: "Evaluate details below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Strengths & Weaknesses
            val strengths = parseJsonList(assessment.strengths)
            val weakConcepts = parseJsonList(assessment.weakConcepts)

            if (strengths.isNotEmpty()) {
                Text(
                    text = "Key Strengths",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        strengths.forEach { str ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(str, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (weakConcepts.isNotEmpty()) {
                Text(
                    text = "Focus Areas (Concepts to Review)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        weakConcepts.forEach { weak ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = NeonAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(weak, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 5. Recommended Reinforcement Exercises
            val practices = parseJsonList(assessment.recommendedPractice)
            if (practices.isNotEmpty()) {
                Text(
                    text = "Recommended Practice",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        practices.forEach { prac ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassWhite4,
                                modifier = Modifier.fillMaxWidth().border(0.5.dp, GlassWhite8, RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = prac,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // 6. Action buttons
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (assessment.score < 80) {
                    Button(
                        onClick = onRetestClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("RETAKE MASTERY", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Button(
                    onClick = onCloseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = if (assessment.score >= 80) ElectricBlue else GlassWhite10),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("BACK TO ROADMAP", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun ScoreBar(label: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Text("$score%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = score.toFloat() / 100f,
            color = color,
            trackColor = GlassWhite10,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
    }
}

fun parseJsonList(jsonStr: String?): List<String> {
    if (jsonStr.isNullOrBlank()) return emptyList()
    return try {
        jsonStr.trim()
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        emptyList()
    }
}
