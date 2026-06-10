package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PremiumDarkColorScheme = darkColorScheme(
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme as requested
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = PremiumDarkColorScheme
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
