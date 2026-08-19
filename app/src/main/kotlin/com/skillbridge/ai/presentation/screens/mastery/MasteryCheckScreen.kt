package com.skillbridge.ai.presentation.screens.mastery

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.draw.clip
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.GlassCard
import com.skillbridge.ai.presentation.components.NeonProgressBar
import com.skillbridge.ai.presentation.components.SkillBridgeTopAppBar
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.MasteryCheckViewModel

@Composable
fun MasteryCheckScreen(
    navController: NavController,
    taskId: Long,
    retestId: Long? = null,
    viewModel: MasteryCheckViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiData = (uiState as? UiState.Success)?.data
    val context = LocalContext.current

    LaunchedEffect(taskId, retestId) {
        if (retestId != null) {
            viewModel.retestMasteryCheck(retestId)
        } else {
            viewModel.startMasteryCheck(taskId)
        }
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
                title = "AI Mastery Check",
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
                        Text("GENERATING AI EVALUATION...", style = MaterialTheme.typography.titleMedium, color = ElectricBlue, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gemini is curating concept-specific questions...", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = (uiState as UiState.Error).message ?: "Failed to generate check",
                            color = NeonRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = {
                                if (retestId != null) {
                                    viewModel.retestMasteryCheck(retestId)
                                } else {
                                    viewModel.startMasteryCheck(taskId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("Retry generation", color = TextPrimary)
                        }
                    }
                }
                is UiState.Success -> {
                    val assessment = uiData?.assessment
                    if (assessment != null && assessment.questions.isNotEmpty()) {
                        val questions = assessment.questions
                        val currentIndex = uiData.currentQuestionIndex
                        val question = questions[currentIndex]
                        val selectedOption = uiData.selectedOption

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                // Question Header & Category Indicators
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
                                        color = when (question.questionType?.uppercase()) {
                                            "APPLICATION" -> NeonAmber.copy(alpha = 0.15f)
                                            "DEBUGGING" -> NeonRed.copy(alpha = 0.15f)
                                            "SCENARIO" -> ElectricBlue.copy(alpha = 0.15f)
                                            else -> NeonGreen.copy(alpha = 0.15f) // Conceptual
                                        },
                                        modifier = Modifier.border(
                                            width = 1.dp,
                                            color = when (question.questionType?.uppercase()) {
                                                "APPLICATION" -> NeonAmber
                                                "DEBUGGING" -> NeonRed
                                                "SCENARIO" -> ElectricBlue
                                                else -> NeonGreen
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    ) {
                                        Text(
                                            text = question.questionType ?: "CONCEPTUAL",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = when (question.questionType?.uppercase()) {
                                                "APPLICATION" -> NeonAmber
                                                "DEBUGGING" -> NeonRed
                                                "SCENARIO" -> ElectricBlue
                                                else -> NeonGreen
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                NeonProgressBar(progress = (currentIndex + 1).toFloat() / questions.size)
                                Spacer(modifier = Modifier.height(20.dp))

                                // Question Box
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        question.concept?.let {
                                            Text(
                                                text = "Concept: $it",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ElectricBlue,
                                                modifier = Modifier.padding(bottom = 6.dp)
                                            )
                                        }
                                        Text(
                                            text = question.questionText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextPrimary,
                                            lineHeight = 24.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Options Box
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    question.options.forEachIndexed { optIndex, option ->
                                        val isSelected = selectedOption == optIndex
                                        val optBorder = if (isSelected) ElectricBlue else GlassWhite10
                                        val optBg = if (isSelected) ElectricBlue.copy(alpha = 0.08f) else GlassWhite4

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, optBorder, RoundedCornerShape(12.dp))
                                                .clickable { viewModel.selectOption(question.id, optIndex) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = optBg
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(if (isSelected) ElectricBlue else GlassWhite10)
                                                        .border(1.dp, if (isSelected) ElectricBlue else TextMuted, RoundedCornerShape(6.dp))
                                                ) {
                                                    Text(
                                                        text = ('A'.code + optIndex).toChar().toString(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) TextPrimary else TextMuted,
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = option,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Navigation Controls Row
                            Column(modifier = Modifier.padding(top = 28.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { viewModel.previousQuestion() },
                                        enabled = currentIndex > 0,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GlassWhite10,
                                            disabledContainerColor = GlassWhite4
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("PREVIOUS", color = if (currentIndex > 0) TextPrimary else TextMuted)
                                    }

                                    if (currentIndex < questions.size - 1) {
                                        Button(
                                            onClick = { viewModel.nextQuestion() },
                                            enabled = selectedOption != null,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ElectricBlue,
                                                disabledContainerColor = GlassWhite10
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("NEXT", color = if (selectedOption != null) TextPrimary else TextMuted)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                viewModel.submitMasteryCheck { assessmentId ->
                                                    navController.navigate("mastery_check_result/$assessmentId") {
                                                        popUpTo("roadmap") // Clean navigation backstack
                                                    }
                                                }
                                            },
                                            enabled = selectedOption != null && !uiData.isSubmitting,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = NeonGreen,
                                                disabledContainerColor = GlassWhite10
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            if (uiData.isSubmitting) {
                                                CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(18.dp))
                                            } else {
                                                Text("SUBMIT EVALUATION", color = SpaceBlack, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Empty/fallback state
                }
            }
        }
    }
}
