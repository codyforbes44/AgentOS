package com.codyforbes.agentos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Immutable
data class AgentOSColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val statusOnline: Color = StatusOnline,
    val statusExecuting: Color = StatusExecuting,
    val statusApprovalRequired: Color = StatusApprovalRequired,
    val statusError: Color = StatusError,
    val statusRateLimited: Color = StatusRateLimited,
    val statusOffline: Color = StatusOffline,
    val cyanAccent: Color = CyanAccent,
    val indigoAccent: Color = IndigoAccent,
)

private val DarkAgentOSColors = AgentOSColors(
    background = PaletteDarkBackground,
    surface = PaletteDarkSurface,
    surfaceVariant = PaletteDarkSurfaceVariant,
    surfaceBorder = PaletteDarkBorder,
    textPrimary = PaletteDarkTextPrimary,
    textSecondary = PaletteDarkTextSecondary,
    textMuted = PaletteDarkTextMuted,
)

private val LightAgentOSColors = AgentOSColors(
    background = PaletteLightBackground,
    surface = PaletteLightSurface,
    surfaceVariant = PaletteLightSurfaceVariant,
    surfaceBorder = PaletteLightBorder,
    textPrimary = PaletteLightTextPrimary,
    textSecondary = PaletteLightTextSecondary,
    textMuted = PaletteLightTextMuted,
)

val LocalAgentOSColors = staticCompositionLocalOf { DarkAgentOSColors }

private val DarkScheme = darkColorScheme(
    primary = StatusExecuting,
    onPrimary = OnBrightFill,
    primaryContainer = PaletteDarkSurfaceVariant,
    onPrimaryContainer = PaletteDarkTextPrimary,
    secondary = CyanAccent,
    onSecondary = OnStatusFill,
    background = PaletteDarkBackground,
    onBackground = PaletteDarkTextPrimary,
    surface = PaletteDarkSurface,
    onSurface = PaletteDarkTextPrimary,
    surfaceVariant = PaletteDarkSurfaceVariant,
    onSurfaceVariant = PaletteDarkTextSecondary,
    error = StatusError,
    onError = OnBrightFill,
    outline = PaletteDarkBorder,
)

private val LightScheme = lightColorScheme(
    primary = StatusExecuting,
    onPrimary = OnBrightFill,
    primaryContainer = PaletteLightSurfaceVariant,
    onPrimaryContainer = PaletteLightTextPrimary,
    secondary = CyanAccent,
    onSecondary = OnStatusFill,
    background = PaletteLightBackground,
    onBackground = PaletteLightTextPrimary,
    surface = PaletteLightSurface,
    onSurface = PaletteLightTextPrimary,
    surfaceVariant = PaletteLightSurfaceVariant,
    onSurfaceVariant = PaletteLightTextSecondary,
    error = StatusError,
    onError = OnBrightFill,
    outline = PaletteLightBorder,
)

val BackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.background

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.surface

val SurfaceVariantDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.surfaceVariant

val SurfaceBorderDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.surfaceBorder

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.textSecondary

val TextMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAgentOSColors.current.textMuted

@Composable
fun AgentOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkAgentOSColors else LightAgentOSColors
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAgentOSColors provides colors) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Typography,
            content = content,
        )
    }
}
