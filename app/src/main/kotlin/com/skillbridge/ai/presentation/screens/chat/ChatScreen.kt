package com.skillbridge.ai.presentation.screens.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.ChatMessage
import com.skillbridge.ai.presentation.viewmodel.CopilotViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: CopilotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val context = uiState.copilotContext
    val dailyMission = uiState.dailyMission

    val quickActions = listOf(
        "What should I learn today?",
        "Explain my skill gap",
        "What did I miss?",
        "Give me today's tasks",
        "Analyze my progress",
        "Prepare me for an interview"
    )

    fun onSend(text: String) {
        if (text.isBlank() || uiState.isThinking) return
        if (text.contains("interview", ignoreCase = true) || text.contains("test me", ignoreCase = true)) {
            navController.navigate("mock_interview")
            return
        }
        if (text.contains("challenge", ignoreCase = true)) {
            navController.navigate("daily_challenge")
            return
        }
        viewModel.sendMessage(text)
        inputText = ""
        coroutineScope.launch {
            listState.animateScrollToItem(uiState.messages.size + 1) // +1 for the header item
        }
    }

    Scaffold(
        topBar = {
            SkillBridgeTopAppBar(
                title = "AI Career Copilot",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SkillBridgeBottomNavBar(
                currentRoute = Screen.Chat.route,
                onNavigate = { route -> navController.navigate(route) }
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
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Top Warning Alert if exists
                if (!uiState.errorMessage.isNullOrBlank()) {
                    Surface(
                        color = NeonRed.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonRed.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text("Dismiss", color = NeonCyan)
                            }
                        }
                    }
                }

                // Chat Scroll Area containing header card, chips, and bubble messages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // 1. Futuristic Copilot Summary Header Card
                    item {
                        GlassCard(
                            glowColor = NeonCyan,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "AI CAREER COPILOT",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = NeonCyan
                                        )
                                        Text(
                                            text = "Good day, ${context?.name ?: "Student"}.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Rounded.RocketLaunch,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Current Focus",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = dailyMission?.focusSkill ?: "Kotlin & REST APIs",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Progress",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                        Text(
                                            text = "${context?.roadmapProgress ?: 40}% Completed",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = NeonCyan
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                NeonProgressBar(
                                    progress = (context?.roadmapProgress ?: 40) / 100f,
                                    modifier = Modifier.fillMaxWidth(),
                                    gradientEnd = NeonCyan
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Priority Goal: ${dailyMission?.tasks?.firstOrNull() ?: "Review intermediate concepts"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                SkillBridgePrimaryButton(
                                    text = "Continue Learning Mission",
                                    onClick = { navController.navigate(Screen.Roadmap.route) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // 2. Chat Bubble Stream
                    items(uiState.messages) { message ->
                        ChatBubble(message)
                    }

                    if (uiState.isThinking) {
                        item {
                            ThinkingBubble()
                        }
                    }
                }

                // 3. Quick Action Chips Carousel
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickActions) { action ->
                        SuggestionChipItem(action) { onSend(action) }
                    }
                }

                // 4. Input Text Bar
                ChatInput(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = { onSend(inputText) },
                    enabled = !uiState.isThinking
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!message.isUser) {
                Icon(
                    imageVector = Icons.Rounded.SmartToy,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GlassWhite8)
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), CircleShape)
                        .padding(6.dp),
                    tint = NeonCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Neon Glow Glass bubble
            Surface(
                color = if (message.isUser) ElectricBlue.copy(alpha = 0.2f) else GlassWhite6,
                shape = shape,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            if (message.isUser) listOf(NeonCyan, ElectricBlue) else listOf(GlassWhite12, GlassWhite6)
                        ),
                        shape = shape
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!message.isUser) {
                        Text(
                            text = "✨ SkillBridge Copilot",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ThinkingBubble() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.SmartToy,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(GlassWhite8)
                .padding(6.dp),
            tint = NeonCyan
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            color = GlassWhite6,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(1.dp, GlassWhite12, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = NeonCyan
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Copilot is analyzing context...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SuggestionChipItem(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
        color = GlassWhite6
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = NeonCyan
        )
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Surface(
        color = DeepNavy,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().border(1.dp, GlassWhite8, RoundedCornerShape(0.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Career Copilot anything...", color = TextMuted) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                enabled = enabled,
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = GlassWhite15,
                    focusedContainerColor = SpaceBlack,
                    unfocusedContainerColor = SpaceBlack
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = enabled && value.isNotBlank(),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (enabled && value.isNotBlank()) NeonCyan else GlassWhite12),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send, 
                    contentDescription = "Send",
                    tint = if (enabled && value.isNotBlank()) Color.Black else Color.Gray
                )
            }
        }
    }
}
