package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.HorizontalDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceSheet(
    isDarkTheme: Boolean = false,
    onDismiss: () -> Unit
) {
    var serviceName by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }

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
                            .background(Color(0xFFFF9800)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = "Add Service",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            Divider()

            // Service Name Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Service Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White else AromexColors.TextDark()
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
                        color = if (isDarkTheme) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                        unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    )
                )
            }

            // Price Field
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Price",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White else AromexColors.TextDark()
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
                        color = if (isDarkTheme) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                        unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
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

                val isSaveEnabled = serviceName.isNotBlank() && price.isNotBlank()
                Button(
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    onClick = {
                        // TODO: Save service
                        onDismiss()
                    },
                    shape = RoundedCornerShape(10.dp),
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
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

