package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class AgentOSColors(
    val statusOnline: Color = StatusOnline,
    val statusExecuting: Color = StatusExecuting,
    val statusApprovalRequired: Color = StatusApprovalRequired,
    val statusError: Color = StatusError,
    val statusRateLimited: Color = StatusRateLimited,
    val statusOffline: Color = StatusOffline,
    val backgroundDark: Color = BackgroundDark,
    val surfaceDark: Color = SurfaceDark,
    val surfaceVariantDark: Color = SurfaceVariantDark,
    val surfaceBorderDark: Color = SurfaceBorderDark,
    val cyanAccent: Color = CyanAccent,
    val indigoAccent: Color = IndigoAccent,
    val textPrimary: Color = TextPrimary,
    val textSecondary: Color = TextSecondary,
    val textMuted: Color = TextMuted
)

val LocalAgentOSColors = staticCompositionLocalOf { AgentOSColors() }

private val AgentOSColorScheme = darkColorScheme(
    primary = StatusExecuting,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = TextPrimary,
    secondary = CyanAccent,
    onSecondary = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    error = StatusError,
    onError = TextPrimary,
    outline = SurfaceBorderDark
)

@Composable
fun AgentOSTheme(
    colors: AgentOSColors = AgentOSColors(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = SurfaceDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    CompositionLocalProvider(
        LocalAgentOSColors provides colors
    ) {
        MaterialTheme(
            colorScheme = AgentOSColorScheme,
            typography = Typography,
            content = content
        )
    }
}

object AgentOSTheme {
    val statusColors: AgentOSColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAgentOSColors.current
}

