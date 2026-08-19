package com.skillbridge.ai.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.skillbridge.ai.presentation.theme.*

// ══════════════════════════════════════════════════════════════════════════════
// ── GLASS-MORPHISM CARD ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Reusable glass-morphism card with translucent dark background and subtle border glow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = ElectricBlue,
    borderAlpha: Float = 0.15f,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        glowColor.copy(alpha = borderAlpha),
                        glowColor.copy(alpha = borderAlpha * 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = GlassWhite6
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── NEON PROGRESS BAR ─────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Animated gradient progress bar with neon glow trail.
 */
@Composable
fun NeonProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color = GlassWhite8,
    gradientStart: Color = ElectricBlue,
    gradientEnd: Color = NeonCyan
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "neon_progress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    )
                )
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── AI STATUS INDICATOR ───────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Pulsing AI status dot with "AI Online" label.
 */
@Composable
fun AIStatusIndicator(
    modifier: Modifier = Modifier,
    label: String = "AI Online"
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(NeonGreen.copy(alpha = pulseAlpha))
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NeonGreen.copy(alpha = 0.9f)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── SHIMMER LOADING ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

fun Modifier.shimmerLoadingAnimation(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1000)
        ), label = ""
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                GlassWhite6,
                GlassWhite15,
                GlassWhite6,
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── PRIMARY BUTTON (GRADIENT) ─────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "button_scale")

    Button(
        onClick = onClick,
        modifier = modifier
            .height(54.dp)
            .scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            disabledContainerColor = GlassWhite8,
            disabledContentColor = TextMuted
        ),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (enabled) {
                        Brush.horizontalGradient(
                            colors = listOf(ElectricBlue, NeonCyan)
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(GlassWhite8, GlassWhite10)
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    letterSpacing = 0.5.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── OUTLINED BUTTON ───────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(ElectricBlue.copy(alpha = 0.6f), NeonCyan.copy(alpha = 0.4f))
            )
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = ElectricBlue,
            containerColor = Color.Transparent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── TEXT FIELD ─────────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillBridgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let { { Icon(imageVector = it, contentDescription = null) } },
            trailingIcon = trailingIcon,
            isError = isError,
            visualTransformation = visualTransformation,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassWhite15,
                errorBorderColor = NeonRed,
                focusedLabelColor = ElectricBlue,
                unfocusedLabelColor = TextMuted,
                cursorColor = NeonCyan,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextSecondary,
                focusedLeadingIconColor = ElectricBlue,
                unfocusedLeadingIconColor = TextMuted,
                focusedContainerColor = GlassWhite4,
                unfocusedContainerColor = Color.Transparent
            )
        )
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = NeonRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── TOP APP BAR ───────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillBridgeTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Go back",
                        tint = TextSecondary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = DeepNavy.copy(alpha = 0.95f),
            scrolledContainerColor = DarkNavy
        )
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// ── SECTION HEADER ────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glow dot indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan, ElectricBlue.copy(alpha = 0.3f))
                    )
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp
            ),
            color = TextPrimary
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── INFO CARD ─────────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    category: String? = null,
    timestamp: String? = null
) {
    GlassCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            if (category != null) {
                val tagColor = when (category.lowercase()) {
                    "resume" -> ElectricBlue
                    "skill" -> NeonViolet
                    "progress" -> NeonCyan
                    else -> TextMuted
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagColor.copy(alpha = 0.15f),
                    modifier = Modifier.border(
                        1.dp,
                        tagColor.copy(alpha = 0.3f),
                        RoundedCornerShape(6.dp)
                    )
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = tagColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        if (timestamp != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── PROGRESS CARD ─────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeProgressCard(
    title: String,
    percentage: Int,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier, glowColor = NeonCyan) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(72.dp)
            ) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = NeonCyan,
                    strokeWidth = 6.dp,
                    trackColor = GlassWhite8,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    ),
                    color = NeonCyan
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── SKILL CHIP (NEON OUTLINED) ────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeSkillChip(
    skillName: String,
    type: String, // "current", "missing", "recommended"
    modifier: Modifier = Modifier
) {
    val chipColor = when (type) {
        "current" -> NeonGreen
        "missing" -> NeonRed
        else -> NeonViolet
    }

    Surface(
        modifier = modifier
            .padding(4.dp)
            .border(1.dp, chipColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = chipColor.copy(alpha = 0.1f)
    ) {
        Text(
            text = skillName,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = chipColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── FEATURE CARD ──────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeFeatureCard(
    title: String,
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = GlassWhite6
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricBlue.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 2
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── BOTTOM NAVIGATION BAR ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun SkillBridgeBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = DeepNavy.copy(alpha = 0.97f),
        tonalElevation = 0.dp,
        modifier = Modifier.border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(BorderSubtle, Color.Transparent)
            ),
            shape = RoundedCornerShape(0.dp)
        )
    ) {
        val items = listOf(
            BottomNavItem("Home", com.skillbridge.ai.presentation.navigation.Screen.Dashboard.route, Icons.Rounded.Home),
            BottomNavItem("Roadmap", com.skillbridge.ai.presentation.navigation.Screen.Roadmap.route, Icons.Rounded.Timeline),
            BottomNavItem("Skills", com.skillbridge.ai.presentation.navigation.Screen.SkillGap.route, Icons.Rounded.Analytics),
            BottomNavItem("AI Chat", com.skillbridge.ai.presentation.navigation.Screen.Chat.route, Icons.Rounded.SmartToy),
            BottomNavItem("Profile", com.skillbridge.ai.presentation.navigation.Screen.Profile.route, Icons.Rounded.Person)
        )

        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (selected) NeonCyan else TextDim
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) NeonCyan else TextDim
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = 0.1f),
                    unselectedIconColor = TextDim,
                    unselectedTextColor = TextDim
                )
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── PRIORITY BADGE (NEON) ─────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun PriorityBadge(priority: String) {
    val badgeColor = when (priority.uppercase()) {
        "HIGH" -> NeonRed
        "MEDIUM" -> NeonAmber
        else -> ElectricBlue
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = badgeColor.copy(alpha = 0.12f),
        modifier = Modifier.border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── LOADING DIALOG ────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun SkillBridgeLoadingDialog(
    show: Boolean,
    message: String = "Loading..."
) {
    if (show) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardNavy,
                modifier = Modifier
                    .size(width = 240.dp, height = 150.dp)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        trackColor = GlassWhite8,
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── LOADING SCREEN ────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun LoadingScreenComponent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = NeonCyan,
                trackColor = GlassWhite8
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── EMPTY STATE ───────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun EmptyStateComponent(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📂",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ── ERROR STATE ───────────────────────────────────────────────────────────────
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ErrorStateComponent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = NeonRed,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        SkillBridgePrimaryButton(
            text = "Retry",
            onClick = onRetry
        )
    }
}
