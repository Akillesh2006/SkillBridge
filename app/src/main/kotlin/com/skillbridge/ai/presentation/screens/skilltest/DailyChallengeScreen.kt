package com.skillbridge.ai.presentation.screens.skilltest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.HighlightOff
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
import com.skillbridge.ai.presentation.viewmodel.ChallengeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    navController: NavController,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var responseText by remember { mutableStateOf("") }
    var selectedOptionIndex by remember { mutableStateOf(-1) }

    val challenge = uiState.activeChallenge
    val result = uiState.resultDetails

    Scaffold(
        topBar = {
            SkillBridgeTopAppBar(
                title = "AI Daily Challenge",
                onBackClick = {
                    viewModel.clearResultDetails()
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
            when {
                // Loading spinner
                uiState.isThinking -> {
                    SkillBridgeLoadingDialog(show = true, message = "Evaluating daily challenge response...")
                }

                // Show completed challenge feedback
                result != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isCorrect = result.correct == true
                        Icon(
                            imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                            contentDescription = null,
                            tint = if (isCorrect) NeonGreen else NeonRed,
                            modifier = Modifier.size(72.dp)
                        )
                        
                        Text(
                            text = if (isCorrect) "Challenge Solved!" else "Incorrect Solution",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isCorrect) NeonGreen else NeonRed
                        )

                        GlassCard(glowColor = if (isCorrect) NeonGreen else NeonRed) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Correct Solution / Expected:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(result.correctAnswer ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                
                                HorizontalDivider(color = GlassWhite8)
                                
                                Text("Gemini Explanation:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(result.explanation ?: "", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                
                                HorizontalDivider(color = GlassWhite8)
                                
                                Text("Improvement Suggestion:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(result.improvementSuggestion ?: "", style = MaterialTheme.typography.bodyMedium, color = NeonCyan)
                            }
                        }

                        SkillBridgePrimaryButton(
                            text = "DONE",
                            onClick = { viewModel.clearResultDetails() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Show active challenge taking session
                challenge != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Skill: ${challenge.skillName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Badge(
                                containerColor = ElectricBlue.copy(alpha = 0.2f),
                                modifier = Modifier.border(1.dp, NeonCyan, RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = challenge.challengeType,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White
                                )
                            }
                        }
                        
                        GlassCard(glowColor = NeonCyan) {
                            Text(
                                text = challenge.questionText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp),
                                color = Color.White
                            )
                        }

                        if ("MCQ".equals(challenge.challengeType, ignoreCase = true)) {
                            val optionsList = challenge.options.split(",")
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                optionsList.forEachIndexed { index, option ->
                                    val isSelected = selectedOptionIndex == index
                                    GlassCard(
                                        glowColor = if (isSelected) NeonCyan else GlassWhite8,
                                        modifier = Modifier.clickable { selectedOptionIndex = index }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedOptionIndex = index },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = NeonCyan,
                                                    unselectedColor = Color.White.copy(alpha = 0.5f)
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = option, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = responseText,
                                onValueChange = { responseText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                placeholder = { Text("Type code or query response here...", color = TextMuted) },
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GlassWhite15,
                                    focusedContainerColor = SpaceBlack,
                                    unfocusedContainerColor = SpaceBlack
                                )
                            )
                        }

                        SkillBridgePrimaryButton(
                            text = "SUBMIT SOLUTION",
                            onClick = {
                                val answer = if ("MCQ".equals(challenge.challengeType, ignoreCase = true)) {
                                    challenge.options.split(",").getOrNull(selectedOptionIndex) ?: ""
                                } else {
                                    responseText
                                }
                                viewModel.submitAnswer(answer)
                                responseText = ""
                                selectedOptionIndex = -1
                            },
                            enabled = if ("MCQ".equals(challenge.challengeType, ignoreCase = true)) selectedOptionIndex != -1 else responseText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Startup landing screen
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        GlassCard(glowColor = NeonCyan) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Ready for Today's Challenge?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Enhance your weakest skill by completing a dynamic conceptual or coding challenge generated just for you.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SkillBridgePrimaryButton(
                                    text = "GENERATE TODAY'S CHALLENGE",
                                    onClick = { viewModel.generateChallenge() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        Text(
                            text = "Completed Challenges",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        
                        if (uiState.history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No challenges completed yet.", color = TextMuted)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.history) { item ->
                                    GlassCard(glowColor = GlassWhite8) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = item.skillName,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "Type: ${item.challengeType}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                            }
                                            Icon(
                                                imageVector = if (item.correct == true) Icons.Rounded.CheckCircle else Icons.Rounded.HighlightOff,
                                                contentDescription = null,
                                                tint = if (item.correct == true) NeonGreen else NeonRed,
                                                modifier = Modifier.size(24.dp)
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
