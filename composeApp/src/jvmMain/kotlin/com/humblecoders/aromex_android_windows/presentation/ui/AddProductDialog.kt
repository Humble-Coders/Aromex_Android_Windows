package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductCard(
    showCard: Boolean,
    isDarkTheme: Boolean = false,
    onClose: () -> Unit
) {
    AnimatedVisibility(
        visible = showCard,
        enter = fadeIn(tween(300)) +
                scaleIn(
                    animationSpec = tween(300),
                    initialScale = 0.9f
                ),
        exit = fadeOut(tween(300)) +
                scaleOut(
                    animationSpec = tween(300),
                    targetScale = 0.9f
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10000f)
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.9f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
                ),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {

                    /* ---------- HEADER ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF2F80ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Add Product",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Divider()

                    /* ---------- ROW 1 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Field("Brand *", "Choose an option", isDarkTheme, Modifier.weight(1f))
                        Field("Model *", "Select a brand first", isDarkTheme, Modifier.weight(1f))
                        CapacityField(isDarkTheme, Modifier.weight(1f))
                    }

                    /* ---------- ROW 2 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Field("IMEI/Serial *", "Enter IMEI or Serial num", isDarkTheme, Modifier.weight(1f))
                        Field("Carrier *", "Choose an option", isDarkTheme, Modifier.weight(1f))
                        Field("Color *", "Choose an option", isDarkTheme, Modifier.weight(1f))
                    }

                    /* ---------- ROW 3 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Field("Status", "Active", isDarkTheme, Modifier.weight(1f))
                        Field("Price *", "Enter price (e.g., 299.99)", isDarkTheme, Modifier.weight(1f))
                        Field("Storage Location *", "Choose an option", isDarkTheme, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    /* ---------- ACTIONS ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            onClick = onClose
                        ) {
                            Text(
                                text = "Cancel",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val isSaveEnabled = false
                        Button(
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp),
                            onClick = {},
                            enabled = isSaveEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color(0xFFCCCCCC)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add Product",
                                color = if (!isSaveEnabled && isDarkTheme) Color(0xFF424242) else Color.Unspecified,
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

@Composable
private fun Field(
    label: String,
    placeholder: String,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Left
                )
            },
            textStyle = TextStyle(
                textAlign = TextAlign.Left,
                color = if (isDarkTheme) Color.White else Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
            )
        )
    }
}

@Composable
private fun CapacityField(
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedUnit by remember { mutableStateOf("GB") }

    Column(modifier) {
        Text(
            "Capacity *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(10.dp))
                .background(
                    if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Choose an option",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("GB", "TB").forEach { unit ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selectedUnit == unit) Color(0xFF2F80ED)
                                    else if (isDarkTheme) Color(0xFFFAFAFA)
                                    else Color(0xFFF2F2F2)
                                )
                                .border(
                                    width = if (selectedUnit == unit) 0.dp else 1.dp,
                                    color = if (selectedUnit == unit) Color.Transparent else Color(0xFFE8E8E8),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { selectedUnit = unit }
                                .pointerHoverIcon(PointerIcon.Hand)
                                .height(36.dp)
                                .padding(horizontal = 18.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unit,
                                fontSize = 13.sp,
                                fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedUnit == unit) Color.White else Color(0xFF424242)
                            )
                        }
                    }
                }
            }
        }
    }
}
