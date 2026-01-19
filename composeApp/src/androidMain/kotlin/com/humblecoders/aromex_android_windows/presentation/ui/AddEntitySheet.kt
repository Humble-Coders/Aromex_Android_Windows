package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel

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

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(onDismissRequest = onDismiss,sheetState= sheetState,  containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .fillMaxHeight(1f)


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
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable { onDismiss() }
                )

                // Title (stronger emphasis)
                Text(
                    text = "Add Entity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Save (primary action)
                Text(
                    text = "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        name.isNotBlank() ->
                            MaterialTheme.colorScheme.primary
                        else ->
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    },
                    modifier = Modifier.clickable(enabled = name.isNotBlank()) {
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
                )
            }

            // Stronger but still subtle divider
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )


            
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // ⭐ makes only this part scroll
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
            )
            {

                val isDark = isSystemInDarkTheme()
            
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

                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        text = "Enter name",
                        color = if (isSystemInDarkTheme())
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    unfocusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    cursorColor = if (isDark)
                        Color.White
                    else
                        Color.Black
                )


            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Initial Balance",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // 🔑 fixed field height
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // ⌨️ Amount input (LEFT)
                    BasicTextField(
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
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            if (initialBalance.text.isEmpty()) {
                                Text(
                                    text = "Enter amount",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )

                    // 🔘 Balance type pills (RIGHT — INLINE)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isDarkTheme = isSystemInDarkTheme()

                        val receiveColor = if (isDarkTheme) Color(0xFF43A047) else Color(0xFF4CAF50)
                        val giveColor = if (isDarkTheme) Color(0xFFEF9A9A) else Color(0xFFE57373)

                        InlineBalanceChip(
                            text = "To Receive",
                            selected = balanceType == BalanceType.TO_RECEIVE,
                            color = receiveColor
                        ) {
                            if (balanceType != BalanceType.TO_RECEIVE) {
                                balanceType = BalanceType.TO_RECEIVE

                                val result = viewModel.formatBalanceOnTypeChange(
                                    currentText = initialBalance.text,
                                    currentCursorPosition = initialBalance.selection.start,
                                    newBalanceType = BalanceType.TO_RECEIVE
                                )

                                initialBalance = TextFieldValue(
                                    text = result.text,
                                    selection = TextRange(result.cursorPosition)
                                )
                            }
                        }


                        InlineBalanceChip(
                            text = "To Give",
                            selected = balanceType == BalanceType.TO_GIVE,
                            color = giveColor
                        ) {
                            if (balanceType != BalanceType.TO_GIVE) {
                                balanceType = BalanceType.TO_GIVE

                                val result = viewModel.formatBalanceOnTypeChange(
                                    currentText = initialBalance.text,
                                    currentCursorPosition = initialBalance.selection.start,
                                    newBalanceType = BalanceType.TO_GIVE
                                )

                                initialBalance = TextFieldValue(
                                    text = result.text,
                                    selection = TextRange(result.cursorPosition)
                                )
                            }
                        }

                    }
                }
            }
            
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

                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        text = "Enter Phone",
                        color = if (isSystemInDarkTheme())
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    unfocusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    cursorColor = if (isDark)
                        Color.White
                    else
                        Color.Black
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

                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        text = "Enter Email",
                        color = if (isSystemInDarkTheme())
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    unfocusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    cursorColor = if (isDark)
                        Color.White
                    else
                        Color.Black
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

                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),

                placeholder = {
                    Text(
                        text = "Enter Address",
                        color = if (isSystemInDarkTheme())
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    unfocusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = if (isDark)
                        Color.White
                    else
                        Color.Black
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

                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 88.dp),
               minLines = 3,
                shape = RoundedCornerShape(12.dp),
                placeholder = {
                    Text(
                        text = "Notes",
                        color = if (isSystemInDarkTheme())
                            Color.White.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    unfocusedContainerColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.surface,

                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isSystemInDarkTheme())
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,

                    cursorColor = if (isDark)
                        Color.White
                    else
                        Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))







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
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                OutlinedButton(
//                    onClick = onDismiss,
//                    modifier = Modifier.weight(1f).height(50.dp),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    Text(
//                        text = "Cancel",
//                        color = MaterialTheme.colorScheme.onSurface,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                }
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TypePill(
            text = "Customer",
            isSelected = selected == EntityType.CUSTOMER,
            color = Color(0xFF1E88E5), // 🔵 blue
            onClick = { onSelect(EntityType.CUSTOMER) }
        )

        TypePill(
            text = "Supplier",
            isSelected = selected == EntityType.SUPPLIER,
            color = Color(0xFF43A047), // 🟢 green
            onClick = { onSelect(EntityType.SUPPLIER) }
        )

        TypePill(
            text = "Middleman",
            isSelected = selected == EntityType.MIDDLEMAN,
            color = Color(0xFFF57C00), // 🟠 orange
            onClick = { onSelect(EntityType.MIDDLEMAN) }
        )
    }
}



@Composable
private fun TypePill(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = when {
        isSelected && isDarkTheme -> color.copy(alpha = 0.25f)
        isSelected -> color.copy(alpha = 0.12f)
        isDarkTheme -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color(0xFFF2F2F2)
    }

    val borderColor = when {
        isSelected -> color
        isDarkTheme -> Color.White.copy(alpha = 0.15f)
        else -> Color(0xFFE0E0E0)
    }

    Box(
        modifier = Modifier
            .height(36.dp) // ✅ consistent pill height
            .width(IntrinsicSize.Min)
            .clip(RoundedCornerShape(18.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) AromexColors.AccentBlue() else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // radio indicator
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
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected)
                    color
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                softWrap = false

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
@Composable
private fun InlineBalanceChip(
    text: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (selected) color
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (selected) color else color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected)
                Color.White
            else
                color
        )
    }
}


