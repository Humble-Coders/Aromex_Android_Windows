package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Header
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            Divider()

            /* ---------- FIELDS - ONE PER ROW ---------- */
            Field("Brand *", "Choose an option", Modifier.fillMaxWidth())
            Field("Model *", "Select a brand first", Modifier.fillMaxWidth())
            CapacityField(Modifier.fillMaxWidth())
            Field("IMEI/Serial *", "Enter IMEI or Serial num", Modifier.fillMaxWidth())
            Field("Carrier *", "Choose an option", Modifier.fillMaxWidth())
            Field("Color *", "Choose an option", Modifier.fillMaxWidth())
            Field("Status", "Active", Modifier.fillMaxWidth())
            Field("Price *", "Enter price (e.g., 299.99)", Modifier.fillMaxWidth())
            Field("Storage Location *", "Choose an option", Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))

            /* ---------- ACTION BUTTONS ---------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    onClick = onDismiss,
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
                        .height(50.dp),
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
            fontWeight = FontWeight.Medium,
            color = AromexColors.TextDark
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
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AromexColors.BackgroundGrey,
                unfocusedContainerColor = AromexColors.BackgroundGrey
            )
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
            fontWeight = FontWeight.Medium,
            color = AromexColors.TextDark
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

