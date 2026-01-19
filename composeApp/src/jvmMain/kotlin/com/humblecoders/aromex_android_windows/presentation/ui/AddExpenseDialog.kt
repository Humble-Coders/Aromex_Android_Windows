package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.Dialog
import com.humblecoders.aromex_android_windows.domain.model.ExpenseCategory
import com.humblecoders.aromex_android_windows.domain.model.PaymentSplit
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ExpenseViewModel
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors

// Helper function for success color based on theme
@Composable
private fun getAromexSuccessColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF4CAF50)
}

@Composable
fun AddExpenseDialog(
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val isLoadingCategories by viewModel.isLoadingCategories.collectAsState()
    val isSaving by viewModel.isSavingTransaction.collectAsState()
    val showExpenseSuccess by viewModel.showExpenseSuccess.collectAsState()
    
    var selectedCategory: ExpenseCategory? by remember { mutableStateOf(null) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    var totalAmount by remember { mutableStateOf("") }
    var cashAmount by remember { mutableStateOf("") }
    var bankAmount by remember { mutableStateOf("") }
    var cardAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Category field position tracking
    val density = LocalDensity.current
    var categoryFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var categoryFieldHeight by remember { mutableStateOf(0.dp) }
    var categoryFieldWidth by remember { mutableStateOf(0.dp) }
    
    // Fetch categories when dialog opens
    LaunchedEffect(Unit) {
        // Always try to fetch when dialog opens (will skip if already loading or loaded)
        viewModel.fetchCategories()
    }
    
    // Also fetch when dropdown opens if categories are empty
    LaunchedEffect(categoryExpanded) {
        if (categoryExpanded && categories.isEmpty() && !isLoadingCategories) {
            viewModel.fetchCategories()
        }
    }
    
    // Close main dialog when saving completes (when saving dialog disappears and saved dialog appears)
    LaunchedEffect(isSaving, showExpenseSuccess) {
        if (!isSaving && showExpenseSuccess) {
            // Close main dialog when saving dialog disappears (in the gap before saved dialog appears)
            onDismiss()
        }
    }

    val total = totalAmount.toDoubleOrNull() ?: 0.0
    val cash = cashAmount.toDoubleOrNull() ?: 0.0
    val bank = bankAmount.toDoubleOrNull() ?: 0.0
    val card = cardAmount.toDoubleOrNull() ?: 0.0
    val splitTotal = cash + bank + card
    val matchesTotal = total > 0 && kotlin.math.abs(splitTotal - total) < 0.01
    
    // Validation: category, totalAmount, and payment split are mandatory
    val isCategoryValid = selectedCategory != null
    val isTotalAmountValid = totalAmount.isNotBlank() && total > 0
    val canSave = isCategoryValid && isTotalAmountValid && matchesTotal

    Dialog(onDismissRequest = {}) {
        Box(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    rootPosition = coordinates.positionInRoot()
                }
        ) {
        Card(
            modifier = Modifier
                .width(640.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Add Expense",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Category
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val selected = selectedCategory // Store in local variable for smart cast
                    val displayValue = selected?.category ?: categorySearchQuery
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                categoryFieldPosition = coordinates.positionInRoot()
                                categoryFieldHeight = with(density) { coordinates.size.height.toDp() }
                                categoryFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                categorySearchQuery = newValue
                                // Clear selection if user types something different
                                if (selected != null && newValue != selected.category) {
                                    selectedCategory = null
                                }
                                categoryExpanded = true
                            },
                            placeholder = {
                                if (selected == null) {
                                    Text(
                                        text = "Search category...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    // Open dropdown when field gains focus (clicked)
                                    if (focusState.isFocused && !categoryExpanded) {
                                        categoryExpanded = true
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(10.dp),
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                                    Icon(
                                        imageVector = if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (categoryExpanded) "Hide" else "Show"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        // Overlay content matching dropdown layout exactly - only show when category is selected
                        if (selected != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .padding(start = 16.dp, end = 48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Category name text (no role pill for categories)
                                Text(
                                    text = selected.category,
                                    fontSize = 14.sp,
                                    color = if (isDarkTheme) Color.White else Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a category for this expense",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Total Amount
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Total Amount *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { input ->
                            totalAmount = input.filter { it.isDigit() || it == '.' }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        singleLine = true,
                        leadingIcon = {
                            Text(
                                text = "$",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
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

                // Payment Split
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment Split *",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (matchesTotal) "Matches total" else "Does not match total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (matchesTotal) getAromexSuccessColor(isDarkTheme) else MaterialTheme.colorScheme.error
                        )
                    }

                    ExpenseAmountRow(
                        label = "Cash",
                        dotColor = getAromexSuccessColor(isDarkTheme),
                        value = cashAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { cashAmount = it }
                    )
                    ExpenseAmountRow(
                        label = "Bank",
                        dotColor = Color(0xFF2196F3),
                        value = bankAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { bankAmount = it }
                    )
                    ExpenseAmountRow(
                        label = "Credit Card",
                        dotColor = Color(0xFF9C27B0),
                        value = cardAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { cardAmount = it }
                    )
                }

                // Notes
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Notes (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        placeholder = {
                            Text(
                                text = "Add notes about this expense",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
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

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (canSave && !isSaving && selectedCategory != null) {
                                viewModel.saveExpenseTransaction(
                                    categoryId = selectedCategory!!.id,
                                    categoryName = selectedCategory!!.category,
                                    totalAmount = total,
                                    paymentSplit = PaymentSplit(
                                        bank = bank,
                                        cash = cash,
                                        creditCard = card
                                    ),
                                    notes = notes
                                )
                            }
                        },
                        enabled = canSave && !isSaving,
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AromexColors.ButtonBlue()
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saving...",
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Save Expense",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Category Dropdown - Using reusable composable (outside Column so it can overflow)
        SearchableDropdown(
            items = categories,
            selectedItem = selectedCategory,
            searchQuery = categorySearchQuery,
            expanded = categoryExpanded,
            onItemSelected = { category ->
                selectedCategory = category
                categorySearchQuery = category.category
                categoryExpanded = false
            },
            onSearchQueryChange = { newValue ->
                categorySearchQuery = newValue
                if (selectedCategory != null && newValue != selectedCategory?.category) {
                    selectedCategory = null
                }
            },
            onExpandedChange = { expanded ->
                categoryExpanded = expanded
                // Fetch categories when dropdown opens if list is empty
                if (expanded && categories.isEmpty() && !isLoadingCategories) {
                    viewModel.fetchCategories()
                }
            },
            onAddNew = { searchQuery, selectItem ->
                viewModel.addCategory(searchQuery) { createdCategory ->
                    selectItem(createdCategory)
                }
                categoryExpanded = false
            },
            fieldPosition = categoryFieldPosition,
            fieldHeight = categoryFieldHeight,
            fieldWidth = categoryFieldWidth,
            rootPosition = rootPosition,
            density = density,
            isDarkTheme = isDarkTheme,
            typeFilter = null,
            placeholder = "Search category...",
            getItemDisplayName = { it.category },
            getItemId = { it.id },
            getItemType = null,
            showRolePill = false
        )
        }
    }
}

@Composable
fun ExpenseAmountRow(
    label: String,
    dotColor: Color,
    value: String,
    isDarkTheme: Boolean,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(dotColor, shape = RoundedCornerShape(50))
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input.filter { it.isDigit() || it == '.' })
            },
            modifier = Modifier
                .widthIn(min = 140.dp)
                .height(60.dp),
            singleLine = true,
            leadingIcon = {
                Text(
                    text = "$",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
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