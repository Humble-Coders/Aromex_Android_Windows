package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme
import com.humblecoders.aromex_android_windows.ui.theme.getAromexSuccessColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntityDialog(
    onDismiss: () -> Unit,
    onSave: (Entity) -> Unit,
    viewModel: com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel,
    isDarkTheme: Boolean = false,
    initialName: String = "",
    initialType: EntityType? = null
) {
    var selectedType by remember { mutableStateOf(initialType ?: EntityType.CUSTOMER) }
    var name by remember { mutableStateOf(initialName) }
    
    // Update values when dialog is shown with new initial values
    LaunchedEffect(initialName, initialType) {
        if (initialName.isNotEmpty()) {
            name = initialName
        }
        if (initialType != null) {
            selectedType = initialType
        }
    }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf(TextFieldValue("")) }
    var balanceType by remember { mutableStateOf(BalanceType.TO_RECEIVE) }

    val nameFieldFocusRequester = remember { FocusRequester() }
    
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
                .width(800.dp)
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
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
                
                // Type Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Type:",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TypeRadioSelector(
                        selected = selectedType,
                        onSelect = { selectedType = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Auto-focus the name field when dialog appears
                LaunchedEffect(Unit) {
                    nameFieldFocusRequester.requestFocus()
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Name",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = " *",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Enter name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 36.dp)
                                .focusRequester(nameFieldFocusRequester),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                errorContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                errorBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                errorTextColor = if (isDarkTheme) Color.White else Color.Black,
                                errorCursorColor = MaterialTheme.colorScheme.onSurface
                            ),
                            isError = name.isBlank()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Phone",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Email",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Enter email") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Address",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Enter address") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Notes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Enter notes") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                    minLines = 3,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Initial Balance",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))

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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    ),
                    trailingIcon = {
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
                )

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

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(8.dp),
                        interactionSource = remember { MutableInteractionSource() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    val isSaveEnabled = name.isNotBlank()
                    Button(
                        onClick = {
                            if (isSaveEnabled) {
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
                            }
                        },
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                            .pointerHoverIcon(PointerIcon.Hand),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFFCCCCCC)
                        ),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Text(
                            text = "Save ${selectedType.name.lowercase().capitalize()}",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeRadioSelector(
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
fun TypePill(
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
                color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .pointerHoverIcon(PointerIcon.Hand)
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
                        if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
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
fun BalanceTypeSelector(
    selected: BalanceType,
    onSelect: (BalanceType) -> Unit
) {
    Row(
        modifier = Modifier.padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BalanceTypeOption(
            text = "To Receive",
            isSelected = selected == BalanceType.TO_RECEIVE,
            onClick = { onSelect(BalanceType.TO_RECEIVE) },
            color = getAromexSuccessColor()
        )
        BalanceTypeOption(
            text = "To Give",
            isSelected = selected == BalanceType.TO_GIVE,
            onClick = { onSelect(BalanceType.TO_GIVE) },
            color = Color(0xFFE57373),
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
fun BalanceTypeOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) color else Color(0xFFFAFAFA))
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else Color(0xFFE8E8E8),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .pointerHoverIcon(PointerIcon.Hand)
            .height(36.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF424242),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
