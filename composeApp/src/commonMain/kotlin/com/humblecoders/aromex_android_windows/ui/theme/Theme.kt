package com.humblecoders.aromex_android_windows.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary and Secondary colors - same in both light and dark modes
private val PrimaryBlue = Color(0xFF1E3A5F)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFF2E5A8F)
private val OnPrimaryContainer = Color(0xFFFFFFFF)
private val SecondaryBlue = Color(0xFF1E88E5)
private val OnSecondary = Color(0xFFFFFFFF)

// Light theme colors - backgrounds and surfaces
private val LightBackground = Color(0xFFFFFFFF) // White background in light mode
private val LightOnBackground = Color(0xFF212121) // Dark text on light background
private val LightSurface = Color(0xFFF5F5F5) // Light gray surface
private val LightOnSurface = Color(0xFF212121) // Dark text on light surface
private val LightSurfaceVariant = Color(0xFFE3F2FD) // Light blue variant
private val LightOnSurfaceVariant = Color(0xFF757575) // Gray text
private val LightError = Color(0xFFD32F2F)
private val LightOnError = Color(0xFFFFFFFF)
private val LightErrorContainer = Color(0xFFFFEBEE)
private val LightSuccess = Color(0xFF4CAF50)
private val LightSuccessContainer = Color(0xFFC8E6C9)

// Dark theme colors - backgrounds and surfaces
private val DarkBackground = Color(0xFF121212) // Dark background in dark mode
private val DarkOnBackground = Color(0xFFE0E0E0) // Light text on dark background
private val DarkSurface = Color(0xFF1E1E1E) // Dark surface
private val DarkOnSurface = Color(0xFFE0E0E0) // Light text on dark surface
private val DarkSurfaceVariant = Color(0xFF2C2C2C) // Dark variant
private val DarkOnSurfaceVariant = Color(0xFFB0B0B0) // Light gray text
private val DarkError = Color(0xFFEF5350)
private val DarkOnError = Color(0xFFFFFFFF)
private val DarkErrorContainer = Color(0xFF8E0000)
private val DarkSuccess = Color(0xFF66BB6A)
private val DarkSuccessContainer = Color(0xFF1B5E20)

private val LightColorScheme = lightColorScheme(
    // Primary and Secondary - same in both modes
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryBlue,
    onSecondary = OnSecondary,
    // Backgrounds and surfaces - light mode (white backgrounds)
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = Color(0xFFC62828)
)

private val DarkColorScheme = darkColorScheme(
    // Primary and Secondary - same in both modes
    primary = PrimaryBlue,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = SecondaryBlue,
    onSecondary = OnSecondary,
    // Backgrounds and surfaces - dark mode (dark backgrounds)
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun AromexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

// Extended color properties for dark mode support
@Composable
fun getAromexSuccessColor(): Color {
    return if (isSystemInDarkTheme()) DarkSuccess else LightSuccess
}

@Composable
fun getAromexSuccessContainerColor(): Color {
    return if (isSystemInDarkTheme()) DarkSuccessContainer else LightSuccessContainer
}


