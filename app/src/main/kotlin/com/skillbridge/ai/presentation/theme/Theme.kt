package com.skillbridge.ai.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Light Color Scheme ─────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary = Brand40,
    onPrimary = Neutral100,
    primaryContainer = Brand90,
    onPrimaryContainer = Brand10,

    secondary = Secondary40,
    onSecondary = Neutral100,
    secondaryContainer = Secondary90,
    onSecondaryContainer = Secondary10,

    tertiary = Tertiary40,
    onTertiary = Neutral100,
    tertiaryContainer = Tertiary90,
    onTertiaryContainer = Tertiary10,

    error = Error40,
    onError = Neutral100,
    errorContainer = Error90,
    onErrorContainer = Error10,

    background = Neutral99,
    onBackground = Neutral10,

    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = NeutralVariant90,
    onSurfaceVariant = NeutralVariant40,

    outline = NeutralVariant50,
    outlineVariant = NeutralVariant80,

    scrim = Neutral0,
)

// ── Futuristic Dark Color Scheme ──────────────────────────────────────────────

private val FuturisticDarkScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = DeepNavy,
    primaryContainer = GradientBlueStart,
    onPrimaryContainer = BrightBlue,

    secondary = NeonCyan,
    onSecondary = DeepNavy,
    secondaryContainer = GradientCyanStart,
    onSecondaryContainer = PaleCyan,

    tertiary = NeonViolet,
    onTertiary = DeepNavy,
    tertiaryContainer = GradientVioletStart,
    onTertiaryContainer = SoftViolet,

    error = NeonRed,
    onError = DeepNavy,
    errorContainer = DarkRedBg,
    onErrorContainer = SoftRed,

    background = DeepNavy,
    onBackground = TextPrimary,

    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceNavy,
    onSurfaceVariant = TextSecondary,

    outline = TextDim,
    outlineVariant = BorderSubtle,

    scrim = SpaceBlack,
)

/**
 * SkillBridge AI Material 3 theme composable.
 *
 * @param darkTheme Whether to use the dark color scheme.
 *   Defaults to the system setting via [isSystemInDarkTheme].
 * @param dynamicColor Whether to use Android 12+ dynamic color extraction.
 *   Falls back to the brand color scheme on older devices.
 * @param content The composable subtree to theme.
 */
@Composable
fun SkillBridgeTheme(
    darkTheme: Boolean = true, // Force dark theme for futuristic look
    dynamicColor: Boolean = false, // Disable dynamic color for consistent branding
    content: @Composable () -> Unit,
) {
    // Always use the futuristic dark scheme for the premium experience
    val colorScheme = FuturisticDarkScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = DeepNavy.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SkillBridgeTypography,
        content = content,
    )
}
