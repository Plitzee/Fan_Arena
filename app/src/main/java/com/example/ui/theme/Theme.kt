package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FanArenaColorScheme = darkColorScheme(
    primary = PrimaryElectricBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryNeonGreen,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = TertiaryOrange,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = BackgroundNavy,
    onBackground = OnSurface,
    surface = SurfaceContainer,
    onSurface = OnSurface,
    surfaceVariant = SurfaceHighest,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineColor,
    outlineVariant = OutlineVariant,
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FanArenaColorScheme,
        typography = Typography,
        content = content
    )
}
