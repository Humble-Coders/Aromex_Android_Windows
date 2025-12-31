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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntitySheet(
    onDismiss: () -> Unit,
    onSave: (Entity) -> Unit
) {
    var selectedType by remember { mutableStateOf(EntityType.CUSTOMER) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf(TextFieldValue("")) }
    var balanceType by remember { mutableStateOf(BalanceType.TO_RECEIVE) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AromexColors.LightBlue,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = AromexColors.AccentBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Add New Entity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AromexColors.TextDark
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Type",
                fontSize = 14.sp,
                color = AromexColors.TextGrey,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TypeRadioSelector(
                selected = selectedType,
                onSelect = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Name *", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AromexColors.TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter name") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AromexColors.BackgroundGrey,
                    unfocusedContainerColor = AromexColors.BackgroundGrey
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Phone", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AromexColors.TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    // Filter to allow only digits
                    phone = newValue.filter { it.isDigit() }
                },
                label = { Text("Enter phone") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AromexColors.BackgroundGrey,
                    unfocusedContainerColor = AromexColors.BackgroundGrey
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(text = "Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AromexColors.TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter email") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AromexColors.BackgroundGrey,
                    unfocusedContainerColor = AromexColors.BackgroundGrey
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Address", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AromexColors.TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Enter address") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AromexColors.BackgroundGrey,
                    unfocusedContainerColor = AromexColors.BackgroundGrey
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AromexColors.TextDark)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Enter notes") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                minLines = 3,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AromexColors.BackgroundGrey,
                    unfocusedContainerColor = AromexColors.BackgroundGrey
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Initial Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AromexColors.TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                 OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { newValue ->
                        val oldText = initialBalance.text
                        val oldSelection = initialBalance.selection
                        
                        // Filter to allow only digits, decimal point, and negative sign
                        var filtered = newValue.text.filter { it.isDigit() || it == '.' || it == '-' }
                        
                        // Ensure only one decimal point
                        val decimalCount = filtered.count { it == '.' }
                        if (decimalCount > 1) {
                            val firstDecimalIndex = filtered.indexOf('.')
                            filtered = filtered.substring(0, firstDecimalIndex + 1) + 
                                      filtered.substring(firstDecimalIndex + 1).replace(".", "")
                        }
                        
                        // Apply balance type logic and calculate cursor position
                        val (resultText, cursorOffset) = when (balanceType) {
                            BalanceType.TO_RECEIVE -> {
                                // Remove negative sign if present, keep only positive
                                val result = filtered.replace("-", "")
                                // Calculate cursor offset: if we removed a negative sign before cursor, adjust
                                val removedBeforeCursor = if (oldText.startsWith("-") && oldSelection.start > 0) 1 else 0
                                val newCursor = (newValue.selection.start - removedBeforeCursor).coerceIn(0, result.length)
                                Pair(result, newCursor)
                            }
                            BalanceType.TO_GIVE -> {
                                // Extract numeric part (digits and decimal point)
                                val numericPart = filtered.replace("-", "")
                                
                                if (numericPart.isEmpty() || numericPart == ".") {
                                    Pair(numericPart, newValue.selection.start.coerceIn(0, numericPart.length))
                                } else {
                                    // For TO_GIVE, we need negative sign
                                    val finalText = if (filtered.startsWith("-") && 
                                        filtered.substring(1) == numericPart &&
                                        numericPart.all { it.isDigit() || it == '.' }) {
                                        // Value is already in correct format - use it directly to preserve cursor
                                        filtered
                                    } else {
                                        // Need to ensure negative sign is at the start
                                        "-$numericPart"
                                    }
                                    
                                    // Calculate cursor position
                                    val newCursor = if (filtered.startsWith("-") && filtered == finalText) {
                                        // Value was already correct, preserve cursor position from newValue
                                        newValue.selection.start.coerceIn(0, finalText.length)
                                    } else {
                                        // We reconstructed the string, need to adjust cursor
                                        // If old text didn't have negative and new does, add 1 to cursor
                                        val cursorAdjustment = if (!oldText.startsWith("-") && finalText.startsWith("-")) {
                                            1
                                        } else if (oldText.startsWith("-") && !finalText.startsWith("-")) {
                                            -1
                                        } else {
                                            0
                                        }
                                        (newValue.selection.start + cursorAdjustment).coerceIn(0, finalText.length)
                                    }
                                    
                                    Pair(finalText, newCursor)
                                }
                            }
                        }
                        
                        initialBalance = TextFieldValue(
                            text = resultText,
                            selection = TextRange(cursorOffset)
                        )
                    },
                    label = { Text("Enter amount") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA)
                    )
                )
                BalanceTypeSelector(
                    selected = balanceType,
                    onSelect = { newBalanceType ->
                        balanceType = newBalanceType
                        // Adjust sign when balance type changes
                        val currentText = initialBalance.text
                        val numericValue = currentText.replace("-", "").replace(".", "")
                        if (numericValue.isNotEmpty() && numericValue.all { it.isDigit() }) {
                            val newText = when (newBalanceType) {
                                BalanceType.TO_RECEIVE -> {
                                    // Remove negative sign if present
                                    currentText.replace("-", "")
                                }
                                BalanceType.TO_GIVE -> {
                                    // Add negative sign if not present and value exists
                                    val valueWithoutSign = currentText.replace("-", "")
                                    if (valueWithoutSign.isNotEmpty() && !currentText.startsWith("-")) {
                                        "-$valueWithoutSign"
                                    } else {
                                        currentText
                                    }
                                }
                            }
                            // Preserve cursor position
                            val cursorPos = initialBalance.selection.start.coerceIn(0, newText.length)
                            initialBalance = TextFieldValue(
                                text = newText,
                                selection = TextRange(cursorPos)
                            )
                        }
                    }
                )
            }
            
            // Watch for balance type changes and adjust initial balance
            LaunchedEffect(balanceType) {
                val currentText = initialBalance.text
                val numericValue = currentText.replace("-", "").replace(".", "")
                if (numericValue.isNotEmpty() && numericValue.all { it.isDigit() }) {
                    val newText = when (balanceType) {
                        BalanceType.TO_RECEIVE -> {
                            // Ensure positive
                            if (currentText.startsWith("-")) {
                                currentText.replace("-", "")
                            } else {
                                currentText
                            }
                        }
                        BalanceType.TO_GIVE -> {
                            // Ensure negative
                            if (!currentText.startsWith("-") && currentText.isNotEmpty()) {
                                "-$currentText"
                            } else {
                                currentText
                            }
                        }
                    }
                    // Preserve cursor position
                    val cursorPos = initialBalance.selection.start.coerceIn(0, newText.length)
                    initialBalance = TextFieldValue(
                        text = newText,
                        selection = TextRange(cursorPos)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", color = AromexColors.TextDark)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        onSave(
                            Entity(
                                type = selectedType,
                                name = name,
                                phone = phone,
                                email = email,
                                address = address,
                                notes = notes,
                                initialBalance = initialBalance.text.toDoubleOrNull() ?: 0.0,
                                balanceType = balanceType
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AromexColors.ButtonBlue,
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Text("Save ${selectedType.name.lowercase().capitalize()}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeRadioSelector(
    selected: EntityType,
    onSelect: (EntityType) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypePill(
            text = "Customer",
            isSelected = selected == EntityType.CUSTOMER,
            onClick = { onSelect(EntityType.CUSTOMER) }
        )
        TypePill(
            text = "Supplier",
            isSelected = selected == EntityType.SUPPLIER,
            onClick = { onSelect(EntityType.SUPPLIER) }
        )
        TypePill(
            text = "Middleman",
            isSelected = selected == EntityType.MIDDLEMAN,
            onClick = { onSelect(EntityType.MIDDLEMAN) }
        )
    }
}

@Composable
private fun TypePill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) AromexColors.LightBlue else AromexColors.BackgroundGrey
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AromexColors.AccentBlue else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Checkmark icon in circular background
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) AromexColors.AccentBlue else AromexColors.ForegroundWhite
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
            Text(
                text = text,
                fontSize = 12.sp,
                color = AromexColors.TextDark,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun BalanceTypeSelector(
    selected: BalanceType,
    onSelect: (BalanceType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AromexColors.BackgroundGrey)
            .padding(4.dp)
    ) {
        BalanceTypeOption(
            text = "To Receive",
            isSelected = selected == BalanceType.TO_RECEIVE,
            onClick = { onSelect(BalanceType.TO_RECEIVE) },
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        BalanceTypeOption(
            text = "To Give",
            isSelected = selected == BalanceType.TO_GIVE,
            onClick = { onSelect(BalanceType.TO_GIVE) },
            color = Color(0xFFE57373),
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
    }
}

@Composable
private fun BalanceTypeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else AromexColors.TextGrey,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
