package com.skillbridge.ai.presentation.screens.profile

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.ProfileViewModel

// Global theme state for simple MVP UI presentation toggle
object AppThemeState {
    var isDarkTheme = mutableStateOf(false)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "User Profile",
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SkillBridgeBottomNavBar(
                currentRoute = com.skillbridge.ai.presentation.navigation.Screen.Profile.route,
                onNavigate = { route -> navController.navigate(route) }
            )
        }
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
                val profile = data.profile

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(DeepNavy)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Initials Avatar
                        val initials = profile.username.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }
                            .joinToString("")
                            .take(2)
                            .uppercase()

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(GlassWhite8)
                                .border(2.dp, Brush.linearGradient(listOf(ElectricBlue, NeonCyan)), CircleShape)
                        ) {
                            Text(
                                text = if (initials.isNotEmpty()) initials else "SB",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // User Details Display
                        Text(
                            text = profile.username.ifBlank { "Akillesh K P" },
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = profile.email.ifBlank { "akillesh@citi.edu.in" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = profile.university.ifBlank { "Chennai Institute of Technology" },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricBlue,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Career Goal
                        SkillBridgeSectionHeader(title = "Career Goal")
                        GlassCard(glowColor = ElectricBlue) {
                            ProfileDetailRow(icon = Icons.Rounded.Work, label = "Target Job Role", value = profile.targetJob.ifBlank { "Android Developer" })
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = GlassWhite8)
                            ProfileDetailRow(icon = Icons.Rounded.Business, label = "Target Company", value = profile.targetCompany.ifBlank { "Google" })
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Skills
                        SkillBridgeSectionHeader(title = "Skills")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val skills = listOf("Kotlin", "Android SDK", "Jetpack Compose", "Java", "Spring Boot", "REST APIs", "Docker", "Git")
                            skills.forEach { skill ->
                                SkillBridgeSkillChip(skillName = skill, type = "current")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Projects
                        SkillBridgeSectionHeader(title = "Projects")
                        GlassCard(glowColor = NeonViolet) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                ProjectItem(title = "SkillBridge AI Career Guide", desc = "AI-driven resume parsing and skill gap mapping.")
                                HorizontalDivider(color = GlassWhite8)
                                ProjectItem(title = "E-Commerce Microservices", desc = "Scalable backend with Spring Boot.")
                                HorizontalDivider(color = GlassWhite8)
                                ProjectItem(title = "Personal Portfolio", desc = "Modern responsive website showcasing Android work.")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Certifications
                        SkillBridgeSectionHeader(title = "Certifications")
                        GlassCard(glowColor = NeonCyan) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CertificationItem(title = "Google Associate Android Developer")
                                HorizontalDivider(color = GlassWhite8)
                                CertificationItem(title = "AWS Certified Cloud Practitioner")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Learning Statistics
                        SkillBridgeSectionHeader(title = "Learning Statistics")
                        GlassCard(glowColor = NeonAmber) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(text = "27.5h", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                                    Text(text = "Study Time", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(text = "5 Days", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                                    Text(text = "Current Streak", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text(text = "12/18", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, color = NeonAmber)
                                    Text(text = "Tasks Done", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Career Readiness
                        SkillBridgeSectionHeader(title = "Career Readiness")
                        GlassCard(glowColor = NeonCyan) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Overall Readiness Score",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonGreen.copy(alpha = 0.15f),
                                        modifier = Modifier.border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    ) {
                                        Text(
                                            text = "High Potential 🚀",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = NeonGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(70.dp)) {
                                    Canvas(modifier = Modifier.size(70.dp)) {
                                        drawArc(
                                            color = GlassWhite8,
                                            startAngle = -90f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                        drawArc(
                                            color = NeonCyan,
                                            startAngle = -90f,
                                            sweepAngle = 360f * 0.82f,
                                            useCenter = false,
                                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                    Text(
                                        text = "82%",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 14.sp),
                                        color = NeonCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // App Settings
                        SkillBridgeSectionHeader(title = "App Settings")

                        GlassCard(glowColor = ElectricBlue) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.DarkMode,
                                        contentDescription = null,
                                        tint = ElectricBlue
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Dark Mode",
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextPrimary
                                    )
                                }
                                Switch(
                                    checked = AppThemeState.isDarkTheme.value,
                                    onCheckedChange = { isDark ->
                                        AppThemeState.isDarkTheme.value = isDark
                                        viewModel.toggleDarkMode(isDark)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = TextPrimary,
                                        checkedTrackColor = ElectricBlue,
                                        uncheckedThumbColor = TextMuted,
                                        uncheckedTrackColor = GlassWhite10,
                                        uncheckedBorderColor = GlassWhite15
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(30.dp))

                        // Edit Profile and Logout buttons
                        SkillBridgePrimaryButton(
                            text = "Edit Profile Details",
                            onClick = { showEditDialog = true },
                            icon = Icons.Rounded.Edit,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.logout {
                                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                                        popUpTo(Screen.Profile.route) { inclusive = true }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NeonRed,
                                containerColor = Color.Transparent
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(NeonRed.copy(alpha = 0.6f), NeonRed.copy(alpha = 0.2f))
                                )
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Rounded.Logout, contentDescription = null, tint = NeonRed)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Log Out",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NeonRed
                                )
                            }
                        }
                    }
                }

                // Edit Profile Popup Modal Dialog
                if (showEditDialog) {
                    var editUsername by remember { mutableStateOf(profile.username.ifBlank { "Akillesh K P" }) }
                    var editUniversity by remember { mutableStateOf(profile.university.ifBlank { "Chennai Institute of Technology" }) }
                    var editJob by remember { mutableStateOf(profile.targetJob.ifBlank { "Android Developer" }) }
                    var editCompany by remember { mutableStateOf(profile.targetCompany.ifBlank { "Google" }) }

                    AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                        modifier = Modifier
                            .padding(24.dp)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = CardNavy,
                        title = {
                            Text(
                                text = "Edit Profile Info",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SkillBridgeTextField(
                                    value = editUsername,
                                    onValueChange = { editUsername = it },
                                    label = "Name"
                                )
                                SkillBridgeTextField(
                                    value = editUniversity,
                                    onValueChange = { editUniversity = it },
                                    label = "University"
                                )
                                SkillBridgeTextField(
                                    value = editJob,
                                    onValueChange = { editJob = it },
                                    label = "Target Job Role"
                                )
                                SkillBridgeTextField(
                                    value = editCompany,
                                    onValueChange = { editCompany = it },
                                    label = "Target Company"
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(editUsername, editUniversity, editJob, editCompany)
                                    showEditDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricBlue,
                                    contentColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Changes", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showEditDialog = false },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = TextMuted
                                )
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = ElectricBlue.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp).border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
    }
}

@Composable
fun ProjectItem(title: String, desc: String) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

@Composable
fun CertificationItem(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.Verified,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary
        )
    }
}
