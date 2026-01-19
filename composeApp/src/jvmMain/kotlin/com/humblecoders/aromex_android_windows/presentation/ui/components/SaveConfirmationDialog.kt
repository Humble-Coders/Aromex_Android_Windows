package com.humblecoders.aromex_android_windows.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Helper function for success color based on theme
@Composable
private fun getAromexSuccessColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF4CAF50)
}

/**
 * Generic save confirmation dialog that shows saving and success states.
 * Can be reused for any save operation (entity, expense, etc.)
 * 
 * @param isSaving Whether the save operation is in progress
 * @param showSuccess Whether to show the success message
 * @param savingText Text to display while saving (e.g., "Saving Expense...")
 * @param successText Text to display on success (e.g., "Expense Added Successfully!")
 * @param isDarkTheme Whether dark theme is active
 * @param onDismissSuccess Callback when success dialog should be dismissed
 * @param autoDismissDelay Delay in milliseconds before auto-dismissing success dialog (default: 2500ms)
 */
@Composable
fun SaveConfirmationDialog(
    isSaving: Boolean,
    showSuccess: Boolean,
    savingText: String,
    successText: String,
    isDarkTheme: Boolean,
    onDismissSuccess: () -> Unit,
    autoDismissDelay: Long = 2500L
) {
    // Auto-dismiss success dialog after showing
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(autoDismissDelay)
            onDismissSuccess()
        }
    }
    
    if (isSaving || showSuccess) {
        Dialog(onDismissRequest = {}) {
            Box(contentAlignment = Alignment.Center) {
                Card(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Saving state
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSaving,
                            enter = fadeIn(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = savingText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        // Success state
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showSuccess,
                            enter = fadeIn(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = getAromexSuccessColor(isDarkTheme),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = successText,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

