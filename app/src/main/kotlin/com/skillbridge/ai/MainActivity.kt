package com.skillbridge.ai

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.*
import com.skillbridge.ai.data.worker.DailyReminderWorker
import com.skillbridge.ai.presentation.navigation.SkillBridgeNavGraph
import com.skillbridge.ai.presentation.theme.SkillBridgeTheme
import com.skillbridge.ai.presentation.screens.profile.AppThemeState
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

/**
 * Single-Activity host for the Compose application.
 *
 * Responsibilities:
 * - Enable edge-to-edge rendering.
 * - Wrap content in [SkillBridgeTheme].
 * - Delegate navigation to [SkillBridgeNavGraph].
 *
 * Contains **no** business logic.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Request POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
        }

        // 2. Schedule periodic missed-task checker worker
        val reminderRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            1, TimeUnit.HOURS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            reminderRequest
        )

        enableEdgeToEdge()
        setContent {
            val isDark = AppThemeState.isDarkTheme.value
            SkillBridgeTheme(darkTheme = isDark) {
                SkillBridgeNavGraph()
            }
        }
    }
}
