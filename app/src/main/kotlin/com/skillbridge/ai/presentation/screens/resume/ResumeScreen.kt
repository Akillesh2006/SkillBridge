package com.skillbridge.ai.presentation.screens.resume

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.LoadingScreenComponent
import com.skillbridge.ai.presentation.components.SkillBridgePrimaryButton
import com.skillbridge.ai.presentation.components.SkillBridgeTopAppBar
import com.skillbridge.ai.presentation.navigation.Screen
import com.skillbridge.ai.presentation.viewmodel.ResumeViewModel

@Composable
fun ResumeScreen(
    navController: NavController,
    viewModel: ResumeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedFileBytes by remember { mutableStateOf<ByteArray?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            localError = null
            val fileName = getFileNameFromUri(context, uri) ?: "resume.pdf"
            val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"

            android.util.Log.d("ResumeUpload", "========== AUDIT: SELECTED PDF FILE ==========")
            android.util.Log.d("ResumeUpload", "Selected URI: $uri")
            android.util.Log.d("ResumeUpload", "File Name: $fileName")
            android.util.Log.d("ResumeUpload", "MIME Type: $mimeType")

            val isPdfExt = fileName.lowercase().endsWith(".pdf")
            val isPdfMime = mimeType.equals("application/pdf", ignoreCase = true) || mimeType.contains("pdf", ignoreCase = true)

            if (!isPdfExt && !isPdfMime) {
                localError = "Invalid file type. Only PDF files (.pdf) are allowed."
                android.util.Log.e("ResumeUpload", "Rejected non-PDF file: $fileName ($mimeType)")
                return@rememberLauncherForActivityResult
            }

            try {
                val bytes = context.contentResolver
                    .openInputStream(uri)
                    ?.use { it.readBytes() }

                if (bytes != null && bytes.isNotEmpty()) {
                    android.util.Log.d("ResumeUpload", "File Size: ${bytes.size} bytes")
                    android.util.Log.d("ResumeUpload", "Number of bytes actually read: ${bytes.size}")
                    android.util.Log.d("ResumeUpload", "Upload request filename: ${fileName.replace(" ", "_")}")
                    android.util.Log.d("ResumeUpload", "=============================================")

                    selectedFileBytes = bytes
                    viewModel.selectFile(fileName, bytes)
                } else {
                    localError = "Selected PDF file is empty (0 bytes)."
                    android.util.Log.e("ResumeUpload", "Failed: read 0 bytes from URI $uri")
                }
            } catch (e: Exception) {
                localError = "Failed to read PDF file: ${e.message}"
                android.util.Log.e("ResumeUpload", "Error reading PDF stream", e)
            }
        }
    }

    val data = (uiState as? UiState.Success)?.data
    val stateErrorMessage = (uiState as? UiState.Error)?.message ?: data?.errorMessage ?: localError

    if (data?.isAnalyzing == true) {
        FullScreenAnalysisLoading(
            stage = data.analysisStage,
            progress = data.analysisProgress
        )
    } else {
        Scaffold(
            topBar = {
                SkillBridgeTopAppBar(
                    title = "Resume Upload",
                    onBackClick = { navController.popBackStack() }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = "Upload Cloud",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Upload Resume",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload your CV in PDF format. SkillBridge AI will parse your experience and map you against target job requirements.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!stateErrorMessage.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = stateErrorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (data?.selectedFileName != null) {
                        FileCard(
                            fileName = data.selectedFileName,
                            fileSize = data.fileSize
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        SkillBridgePrimaryButton(
                            text = "Analyze Resume",
                            onClick = {
                                localError = null
                                viewModel.startAnalysis {
                                    navController.navigate(Screen.ResumeResult.route)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { pdfPickerLauncher.launch("application/pdf") }) {
                            Text("Select Different File")
                        }
                    } else {
                        Button(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Choose PDF File",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Only PDF files supported",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileCard(fileName: String, fileSize: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = "PDF icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = fileSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FullScreenAnalysisLoading(stage: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "analysis_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(80.dp),
                strokeWidth = 6.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Analyzing Resume",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "AI is extracting your skills and preparing recommendations...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            AnimatedContent(
                targetState = stage,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                },
                label = "stage_animation"
            ) { targetStage ->
                Text(
                    text = "✓ $targetStage",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

private fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}
