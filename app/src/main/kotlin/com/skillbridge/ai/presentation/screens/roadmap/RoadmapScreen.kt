package com.skillbridge.ai.presentation.screens.roadmap

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
                onBackClick = { navController.popBackStack() }
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
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // ── Overall Progress Header ──────────────────────────────
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SkillBridgeProgressCard(
                            title = "Roadmap Completion Progress",
                            percentage = data.completionPercentage,
                            subtitle = "Complete all weeks to become fully prepared for your target job!"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Timeline Items ────────────────────────────────────────
                    itemsIndexed(data.roadmapWeeks) { index, week ->
                        var isExpanded by remember { mutableStateOf(false) }
                        val isLast = index == data.roadmapWeeks.lastIndex

                        RoadmapTimelineItem(
                            week = week,
                            isExpanded = isExpanded,
                            isLast = isLast,
                            onHeaderClick = { isExpanded = !isExpanded },
                            onToggleTaskComplete = { taskId, completed ->
                                viewModel.toggleTaskCompleted(taskId, completed)
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

@Composable
fun RoadmapTimelineItem(
    week: RoadmapWeek,
    isExpanded: Boolean,
    isLast: Boolean,
    onHeaderClick: () -> Unit,
    onToggleTaskComplete: (Long, Boolean) -> Unit,
    onStartMasteryCheck: (Long) -> Unit,
    onViewMasteryResult: (Long) -> Unit,
    onRetestMastery: (Long) -> Unit,
    onResourceClick: (LearningResource) -> Unit
) {
    val nodeColor = if (week.completed) NeonGreen else ElectricBlue
    val nodeGlowColor = if (week.completed) NeonGreen.copy(alpha = 0.3f) else ElectricBlue.copy(alpha = 0.2f)

    Row(modifier = Modifier.fillMaxWidth()) {
        // ── Timeline Track (left side) ───────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Glowing node
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(nodeGlowColor)
                    .border(2.dp, nodeColor, CircleShape)
            ) {
                if (week.completed) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Done",
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "${week.weekNumber}",
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Connecting line
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(if (isExpanded) 560.dp else 60.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(nodeColor.copy(alpha = 0.5f), GlassWhite8)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // ── Week Card (right side) ───────────────────────────────────
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .border(
                    1.dp,
                    if (week.completed) NeonGreen.copy(alpha = 0.2f) else GlassWhite8,
                    RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            color = if (week.completed) NeonGreen.copy(alpha = 0.06f) else GlassWhite6
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onHeaderClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Week ${week.weekNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = nodeColor
                        )
                        Text(
                            text = week.topic,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Show Less" else "Show More",
                        tint = TextMuted
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        // Description
                        Text(
                            text = week.details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Learning Resources:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Resource cards
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.resources.forEach { resource ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, GlassWhite8, RoundedCornerShape(10.dp))
                                        .clickable { onResourceClick(resource) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = GlassWhite4
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (resource.type.lowercase() == "video") Icons.Rounded.PlayCircle else Icons.Rounded.Link,
                                            contentDescription = resource.type,
                                            tint = ElectricBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = resource.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
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

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Learning Tasks & Skill Mastery:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .border(1.dp, GlassWhite8, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    color = GlassWhite4
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.completed,
                                                onCheckedChange = { onToggleTaskComplete(task.id, !task.completed) },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = ElectricBlue,
                                                    uncheckedColor = TextMuted
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = task.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextMuted
                                                )
                                            }
                                        }

                                        if (task.status != "PENDING" && task.status != "IN_PROGRESS" && task.status != "COMPLETED") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = GlassWhite8, thickness = 0.5.dp)
                                            Spacer(modifier = Modifier.height(8.dp))

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
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                            modifier = Modifier.height(28.dp),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text("START MASTERY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                                                        }
                                                    }
                                                    "MASTERED" -> {
                                                        task.lastAssessmentId?.let { aid ->
                                                            Button(
                                                                onClick = { onViewMasteryResult(aid) },
                                                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.2f)),
                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                                modifier = Modifier.height(28.dp),
                                                                shape = RoundedCornerShape(6.dp)
                                                            ) {
                                                                Text("REVIEW RESULT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NeonGreen)
                                                            }
                                                        }
                                                    }
                                                    "REINFORCEMENT_REQUIRED", "NEEDS_IMPROVEMENT" -> {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            task.lastAssessmentId?.let { aid ->
                                                                Button(
                                                                    onClick = { onViewMasteryResult(aid) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = taskNodeColor.copy(alpha = 0.2f)),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                                                                    modifier = Modifier.height(28.dp),
                                                                    shape = RoundedCornerShape(6.dp)
                                                                ) {
                                                                    Text("REVIEW", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = taskNodeColor)
                                                                }
                                                                Button(
                                                                    onClick = { onRetestMastery(aid) },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
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
                    }
                }
            }
        }
    }
}
