package com.humblecoders.aromex_android_windows.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AromexColors {
    // Get theme-aware colors using MaterialTheme
    @Composable
    fun PrimaryBlue(): Color = MaterialTheme.colorScheme.primary
    
    @Composable
    fun SelectedBlue(): Color = MaterialTheme.colorScheme.primaryContainer
    
    @Composable
    fun LightBlue(): Color = MaterialTheme.colorScheme.surfaceVariant
    
    @Composable
    fun AccentBlue(): Color = MaterialTheme.colorScheme.secondary
    
    @Composable
    fun ButtonBlue(): Color = MaterialTheme.colorScheme.primary
    
    @Composable
    fun BackgroundGrey(): Color = MaterialTheme.colorScheme.background
    
    @Composable
    fun ForegroundWhite(): Color = MaterialTheme.colorScheme.surface
    
    @Composable
    fun TextDark(): Color = MaterialTheme.colorScheme.onSurface
    
    @Composable
    fun TextGrey(): Color = MaterialTheme.colorScheme.onSurfaceVariant
    
    @Composable
    fun SuccessColor(): Color = getAromexSuccessColor()
    
    // Legacy static properties for backward compatibility (will be deprecated)
    @Deprecated("Use PrimaryBlue() instead", ReplaceWith("PrimaryBlue()"))
    val PrimaryBlue = Color(0xFF1E3A5F)
    
    @Deprecated("Use SelectedBlue() instead", ReplaceWith("SelectedBlue()"))
    val SelectedBlue = Color(0xFF2E5A8F)
    
    @Deprecated("Use LightBlue() instead", ReplaceWith("LightBlue()"))
    val LightBlue = Color(0xFFE3F2FD)
    
    @Deprecated("Use AccentBlue() instead", ReplaceWith("AccentBlue()"))
    val AccentBlue = Color(0xFF1E88E5)
    
    @Deprecated("Use ButtonBlue() instead", ReplaceWith("ButtonBlue()"))
    val ButtonBlue = Color(0xFF1E3A5F)
    
    @Deprecated("Use BackgroundGrey() instead", ReplaceWith("BackgroundGrey()"))
    val BackgroundGrey = Color(0xFFF5F5F5)
    
    @Deprecated("Use ForegroundWhite() instead", ReplaceWith("ForegroundWhite()"))
    val ForegroundWhite = Color(0xFFFFFFFF)
    
    @Deprecated("Use TextDark() instead", ReplaceWith("TextDark()"))
    val TextDark = Color(0xFF212121)
    
    @Deprecated("Use TextGrey() instead", ReplaceWith("TextGrey()"))
    val TextGrey = Color(0xFF757575)
}

