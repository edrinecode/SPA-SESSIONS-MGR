package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpaColorScheme = lightColorScheme(
    primary = SpaDeepForest,
    onPrimary = Color.White,
    primaryContainer = SpaMist,
    onPrimaryContainer = SpaDarkGreen,

    secondary = SpaGoldDark,
    onSecondary = Color.White,
    secondaryContainer = SpaGoldLight,
    onSecondaryContainer = SpaDarkGreen,

    tertiary = SpaSage,
    onTertiary = Color.White,
    tertiaryContainer = SpaMist,
    onTertiaryContainer = SpaDarkGreen,

    background = SpaBackground,
    onBackground = SpaTextPrimary,

    surface = SpaSurface,
    onSurface = SpaTextPrimary,
    surfaceVariant = SpaPaleSurface,
    onSurfaceVariant = SpaTextSecondary,

    outline = SpaCardBorder,
    outlineVariant = Color(0xFFD3DDD8)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SpaColorScheme,
        typography = Typography,
        content = content
    )
}
