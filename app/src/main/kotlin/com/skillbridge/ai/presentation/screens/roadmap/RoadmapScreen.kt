package com.skillbridge.ai.presentation.screens.roadmap

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.domain.model.LearningResource
import com.skillbridge.ai.domain.model.RoadmapWeek
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.RoadmapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapScreen(
    navController: NavController,
    viewModel: RoadmapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel) {
        viewModel.errorFlow.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Learning Roadmap",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadRoadmap(forceRegenerate = true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Roadmap",
                            tint = ElectricBlue
                        )
                    }
                }
            )
        },
        bottomBar = {
            SkillBridgeBottomNavBar(
                currentRoute = com.skillbridge.ai.presentation.navigation.Screen.Roadmap.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = ElectricBlue)
                        Text(
                            text = "Loading your personalized roadmap...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Error: ${state.message}", color = NeonRed)
                        Button(
                            onClick = { viewModel.loadRoadmap(forceRegenerate = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Empty, is UiState.Success -> {
                val data = (state as? UiState.Success)?.data ?: return@Scaffold

                if (data.isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            CircularProgressIndicator(color = ElectricBlue, strokeWidth = 3.dp)
                            Text(
                                text = data.generationStage.ifBlank { "Creating your personalized roadmap..." },
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Gemini is analyzing your skills and target role (${data.targetRole}) to design a multi-module curriculum.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(DeepNavy)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
                    ) {
                        // ── Overall Progress Header Card ──────────────────────────
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, GlassWhite8, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                color = GlassWhite6
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "TARGET CAREER",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    letterSpacing = 1.2.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = ElectricBlue
                                            )
                                            Text(
                                                text = data.targetRole,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                color = TextPrimary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ElectricBlue.copy(alpha = 0.15f),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "${data.completionPercentage}%",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = ElectricBlue
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Progress Bar
                                    LinearProgressIndicator(
                                        progress = { data.completionPercentage / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (data.completionPercentage >= 100) NeonGreen else ElectricBlue,
                                        trackColor = GlassWhite8,
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val completedModuleCount = data.roadmapWeeks.count { it.completed }
                                    val totalModuleCount = data.roadmapWeeks.size
                                    val allTasks = data.roadmapWeeks.flatMap { it.tasks }
                                    val completedTaskCount = allTasks.count { it.completed }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "$completedModuleCount of $totalModuleCount Modules Complete",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "$completedTaskCount/${allTasks.size} Tasks Done",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // ── Modules List ──────────────────────────────────────────
                        itemsIndexed(data.roadmapWeeks) { index, week ->
                            var isExpanded by remember { mutableStateOf(index == 0) }

                            RoadmapModuleCard(
                                week = week,
                                isExpanded = isExpanded,
                                onHeaderClick = { isExpanded = !isExpanded },
                                onToggleTaskComplete = { taskId, completed ->
                                    viewModel.toggleTaskCompleted(taskId, completed)
                                },
                                onToggleModuleComplete = { weekNum, completed ->
                                    viewModel.toggleModuleCompleted(weekNum, completed)
                                },
                                onStartMasteryCheck = { taskId ->
                                    navController.navigate("mastery_check/$taskId")
                                },
                                onViewMasteryResult = { assessmentId ->
                                    navController.navigate("mastery_check_result/$assessmentId")
                                },
                                onRetestMastery = { oldAssessmentId ->
                                    val task = week.tasks.find { it.lastAssessmentId == oldAssessmentId }
                                    val taskId = task?.id ?: 0L
                                    navController.navigate("mastery_check/$taskId?retestId=$oldAssessmentId")
                                },
                                onResourceClick = { resource ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resource.url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Resource unavailable.")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoadmapModuleCard(
    week: RoadmapWeek,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onToggleTaskComplete: (Long, Boolean) -> Unit,
    onToggleModuleComplete: (Int, Boolean) -> Unit,
    onStartMasteryCheck: (Long) -> Unit,
    onViewMasteryResult: (Long) -> Unit,
    onRetestMastery: (Long) -> Unit,
    onResourceClick: (LearningResource) -> Unit
) {
    val totalTasks = week.tasks.size
    val completedTasks = week.tasks.count { it.completed }
    val moduleProgressPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else (if (week.completed) 100 else 0)
    val isAllCompleted = week.completed || (totalTasks > 0 && completedTasks == totalTasks)

    val statusBadgeText = when {
        isAllCompleted -> "✓ Completed"
        moduleProgressPercent > 0 -> "Progress $moduleProgressPercent%"
        else -> "Not Started"
    }

    val statusBadgeColor = when {
        isAllCompleted -> NeonGreen
        moduleProgressPercent > 0 -> ElectricBlue
        else -> TextMuted
    }

    val borderColor = if (isAllCompleted) NeonGreen.copy(alpha = 0.3f) else GlassWhite8
    val cardBackground = if (isAllCompleted) NeonGreen.copy(alpha = 0.04f) else GlassWhite6

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = cardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row (clickable to expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Module Index Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isAllCompleted) NeonGreen.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.15f))
                        .border(1.5.dp, if (isAllCompleted) NeonGreen else ElectricBlue, CircleShape)
                ) {
                    if (isAllCompleted) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Completed",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${week.weekNumber}",
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "MODULE ${week.weekNumber}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = ElectricBlue
                        )
                        Text(
                            text = "•  $statusBadgeText",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = statusBadgeColor
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = week.topic,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = TextMuted
                )
            }

            // Expanded Module Details
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = GlassWhite8, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    if (week.details.isNotBlank()) {
                        Text(
                            text = week.details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Skills Tags (Derived from topic & tasks)
                    val skills = remember(week) {
                        val set = mutableSetOf<String>()
                        if (week.topic.isNotBlank()) set.add(week.topic)
                        week.tasks.forEach { task ->
                            if (!task.skillName.isNullOrBlank()) set.add(task.skillName)
                        }
                        set.toList().take(4)
                    }

                    if (skills.isNotEmpty()) {
                        Text(
                            text = "Skills Covered:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            skills.forEach { skill ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ElectricBlue.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, ElectricBlue.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = skill,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricBlue
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Tasks Section
                    Text(
                        text = "Module Tasks & Checkpoints:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        week.tasks.forEach { task ->
                            val taskNodeColor = when (task.status) {
                                "MASTERED" -> NeonGreen
                                "REINFORCEMENT_REQUIRED" -> NeonAmber
                                "NEEDS_IMPROVEMENT" -> NeonRed
                                "MASTERY_PENDING" -> ElectricBlue
                                else -> TextSecondary
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (task.completed) NeonGreen.copy(alpha = 0.2f) else GlassWhite8, RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = if (task.completed) NeonGreen.copy(alpha = 0.03f) else GlassWhite4
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.completed,
                                            onCheckedChange = { onToggleTaskComplete(task.id, !task.completed) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = NeonGreen,
                                                uncheckedColor = TextMuted,
                                                checkmarkColor = DeepNavy
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (task.completed) TextMuted else TextPrimary
                                                )
                                            )
                                            if (task.description.isNotBlank()) {
                                                Text(
                                                    text = task.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextMuted
                                                )
                                            }
                                        }
                                    }

                                    // Mastery assessment actions if applicable
                                    if (!task.status.isNullOrBlank() && task.status != "PENDING" && task.status != "IN_PROGRESS" && task.status != "COMPLETED") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        HorizontalDivider(color = GlassWhite8, thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Status: ${task.status.replace("_", " ")}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = taskNodeColor
                                            )

                                            when (task.status) {
                                                "MASTERY_PENDING" -> {
                                                    Button(
                                                        onClick = { onStartMasteryCheck(task.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(28.dp),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("ASSESS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                    }
                                                }
                                                "MASTERED" -> {
                                                    task.lastAssessmentId?.let { aid ->
                                                        Button(
                                                            onClick = { onViewMasteryResult(aid) },
                                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.2f)),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text("RESULT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NeonGreen)
                                                        }
                                                    }
                                                }
                                                "REINFORCEMENT_REQUIRED", "NEEDS_IMPROVEMENT" -> {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        task.lastAssessmentId?.let { aid ->
                                                            Button(
                                                                onClick = { onViewMasteryResult(aid) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = taskNodeColor.copy(alpha = 0.2f)),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text("REVIEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = taskNodeColor)
                                                            }
                                                            Button(
                                                                onClick = { onRetestMastery(aid) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text("RETEST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
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

                    // Resources Section
                    if (week.resources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Learning Resources:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            week.resources.forEach { resource ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GlassWhite8, RoundedCornerShape(8.dp))
                                        .clickable { onResourceClick(resource) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = GlassWhite4
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (resource.type.lowercase() == "video") Icons.Rounded.PlayCircle else Icons.Rounded.Link,
                                            contentDescription = resource.type,
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = resource.title,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${resource.type} • ${resource.duration}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Manual Module Completion Button
                    Button(
                        onClick = { onToggleModuleComplete(week.weekNumber, !isAllCompleted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAllCompleted) NeonGreen.copy(alpha = 0.2f) else ElectricBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = if (isAllCompleted) androidx.compose.foundation.BorderStroke(1.dp, NeonGreen) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isAllCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MODULE COMPLETED (TAP TO UNDO)",
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            } else {
                                Text(
                                    text = "MARK MODULE AS COMPLETED",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
