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
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Star
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
import com.skillbridge.ai.presentation.viewmodel.InterviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockInterviewScreen(
    navController: NavController,
    viewModel: InterviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var responseText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            SkillBridgeTopAppBar(
                title = "AI Mock Interview",
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
                // Show thinking overlay spinner
                uiState.isThinking -> {
                    SkillBridgeLoadingDialog(show = true, message = "Analyzing answer and adapting next question...")
                }
                
                // Show result details from history click
                uiState.resultDetails != null -> {
                    val report = uiState.resultDetails!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Interview Report Summary",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                        Text(
                            text = "Target Role: ${report.targetRole}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        
                        GlassCard(glowColor = NeonCyan) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Overall Score", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${report.overallScore ?: 0}%",
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                    color = NeonCyan
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = report.feedback ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        Text(
                            text = "Question Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        
                        report.questions.forEach { q ->
                            GlassCard(glowColor = GlassWhite8) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Q${q.questionOrder}: ${q.questionText}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    Text("Your Answer: ${q.studentAnswer ?: ""}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Score: ${q.score}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = NeonCyan)
                                        Text("Difficulty: ${q.difficulty}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    }
                                    if (q.feedback != null) {
                                        Text("Feedback: ${q.feedback}", style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                                    }
                                }
                            }
                        }
                        
                        SkillBridgePrimaryButton(
                            text = "CLOSE REPORT",
                            onClick = { viewModel.clearResultDetails() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Show completed active interview scorecard
                uiState.completed -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interview Completed!",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = NeonGreen
                        )
                        
                        GlassCard(glowColor = NeonGreen) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Overall Performance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${uiState.overallScore}%",
                                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                    color = NeonGreen
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.feedback,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        SkillBridgePrimaryButton(
                            text = "GO BACK TO COPILOT",
                            onClick = {
                                viewModel.startInterview() // Reset
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Show active question taking session
                uiState.activeInterviewId != null -> {
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
                                text = "Question ${uiState.questionOrder} of 5",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Badge(
                                containerColor = when (uiState.difficulty) {
                                    "HARD" -> NeonRed.copy(alpha = 0.2f)
                                    "MEDIUM" -> NeonCyan.copy(alpha = 0.2f)
                                    else -> NeonGreen.copy(alpha = 0.2f)
                                },
                                modifier = Modifier.border(1.dp, NeonCyan, RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = uiState.difficulty,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    color = Color.White
                                )
                            }
                        }
                        
                        NeonProgressBar(
                            progress = uiState.questionOrder / 5f,
                            modifier = Modifier.fillMaxWidth(),
                            gradientEnd = NeonCyan
                        )
                        
                        GlassCard(glowColor = NeonCyan) {
                            Text(
                                text = uiState.questionText,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                modifier = Modifier.padding(16.dp),
                                color = Color.White
                            )
                        }
                        
                        OutlinedTextField(
                            value = responseText,
                            onValueChange = { responseText = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            placeholder = { Text("Type your technical answer here...", color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassWhite15,
                                focusedContainerColor = SpaceBlack,
                                unfocusedContainerColor = SpaceBlack
                            )
                        )
                        
                        SkillBridgePrimaryButton(
                            text = "SUBMIT RESPONSE",
                            onClick = {
                                viewModel.submitAnswer(responseText)
                                responseText = ""
                            },
                            enabled = responseText.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Startup Landing screen (Welcome & History list)
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        GlassCard(glowColor = NeonCyan) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Ready to test your readiness?",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Start an adaptive technical mock interview for your target role. The questions adapt dynamically to your answers.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SkillBridgePrimaryButton(
                                    text = "START NEW MOCK INTERVIEW",
                                    onClick = { viewModel.startInterview() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        
                        Text(
                            text = "Previous Mock Interviews",
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
                                Text("No interviews completed yet.", color = TextMuted)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.history) { item ->
                                    GlassCard(
                                        glowColor = GlassWhite8,
                                        modifier = Modifier.clickable { viewModel.loadResultDetails(item.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = item.targetRole,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Rounded.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = item.createdAt.substring(0, 10), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                }
                                            }
                                            
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Rounded.Star, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${item.overallScore ?: 0}%",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = NeonCyan
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
}
