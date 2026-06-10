package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFF00E5FF),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFFFAB00),
    onSecondary = Color(0xFF000000),
    tertiary = Color(0xFFE040FB),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF0F1014),
    surface = Color(0xFF1A1C23),
    onBackground = Color(0xFFE6E8F0),
    onSurface = Color(0xFFE6E8F0),
    surfaceVariant = Color(0xFF262933),
    onSurfaceVariant = Color(0xFFB0B3C0),
    error = Color(0xFFFF5252),
    onError = Color(0xFF000000),
)

val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFF4C82FF),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFFB388FF),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF0B101E),
    surface = Color(0xFF141926),
    onBackground = Color(0xFFD4DAE8),
    onSurface = Color(0xFFD4DAE8),
    surfaceVariant = Color(0xFF1F2536),
    onSurfaceVariant = Color(0xFF9098A9),
    error = Color(0xFFFF5252),
)

val EmeraldColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF0A120D),
    surface = Color(0xFF111D15),
    onBackground = Color(0xFFD2E0D6),
    onSurface = Color(0xFFD2E0D6),
    surfaceVariant = Color(0xFF182A1E),
    onSurfaceVariant = Color(0xFF8B9E91),
    error = Color(0xFFFF5252),
)

val FreshLightColorScheme = lightColorScheme(
    primary = Color(0xFF12C99B),
    onPrimary = Color.White,
    secondary = Color(0xFF2196F3),
    onSecondary = Color.White,
    tertiary = Color(0xFFFF7043),
    onTertiary = Color.White,
    background = Color(0xFFF9F9F9),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF444444),
    error = Color(0xFFFF5252),
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    themeIndex: Int = 0,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeIndex) {
        1 -> MidnightColorScheme
        2 -> EmeraldColorScheme
        3 -> FreshLightColorScheme
        else -> CyberpunkColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

