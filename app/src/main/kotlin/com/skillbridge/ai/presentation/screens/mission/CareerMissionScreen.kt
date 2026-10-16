package com.skillbridge.ai.presentation.screens.mission

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.data.remote.dto.CareerMissionResponse
import com.skillbridge.ai.data.remote.dto.ProjectTaskItem
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.CareerMissionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerMissionScreen(
    navController: NavController,
    viewModel: CareerMissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Active Mission, 1: History
    var showGitHubDialog by remember { mutableStateOf(false) }
    var verificationStepText by remember { mutableStateOf("Analyzing your project...") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val mission = uiState.activeMission
    val result = uiState.resultDetails

    LaunchedEffect(uiState.isThinking) {
        if (uiState.isThinking) {
            verificationStepText = "Analyzing your project..."
            delay(900)
            verificationStepText = "AI is reviewing your repository against the project mission."
            delay(1200)
            verificationStepText = "Evaluating architecture, code quality & requirements..."
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "AI Career Project Mission",
                onBackClick = {
                    if (result != null) {
                        viewModel.clearResultDetails()
                    } else {
                        navController.popBackStack()
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.generateNewMission() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "New Project Mission",
                            tint = ElectricBlue
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when {
                uiState.isThinking -> {
                    SkillBridgeLoadingDialog(
                        show = true,
                        message = verificationStepText
                    )
                }

                uiState.isLoading && mission == null -> {
                    LoadingScreenComponent()
                }

                result != null -> {
                    MissionResultView(
                        result = result,
                        onBackToMission = {
                            viewModel.clearResultDetails()
                            selectedTab = 0
                        },
                        onOpenGitHub = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Unable to open URL.")
                                }
                            }
                        }
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Futuristic Tab Bar
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = GlassWhite4,
                            contentColor = ElectricBlue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, GlassWhite8, RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "Project Mission",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 0) ElectricBlue else TextMuted
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "Mission History (${uiState.history.size})",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == 1) ElectricBlue else TextMuted
                                    )
                                }
                            )
                        }

                        if (selectedTab == 0) {
                            val activeMission = mission ?: CareerMissionViewModel.createDefaultMockMission()
                            ActiveMissionView(
                                mission = activeMission,
                                tasks = if (uiState.tasks.isNotEmpty()) uiState.tasks else activeMission.tasks,
                                isSubmitted = uiState.isSubmitted,
                                lastSubmittedUrl = uiState.lastSubmittedUrl ?: activeMission.githubUrl,
                                lastSubmittedScore = uiState.lastSubmittedScore ?: activeMission.score,
                                lastSubmittedTime = uiState.lastSubmittedTime ?: (if (uiState.isSubmitted) "Submitted just now" else null),
                                onTaskToggle = { taskId -> viewModel.toggleTaskStatus(taskId) },
                                onOpenSubmitDialog = { showGitHubDialog = true },
                                onViewEvaluation = { viewModel.viewEvaluationDetails() },
                                onGenerateNew = { viewModel.generateNewMission() }
                            )
                        } else {
                            MissionHistoryList(
                                history = uiState.history,
                                onGenerateNew = {
                                    selectedTab = 0
                                    viewModel.generateNewMission()
                                },
                                onSelectHistoryItem = { item ->
                                    viewModel.selectHistoryItem(item)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // GitHub Submission Dialog
    if (showGitHubDialog) {
        GitHubSubmissionDialog(
            initialUrl = uiState.lastSubmittedUrl ?: "https://github.com/Akillesh2006/SkillBridge",
            onDismiss = { showGitHubDialog = false },
            onSubmit = { url ->
                showGitHubDialog = false
                viewModel.submitMissionResponse(url)
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveMissionView(
    mission: CareerMissionResponse,
    tasks: List<ProjectTaskItem>,
    isSubmitted: Boolean,
    lastSubmittedUrl: String?,
    lastSubmittedScore: Int?,
    lastSubmittedTime: String?,
    onTaskToggle: (Int) -> Unit,
    onOpenSubmitDialog: () -> Unit,
    onViewEvaluation: () -> Unit,
    onGenerateNew: () -> Unit
) {
    val completedTasks = tasks.count { it.status.equals("COMPLETED", ignoreCase = true) }
    val totalTasks = if (tasks.isNotEmpty()) tasks.size else 6
    val progressPercent = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Mission Header Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassWhite8, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = GlassWhite6
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSubmitted) "✓ PROJECT MISSION COMPLETED" else "🎯 AI CAREER PROJECT MISSION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isSubmitted) NeonGreen else ElectricBlue
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSubmitted) NeonGreen.copy(alpha = 0.15f) else NeonAmber.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSubmitted) NeonGreen.copy(alpha = 0.3f) else NeonAmber.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            text = if (isSubmitted) "SUBMITTED" else "IN PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSubmitted) NeonGreen else NeonAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = mission.title ?: "Build a Smart Career Readiness Tracker",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Target: ${mission.targetJob ?: "Android Developer"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricBlue
                    )
                    Text(
                        text = "Difficulty: ${mission.difficulty ?: "Intermediate"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "Duration: 7 Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // 2. WHY THIS PROJECT? Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = ElectricBlue.copy(alpha = 0.04f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "WHY THIS PROJECT?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricBlue
                )
                Text(
                    text = mission.whyThisMission
                        ?: "Build an Android application that helps students track their career readiness by managing skills, learning progress, and project activities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }

        // 3. SKILLS TO DEMONSTRATE
        val skills = if (mission.skillsEvaluated.isNotEmpty()) {
            mission.skillsEvaluated
        } else {
            listOf("Kotlin", "Jetpack Compose", "REST API", "MVVM", "MySQL", "Git/GitHub")
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassWhite8, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = GlassWhite4
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SKILLS TO DEMONSTRATE",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    skills.forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElectricBlue.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, ElectricBlue.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "• $skill",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. PROJECT REQUIREMENTS
        val requirements = if (mission.projectRequirements.isNotEmpty()) {
            mission.projectRequirements
        } else {
            listOf(
                "User profile screen",
                "Skill tracking",
                "Progress dashboard",
                "REST API integration",
                "Local data persistence",
                "GitHub repository",
                "Clean Android architecture"
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassWhite8, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = GlassWhite4
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PROJECT REQUIREMENTS",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )

                requirements.forEachIndexed { idx, req ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ElectricBlue.copy(alpha = 0.15f),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricBlue
                                )
                            }
                        }
                        Text(
                            text = req,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 5. PROJECT TASKS SECTION
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            color = GlassWhite4
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Progress Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PROJECT TASKS",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = NeonCyan
                        )
                        Text(
                            text = "Tap task to update status",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$completedTasks / $totalTasks Tasks Completed",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (completedTasks == totalTasks) NeonGreen else TextPrimary
                        )
                        Text(
                            text = "$progressPercent% Project Progress",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricBlue
                        )
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .border(0.5.dp, GlassWhite8, RoundedCornerShape(4.dp)),
                    color = if (completedTasks == totalTasks) NeonGreen else ElectricBlue,
                    trackColor = SpaceBlack
                )

                HorizontalDivider(color = GlassWhite8, thickness = 0.5.dp)

                // Task Items List
                tasks.forEach { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { onTaskToggle(task.id) }
                    )
                }
            }
        }

        // 6. GITHUB SUBMISSION SECTION / SUBMITTED STATE
        if (isSubmitted) {
            // Already Submitted Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = NeonGreen.copy(alpha = 0.06f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "PROJECT SUBMITTED ✓",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = NeonGreen
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonGreen.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${lastSubmittedScore ?: 82} / 100",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = NeonGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!lastSubmittedUrl.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = lastSubmittedUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = ElectricBlue,
                                maxLines = 1
                            )
                        }
                    }

                    if (!lastSubmittedTime.isNullOrBlank()) {
                        Text(
                            text = lastSubmittedTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onViewEvaluation,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VIEW AI EVALUATION",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenSubmitDialog,
                            modifier = Modifier.height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassWhite15)
                        ) {
                            Text("Re-submit", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        } else {
            // Not Submitted Yet - Submission Prompt
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = ElectricBlue.copy(alpha = 0.05f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudUpload,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "GitHub Submission",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Submit your public repository for AI evaluation and score breakdown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Button(
                        onClick = onOpenSubmitDialog,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Code, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SUBMIT GITHUB PROJECT",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 7. Footer: Generate Different Project
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onGenerateNew) {
                Text("Generate Different Project ↻", color = ElectricBlue, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TaskItemRow(
    task: ProjectTaskItem,
    onToggle: () -> Unit
) {
    val statusUpper = task.status.uppercase()
    val isCompleted = statusUpper == "COMPLETED"
    val isInProgress = statusUpper == "IN_PROGRESS"

    val statusColor = when {
        isCompleted -> NeonGreen
        isInProgress -> NeonAmber
        else -> TextDim
    }

    val statusBg = when {
        isCompleted -> NeonGreen.copy(alpha = 0.12f)
        isInProgress -> NeonAmber.copy(alpha = 0.12f)
        else -> GlassWhite4
    }

    val statusText = when {
        isCompleted -> "COMPLETED"
        isInProgress -> "IN PROGRESS"
        else -> "NOT STARTED"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isCompleted) NeonGreen.copy(alpha = 0.25f) else if (isInProgress) NeonAmber.copy(alpha = 0.25f) else GlassWhite8,
                RoundedCornerShape(12.dp)
            )
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        color = if (isCompleted) NeonGreen.copy(alpha = 0.03f) else GlassWhite4
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Task Number or Checkmark Circle
            Surface(
                shape = CircleShape,
                color = statusBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    } else if (isInProgress) {
                        Icon(
                            imageVector = Icons.Rounded.Pending,
                            contentDescription = null,
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = "${task.id}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                    }
                }
            }

            // Title and Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Task ${task.id}: ${task.title}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isCompleted || isInProgress) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isCompleted) TextPrimary else if (isInProgress) BrightBlue else TextSecondary,
                    lineHeight = 18.sp
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        lineHeight = 14.sp
                    )
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusBg,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun GitHubSubmissionDialog(
    initialUrl: String,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var urlText by remember { mutableStateOf(initialUrl) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(1.dp, ElectricBlue.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = DarkNavy
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = ElectricBlue.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Code,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = "Submit GitHub Repository",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Paste the public GitHub repository URL of your completed project.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                // Input Field
                OutlinedTextField(
                    value = urlText,
                    onValueChange = {
                        urlText = it
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("GitHub Repository URL", color = TextMuted) },
                    placeholder = {
                        Text(
                            text = "https://github.com/username/project-name",
                            color = TextDim,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Link,
                            contentDescription = null,
                            tint = ElectricBlue
                        )
                    },
                    isError = validationError != null,
                    supportingText = {
                        if (validationError != null) {
                            Text(text = validationError!!, color = NeonRed, style = MaterialTheme.typography.labelSmall)
                        } else {
                            Text("Example: https://github.com/Akillesh2006/SkillBridge", color = TextDim, style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = GlassWhite15,
                        focusedContainerColor = SpaceBlack,
                        unfocusedContainerColor = SpaceBlack
                    )
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = TextMuted, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val clean = urlText.trim()
                            if (clean.isBlank()) {
                                validationError = "Please enter a repository URL."
                            } else if (!clean.contains("github.com", ignoreCase = true) && !clean.startsWith("http")) {
                                validationError = "Please enter a valid GitHub URL."
                            } else {
                                onSubmit(clean)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SUBMIT PROJECT", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionResultView(
    result: CareerMissionResponse,
    onBackToMission: () -> Unit,
    onOpenGitHub: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val score = result.score ?: 82
        val scoreColor = if (score >= 80) NeonGreen else if (score >= 60) NeonAmber else NeonRed

        Spacer(modifier = Modifier.height(4.dp))

        Icon(
            imageVector = Icons.Rounded.EmojiEvents,
            contentDescription = null,
            tint = scoreColor,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = "PROJECT EVALUATION",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = TextPrimary
        )

        // Hero Score Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, scoreColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = GlassWhite6
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "$score / 100",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = scoreColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (score >= 80) "Overall Score • 82 / 100" else if (score >= 60) "Good Foundation" else "Needs Improvement",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = scoreColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                HorizontalDivider(color = GlassWhite8, thickness = 0.5.dp)

                // Sub-scores breakdown
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScoreRow("Mission Completion", "${result.requirementsScore ?: 88}%", ElectricBlue)
                    ScoreRow("Code Quality", "${result.codeQualityScore ?: 80}%", NeonGreen)
                    ScoreRow("Architecture", "${result.architectureScore ?: 85}%", NeonCyan)
                    ScoreRow("Requirements", "${result.requirementsScore ?: 84}%", ElectricBlue)
                    ScoreRow("Documentation", "${result.documentationScore ?: 76}%", NeonCyan)
                    ScoreRow("Career Relevance", "${result.careerRelevanceScore ?: 90}%", NeonGreen)
                }
            }
        }

        // GitHub Repository URL Link
        val url = if (!result.githubUrl.isNullOrBlank()) result.githubUrl else if (!result.studentResponse.isNullOrBlank()) result.studentResponse else "https://github.com/Akillesh2006/SkillBridge"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GlassWhite8, RoundedCornerShape(12.dp))
                .clickable { onOpenGitHub(url) },
            shape = RoundedCornerShape(12.dp),
            color = GlassWhite4
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Code, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = ElectricBlue,
                        maxLines = 1
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        // STRENGTHS
        val strengthsList = if (result.strengths.isNotEmpty()) {
            result.strengths
        } else {
            listOf(
                "Clean Android project structure",
                "Good use of Jetpack Compose",
                "REST API integration",
                "Appropriate MVVM architecture",
                "GitHub repository available"
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = NeonGreen.copy(alpha = 0.04f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✓ Strengths", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NeonGreen)
                strengthsList.forEach { item ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✓", color = NeonGreen, fontWeight = FontWeight.Bold)
                        Text(item, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        // MISSING / IMPROVEMENTS
        val needsImprovementList = if (result.needsImprovement.isNotEmpty()) {
            result.needsImprovement
        } else {
            listOf(
                "Add more unit tests",
                "Improve README documentation",
                "Add better error handling",
                "Increase persistence coverage"
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonAmber.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = NeonAmber.copy(alpha = 0.04f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("• Missing / Improvements", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = NeonAmber)
                needsImprovementList.forEach { item ->
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("•", color = NeonAmber, fontWeight = FontWeight.Bold)
                        Text(item, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        // AI FEEDBACK
        val feedbackText = if (!result.feedback.isNullOrBlank()) {
            result.feedback
        } else if (!result.recommendedNextStep.isNullOrBlank()) {
            result.recommendedNextStep
        } else {
            "Your project demonstrates a strong understanding of Android application development and follows a reasonable MVVM structure. The core mission requirements are implemented, but testing and documentation can be improved."
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = ElectricBlue.copy(alpha = 0.04f)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("🤖 AI Feedback", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = ElectricBlue)
                Text(feedbackText, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 20.sp)
            }
        }

        // Action Button: [ BACK TO MISSION ]
        Button(
            onClick = onBackToMission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("BACK TO MISSION", fontWeight = FontWeight.Bold, color = TextPrimary, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ScoreRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun MissionHistoryList(
    history: List<CareerMissionResponse>,
    onGenerateNew: () -> Unit,
    onSelectHistoryItem: (CareerMissionResponse) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("No project missions completed yet.", color = TextMuted)
                Button(
                    onClick = onGenerateNew,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start First Project Mission", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(history) { item ->
                val score = item.score ?: 82
                val color = if (score >= 80) NeonGreen else if (score >= 60) NeonAmber else NeonRed

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { onSelectHistoryItem(item) },
                    shape = RoundedCornerShape(14.dp),
                    color = GlassWhite4
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title ?: "Build a Smart Career Readiness Tracker",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = color.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "$score / 100",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = item.targetJob ?: "Android Developer",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricBlue
                        )

                        if (!item.githubUrl.isNullOrBlank()) {
                            Text(
                                text = "GitHub: ${item.githubUrl}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = NeonGreen
                            )
                            Text(
                                text = "View evaluation →",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
