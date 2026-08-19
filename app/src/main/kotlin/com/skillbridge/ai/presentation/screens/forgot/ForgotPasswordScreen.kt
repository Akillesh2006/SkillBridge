package com.skillbridge.ai.presentation.screens.forgot

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.skillbridge.ai.core.common.UiState
import com.skillbridge.ai.presentation.components.*
import com.skillbridge.ai.presentation.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val email by viewModel.email.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val uiData = (uiState as? UiState.Success)?.data

    Scaffold(
        topBar = {
            SkillBridgeTopAppBar(
                title = "Forgot Password",
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "🔑",
                    style = MaterialTheme.typography.displayMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Reset Your Password",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Enter your registered email address below. We'll send you instructions to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                SkillBridgeTextField(
                    value = email,
                    onValueChange = { viewModel.setEmail(it) },
                    label = "Email Address",
                    leadingIcon = Icons.Default.Email,
                    isError = uiData?.emailError != null,
                    errorMessage = uiData?.emailError
                )

                Spacer(modifier = Modifier.height(24.dp))

                SkillBridgePrimaryButton(
                    text = "Send Reset Link",
                    onClick = {
                        viewModel.resetPassword {
                            Toast.makeText(context, "Password reset link sent to your email!", Toast.LENGTH_LONG).show()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    SkillBridgeLoadingDialog(show = uiState is UiState.Loading, message = "Sending request...")
}
