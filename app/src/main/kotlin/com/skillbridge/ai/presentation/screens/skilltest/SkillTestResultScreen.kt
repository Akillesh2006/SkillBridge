package com.skillbridge.ai.presentation.screens.skilltest

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.domain.model.Assessment
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.AssessmentViewModel

@Composable
fun SkillTestResultScreen(
    navController: NavController,
    assessmentId: Long,
    viewModel: AssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiData = (uiState as? UiState.Success)?.data
    val context = LocalContext.current

    LaunchedEffect(assessmentId) {
        viewModel.loadAssessmentResult(assessmentId)
    }

    LaunchedEffect(viewModel) {
        viewModel.errorFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Verification Results",
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
            val assessment = uiData?.assessment

            if (assessment != null && uiState is UiState.Success) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Overall Score GlassCard
                    GlassCard(glowColor = NeonCyan) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "VERIFICATION COMPLETE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(120.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { assessment.score / 100f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = NeonCyan,
                                    strokeWidth = 8.dp,
                                    trackColor = GlassWhite8,
                                    strokeCap = StrokeCap.Round
                                )
                                Text(
                                    text = "${assessment.score}%",
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp
                                    ),
                                    color = TextPrimary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val verifiedCount = assessment.skillBreakdown.count { it.status == "VERIFIED" }
                            val partialCount = assessment.skillBreakdown.count { it.status == "PARTIALLY_VERIFIED" }
                            val needsCount = assessment.skillBreakdown.count { it.status == "NEEDS_IMPROVEMENT" }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatusBadge(label = "Verified", count = verifiedCount, color = NeonGreen)
                                StatusBadge(label = "Partial", count = partialCount, color = NeonAmber)
                                StatusBadge(label = "Review", count = needsCount, color = NeonRed)
                            }
                        }
                    }

                    // 2. Skill Breakdown list
                    if (assessment.skillBreakdown.isNotEmpty()) {
                        SkillBridgeSectionHeader(title = "Skill Verification Breakdown")
                        
                        assessment.skillBreakdown.forEach { item ->
                            GlassCard(
                                glowColor = when (item.status) {
                                    "VERIFIED" -> NeonGreen
                                    "PARTIALLY_VERIFIED" -> NeonAmber
                                    else -> NeonRed
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.skillName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    
                                    val statusLabel = when (item.status) {
                                        "VERIFIED" -> "✓ VERIFIED"
                                        "PARTIALLY_VERIFIED" -> "⚠ PARTIAL"
                                        else -> "! NEEDS WORK"
                                    }
                                    val statusColor = when (item.status) {
                                        "VERIFIED" -> NeonGreen
                                        "PARTIALLY_VERIFIED" -> NeonAmber
                                        else -> NeonRed
                                    }
                                    
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                NeonProgressBar(
                                    progress = item.percentage / 100f,
                                    gradientStart = when (item.status) {
                                        "VERIFIED" -> NeonGreen
                                        "PARTIALLY_VERIFIED" -> NeonAmber
                                        else -> NeonRed
                                    },
                                    gradientEnd = when (item.status) {
                                        "VERIFIED" -> NeonCyan
                                        "PARTIALLY_VERIFIED" -> NeonAmber.copy(alpha = 0.6f)
                                        else -> NeonRed.copy(alpha = 0.6f)
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.correctQuestions}/${item.totalQuestions} Questions Correct",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "${item.percentage}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // 3. Incorrect Review Explanations
                    val incorrectQuestions = assessment.questions.filter { it.isCorrect == false }
                    if (incorrectQuestions.isNotEmpty()) {
                        SkillBridgeSectionHeader(title = "AI Explanations & Corrections")
                        
                        incorrectQuestions.forEach { q ->
                            GlassCard(glowColor = NeonRed) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Cancel,
                                        contentDescription = null,
                                        tint = NeonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${q.skillName} • ${q.difficulty}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Text(
                                    text = q.questionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val studentAnsText = q.studentAnswer?.let { q.options.getOrNull(it) } ?: "Not answered"
                                val correctAnsText = q.correctAnswer?.let { q.options.getOrNull(it) } ?: "N/A"

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonRed.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Your Answer: $studentAnsText",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonRed
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonGreen.copy(alpha = 0.08f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, NeonGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Correct Answer: $correctAnsText",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NeonGreen
                                    )
                                }

                                if (!q.explanation.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Why: " + q.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // 4. Retake Test Button
                    SkillBridgePrimaryButton(
                        text = "Retake Assessment",
                        onClick = {
                            viewModel.startTest()
                            navController.navigate(Screen.SkillTest.route) {
                                popUpTo(Screen.SkillTest.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    )
                }
            } else if (uiState is UiState.Error) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Failed to load verification results.", color = NeonRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        SkillBridgePrimaryButton(
                            text = "Back to Safety",
                            onClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    SkillBridgeLoadingDialog(show = uiState is UiState.Loading, message = "Loading results...")
}

@Composable
fun StatusBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
