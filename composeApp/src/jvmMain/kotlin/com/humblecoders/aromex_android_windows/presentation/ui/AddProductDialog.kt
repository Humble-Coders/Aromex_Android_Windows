package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    onClose: () -> Unit
) {
    DialogWindow(
        onCloseRequest = onClose,
        title = "Add Product",
        state = rememberDialogState(
            width = 1400.dp,   // ✅ REAL WIDTH CONTROL
            height = 900.dp
        ),
        resizable = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F8F8)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(12.dp)
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
                                fontWeight = FontWeight.SemiBold
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
                        Field("Brand *", "Choose an option", Modifier.weight(1f))
                        Field("Model *", "Select a brand first", Modifier.weight(1f))
                        CapacityField(Modifier.weight(1f))
                    }

                    /* ---------- ROW 2 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Field("IMEI/Serial *", "Enter IMEI or Serial num", Modifier.weight(1f))
                        Field("Carrier *", "Choose an option", Modifier.weight(1f))
                        Field("Color *", "Choose an option", Modifier.weight(1f))
                    }

                    /* ---------- ROW 3 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Field("Status", "Active", Modifier.weight(1f))
                        Field("Price *", "Enter price (e.g., 299.99)", Modifier.weight(1f))
                        Field("Storage Location *", "Choose an option", Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(12.dp))

                    /* ---------- ACTION BUTTONS ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            onClick = onClose,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            enabled = false
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add Product",
                                fontSize = 15.sp,
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
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
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
            textStyle = TextStyle(textAlign = TextAlign.Left),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )
    }
}

@Composable
private fun CapacityField(
    modifier: Modifier = Modifier
) {
    var selectedUnit by remember { mutableStateOf("GB") }
    
    Column(modifier) {
        Text(
            text = "Capacity *",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF79747E), RoundedCornerShape(10.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TextField area
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
                
                // Buttons area
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    // GB button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedUnit == "GB") Color(0xFF2F80ED) else Color(0xFFF2F2F2))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedUnit = "GB"
                            }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "GB",
                            color = if (selectedUnit == "GB") Color.White else Color.Gray,
                            fontWeight = if (selectedUnit == "GB") FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                    // TB button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedUnit == "TB") Color(0xFF2F80ED) else Color(0xFFF2F2F2))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                selectedUnit = "TB"
                            }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TB",
                            color = if (selectedUnit == "TB") Color.White else Color.Gray,
                            fontWeight = if (selectedUnit == "TB") FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

