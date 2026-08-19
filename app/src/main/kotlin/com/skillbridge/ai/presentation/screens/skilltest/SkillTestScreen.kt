package com.skillbridge.ai.presentation.screens.skilltest

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.domain.model.AssessmentHistoryItem
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.AssessmentViewModel
import kotlinx.coroutines.launch

@Composable
fun SkillTestScreen(
    navController: NavController,
    viewModel: AssessmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiData = (uiState as? UiState.Success)?.data
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.errorFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Skill Verification",
                onBackClick = {
                    if (uiData?.assessment != null) {
                        // Confirm exit dialog could go here; for now, clear and pop
                        viewModel.clearActiveAssessment()
                    }
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

            if (assessment == null) {
                // Assessment Start & History List Screen
                StartAssessmentContent(
                    history = uiData?.history ?: emptyList(),
                    onStartClick = { viewModel.startTest() },
                    onHistoryItemClick = { assessmentId ->
                        navController.navigate("skill_test_result/$assessmentId")
                    }
                )
            } else {
                // Active Assessment Questions Screen
                val questions = assessment.questions
                val currentIndex = uiData.currentQuestionIndex

                if (questions.isNotEmpty() && currentIndex < questions.size) {
                    val question = questions[currentIndex]
                    val selectedOption = uiData.selectedOption

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // 1. Progress indicators
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Question ${currentIndex + 1} of ${questions.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (question.difficulty.uppercase()) {
                                        "EASY" -> NeonGreen.copy(alpha = 0.15f)
                                        "HARD" -> NeonRed.copy(alpha = 0.15f)
                                        else -> ElectricBlue.copy(alpha = 0.15f)
                                    },
                                    modifier = Modifier.border(
                                        width = 1.dp,
                                        color = when (question.difficulty.uppercase()) {
                                            "EASY" -> NeonGreen.copy(alpha = 0.4f)
                                            "HARD" -> NeonRed.copy(alpha = 0.4f)
                                            else -> ElectricBlue.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                ) {
                                    Text(
                                        text = question.difficulty,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (question.difficulty.uppercase()) {
                                            "EASY" -> NeonGreen
                                            "HARD" -> NeonRed
                                            else -> ElectricBlue
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            NeonProgressBar(progress = (currentIndex + 1).toFloat() / questions.size)

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Question details GlassCard
                            GlassCard(glowColor = ElectricBlue) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = question.skillName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NeonCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = question.questionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    lineHeight = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. Render 4 Options
                            question.options.forEachIndexed { index, optionText ->
                                val isSelected = selectedOption == index
                                val optionColor = if (isSelected) NeonCyan else GlassWhite15

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.horizontalGradient(
                                                if (isSelected) {
                                                    listOf(NeonCyan, ElectricBlue)
                                                } else {
                                                    listOf(GlassWhite10, GlassWhite6)
                                                }
                                            ),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            viewModel.selectOption(question.id, index)
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ElectricBlue.copy(alpha = 0.12f) else GlassWhite4
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.selectOption(question.id, index) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = NeonCyan,
                                                unselectedColor = TextMuted
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = optionText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isSelected) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Navigation Buttons (PREV / NEXT-SUBMIT)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (currentIndex > 0) {
                                SkillBridgeOutlinedButton(
                                    text = "Back",
                                    onClick = { viewModel.previousQuestion() },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            val isLast = currentIndex == questions.size - 1
                            val buttonText = if (isLast) "Submit Test" else "Next"
                            val isEnabled = selectedOption != null

                            SkillBridgePrimaryButton(
                                text = buttonText,
                                onClick = {
                                    if (isLast) {
                                        viewModel.submitTest { assessmentId ->
                                            navController.navigate("skill_test_result/$assessmentId") {
                                                popUpTo(Screen.SkillTest.route) { inclusive = true }
                                            }
                                        }
                                    } else {
                                        viewModel.nextQuestion()
                                    }
                                },
                                enabled = isEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    SkillBridgeLoadingDialog(
        show = uiState is UiState.Loading || uiData?.isSubmitting == true,
        message = if (uiData?.isSubmitting == true) "Analyzing skills..." else "Generating test..."
    )
}

@Composable
fun StartAssessmentContent(
    history: List<AssessmentHistoryItem>,
    onStartClick: () -> Unit,
    onHistoryItemClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Card
        GlassCard(glowColor = ElectricBlue) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = "AI Skill Verification",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Verify your skills through an AI-powered knowledge assessment.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This adaptive assessment checks your practical understanding of the technical skills extracted from your resume. Your custom roadmap and readiness score will improve dynamically based on your verified levels.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            SkillBridgePrimaryButton(
                text = "Take Skill Test",
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Assessment History Section
        if (history.isNotEmpty()) {
            SkillBridgeSectionHeader(title = "Assessment History")

            history.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .clickable { onHistoryItemClick(item.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = GlassWhite4
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Verification Score: ${item.score}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.score >= 80) NeonGreen else if (item.score >= 50) NeonAmber else NeonRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Date: " + item.createdDate.substringBefore("T"),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "View Details",
                            tint = TextMuted
                        )
                    }
                }
            }
        }
    }
}
