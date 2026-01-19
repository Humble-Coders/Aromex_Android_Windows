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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntitySheet(
    onDismiss: () -> Unit,
    onSave: (Entity) -> Unit,
    viewModel: com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel,
    initialName: String = "",
    initialType: EntityType? = null
) {
    var selectedType by rememberSaveable { mutableStateOf(initialType ?: EntityType.CUSTOMER) }
    var name by rememberSaveable { mutableStateOf(initialName) }
    
    // Update values when dialog is shown with new initial values
    LaunchedEffect(initialName, initialType) {
        if (initialName.isNotEmpty()) {
            name = initialName
        }
        if (initialType != null) {
            selectedType = initialType
        }
    }
    var phone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var initialBalance by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var balanceType by rememberSaveable { mutableStateOf(BalanceType.TO_RECEIVE) }

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
                    color = AromexColors.LightBlue(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = AromexColors.AccentBlue()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Add New Entity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Type",
                fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TypeRadioSelector(
                selected = selectedType,
                onSelect = { selectedType = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Name *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Phone",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Email",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Address",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Enter address") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Notes",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Initial Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                        
                        val result = viewModel.formatBalanceInput(
                            newText = newValue.text,
                            newCursorPosition = newValue.selection.start,
                            oldText = oldText,
                            oldCursorPosition = oldSelection.start,
                            balanceType = balanceType
                        )
                        
                        initialBalance = TextFieldValue(
                            text = result.text,
                            selection = TextRange(result.cursorPosition)
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
                            val result = viewModel.formatBalanceOnTypeChange(
                                currentText = currentText,
                                currentCursorPosition = initialBalance.selection.start,
                                newBalanceType = newBalanceType
                            )
                            initialBalance = TextFieldValue(
                                text = result.text,
                                selection = TextRange(result.cursorPosition)
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
                    val result = viewModel.formatBalanceOnTypeChangeSimple(
                        currentText = currentText,
                        currentCursorPosition = initialBalance.selection.start,
                        balanceType = balanceType
                    )
                    initialBalance = TextFieldValue(
                        text = result.text,
                        selection = TextRange(result.cursorPosition)
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
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                                balance = initialBalance.text.toDoubleOrNull() ?: 0.0,
                                balanceType = balanceType
                            )
                        )
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.weight(2f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AromexColors.ButtonBlue(),
                        disabledContainerColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Text(
                        text = "Save ${selectedType.name.lowercase().capitalize()}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.background
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AromexColors.AccentBlue() else Color(0xFFE0E0E0),
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
                        if (isSelected) AromexColors.AccentBlue() else AromexColors.ForegroundWhite()
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
                    color = MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
            .background(MaterialTheme.colorScheme.background)
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
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
