package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OpenSwosDarkColorScheme = darkColorScheme(
    primary = SwosGold,
    onPrimary = RetroDarkBg,
    primaryContainer = RetroSurface,
    onPrimaryContainer = SwosGold,
    secondary = PitchGreen,
    onSecondary = PitchLineWhite,
    secondaryContainer = DarkPitchGreen,
    onSecondaryContainer = PitchLineWhite,
    tertiary = SwosCyan,
    onTertiary = RetroDarkBg,
    background = RetroDarkBg,
    onBackground = RetroTextPrimary,
    surface = RetroCardBg,
    onSurface = RetroTextPrimary,
    surfaceVariant = RetroSurface,
    onSurfaceVariant = RetroTextSecondary,
    outline = RetroBorder,
    error = SwosRed,
    onError = PitchLineWhite
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = OpenSwosDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = RetroDarkBg.toArgb()
                window.navigationBarColor = RetroDarkBg.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
