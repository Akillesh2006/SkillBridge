package com.skillbridge.ai.presentation.screens.jobselection

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Work
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
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.GlassCard
import com.skillbridge.ai.presentation.components.LoadingScreenComponent
import com.skillbridge.ai.presentation.components.SkillBridgePrimaryButton
import com.skillbridge.ai.presentation.components.SkillBridgeSectionHeader
import com.skillbridge.ai.presentation.components.SkillBridgeTopAppBar
import com.skillbridge.ai.presentation.theme.*
import com.skillbridge.ai.presentation.viewmodel.JobSelectionViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JobSelectionScreen(
    navController: NavController,
    viewModel: JobSelectionViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val companyQuery by viewModel.companyQuery.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showConfirmationDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DeepNavy,
        topBar = {
            SkillBridgeTopAppBar(
                title = "Configure Target Career",
                onBackClick = { navController.popBackStack() }
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
                .padding(horizontal = 20.dp)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    LoadingScreenComponent()
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${state.message}", color = NeonRed, fontWeight = FontWeight.Bold)
                    }
                }
                is UiState.Empty, is UiState.Success -> {
                    val data = (state as? UiState.Success)?.data ?: return@Scaffold

                    if (data.isSavedSuccessfully) {
                        LaunchedEffect(Unit) {
                            Toast.makeText(context, "Career target saved & roadmap regenerated!", Toast.LENGTH_SHORT).show()
                            viewModel.resetSavedState()
                            navController.popBackStack()
                        }
                    }

                    if (showConfirmationDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmationDialog = false },
                            containerColor = SurfaceNavy,
                            title = {
                                Text(
                                    text = "Regenerate Roadmap?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            },
                            text = {
                                Text(
                                    text = "Changing your target career will regenerate your skill-gap analysis and learning roadmap.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showConfirmationDialog = false
                                        viewModel.saveTargetJobSelection()
                                    }
                                ) {
                                    Text("Confirm", color = ElectricBlue, fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmationDialog = false }) {
                                    Text("Cancel", color = TextMuted)
                                }
                            }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // 1. Target Job Search
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            SkillBridgeSectionHeader(title = "Target Job Role")
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.searchRoles(it) },
                                placeholder = { Text("Search or type custom job role...") },
                                leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = TextMuted) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = GlassWhite10,
                                    focusedLabelColor = TextPrimary,
                                    unfocusedLabelColor = TextMuted,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // Suggested / Custom Job Role Items
                        if (searchQuery.isNotEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectJobTitle(searchQuery) }
                                        .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                    color = GlassWhite4,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.Work, contentDescription = null, tint = ElectricBlue)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Use custom role: \"$searchQuery\"", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            items(data.searchResults.filter { it.contains(searchQuery, ignoreCase = true) }) { job ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectJobTitle(job) }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Work,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = job,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (data.selectedJobTitle == job) FontWeight.Bold else FontWeight.Normal,
                                        color = if (data.selectedJobTitle == job) ElectricBlue else TextPrimary
                                    )
                                }
                                HorizontalDivider(color = GlassWhite10)
                            }
                        } else {
                            item {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    data.popularJobs.forEach { job ->
                                        val isSelected = data.selectedJobTitle == job
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { viewModel.selectJobTitle(job) },
                                            label = { Text(job) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = ElectricBlue.copy(alpha = 0.2f),
                                                selectedLabelColor = ElectricBlue,
                                                containerColor = GlassWhite4,
                                                labelColor = TextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Target Company Search
                        item {
                            SkillBridgeSectionHeader(title = "Target Company (Optional)")
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = companyQuery,
                                onValueChange = { viewModel.searchCompanies(it) },
                                placeholder = { Text("Search or type custom company...") },
                                leadingIcon = { Icon(imageVector = Icons.Rounded.Business, contentDescription = null, tint = TextMuted) },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricBlue,
                                    unfocusedBorderColor = GlassWhite10,
                                    focusedLabelColor = TextPrimary,
                                    unfocusedLabelColor = TextMuted,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        if (companyQuery.isNotEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectCompany(companyQuery) }
                                        .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                    color = GlassWhite4,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.Business, contentDescription = null, tint = NeonGreen)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Use custom company: \"$companyQuery\"", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                item {
                                    val isAnySelected = data.selectedCompany.isBlank() || "Any company".equals(data.selectedCompany, ignoreCase = true)
                                    FilterChip(
                                        selected = isAnySelected,
                                        onClick = { viewModel.selectCompany("Any company") },
                                        label = { Text("Any Company") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = NeonGreen,
                                            containerColor = GlassWhite4,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                                items(data.popularCompanies) { company ->
                                    val isSelected = data.selectedCompany.equals(company, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectCompany(company) },
                                        label = { Text(company) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = NeonGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = NeonGreen,
                                            containerColor = GlassWhite4,
                                            labelColor = TextSecondary
                                        )
                                    )
                                }
                            }
                        }

                        // 3. Selection Summary Card
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Text(
                                        text = "TARGET CAREER PROFILE",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ElectricBlue
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${data.selectedJobTitle}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = TextPrimary
                                    )
                                    if (data.selectedCompany.isNotBlank() && !"Any company".equals(data.selectedCompany, ignoreCase = true)) {
                                        Text(
                                            text = "@ ${data.selectedCompany}",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp,
                                            color = NeonGreen
                                        )
                                    } else {
                                        Text(
                                            text = "@ Industry Standard Preference",
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = data.jobSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        // 4. Save Button
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SkillBridgePrimaryButton(
                                text = "Save Career Target",
                                onClick = {
                                    val changed = data.selectedJobTitle != data.initialJobTitle || data.selectedCompany != data.initialCompany
                                    if (changed && data.initialJobTitle.isNotBlank()) {
                                        showConfirmationDialog = true
                                    } else {
                                        viewModel.saveTargetJobSelection()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
