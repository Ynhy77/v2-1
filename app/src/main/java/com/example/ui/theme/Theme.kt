package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = CyberOnPrimary,
    onSecondary = CyberOnSecondary,
    onTertiary = CyberOnPrimary,
    onBackground = CyberText,
    onSurface = CyberText,
    surfaceVariant = CyberSurfaceVariant,
    error = CyberError
)

private val LightColorScheme = lightColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = CyberOnPrimary,
    onSecondary = CyberOnSecondary,
    onTertiary = CyberOnPrimary,
    onBackground = CyberText,
    onSurface = CyberText,
    surfaceVariant = CyberSurfaceVariant,
    error = CyberError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme for the immersive cyber environment
    dynamicColor: Boolean = false, // Disable dynamic colors to keep brand's distinct cyan-magenta neon theme
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
