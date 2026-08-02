package com.example.surveycad.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = BgCanvas,
    primaryContainer = AccentCyanDim,
    onPrimaryContainer = AccentCyan,
    secondary = AmberWarning,
    onSecondary = BgCanvas,
    background = BgApp,
    onBackground = TextPrimary,
    surface = BgPanel,
    onSurface = TextPrimary,
    surfaceVariant = BgPanel2,
    onSurfaceVariant = TextDim,
    outline = LineBorder,
    outlineVariant = LineBorderSoft
)

@Composable
fun SurveyCADTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
