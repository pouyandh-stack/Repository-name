package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    secondary = AmberAccent,
    tertiary = EmeraldGreen,
    background = SurfaceDark,
    surface = SurfaceCardDark,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceDark,
    onTertiary = SurfaceWhite,
    onBackground = SurfaceWhite,
    onSurface = SurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = SoberNavy,
    secondary = AmberAccent,
    tertiary = EmeraldGreen,
    background = NeutralLight,
    surface = SurfaceWhite,
    onPrimary = SurfaceWhite,
    onSecondary = SurfaceDark,
    onTertiary = SurfaceWhite,
    onBackground = NeutralDark,
    onSurface = NeutralDark
)

@Composable
fun JaarchiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
