package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

@Composable
fun AddServiceDialog(
    isDarkTheme: Boolean = false,
    onDismiss: () -> Unit
) {
    var serviceName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    val slideOffset = remember { Animatable(-1000f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(10)
        kotlinx.coroutines.coroutineScope {
            launch {
                slideOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 400,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .width(500.dp)
                .padding(16.dp)
                .graphicsLayer {
                    translationY = slideOffset.value
                    this.alpha = alpha.value
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Add Service",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            serviceName = ""
                            price = ""
                            onDismiss()
                        },
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                HorizontalDivider()

                // Service Name Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Service Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        placeholder = {
                            Text(
                                "Enter service name",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Left
                            )
                        },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Left,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Price Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Price",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { newValue ->
                            // Filter to allow only digits, decimal point
                            var filtered = newValue.filter { it.isDigit() || it == '.' }
                            // Ensure only one decimal point
                            val decimalCount = filtered.count { it == '.' }
                            if (decimalCount > 1) {
                                val firstDecimalIndex = filtered.indexOf('.')
                                filtered = filtered.take(firstDecimalIndex + 1) +
                                        filtered.substring(firstDecimalIndex + 1).replace(".", "")
                            }
                            price = filtered
                        },
                        placeholder = {
                            Text(
                                "$0.00",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Left
                            )
                        },
                        textStyle = TextStyle(
                            textAlign = TextAlign.Left,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        onClick = {
                            serviceName = ""
                            price = ""
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = "Cancel",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val isSaveEnabled = serviceName.isNotBlank() && price.isNotBlank()
                    Button(
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        onClick = {
                            // TODO: Save service
                            serviceName = ""
                            price = ""
                            onDismiss()
                        },
                        enabled = isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9800),
                            disabledContainerColor = Color(0xFFCCCCCC)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add Service",
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
