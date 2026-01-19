package com.humblecoders.aromex_android_windows.presentation.ui

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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
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
fun ExpenseAmountRowAndroid(
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
                .size(8.dp)
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
            modifier = Modifier.widthIn(min = 120.dp),
            singleLine = true,
            leadingIcon = {
                Text(
                    text = "$",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onDismiss: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val isLoadingCategories by viewModel.isLoadingCategories.collectAsState()
    val isSaving by viewModel.isSavingTransaction.collectAsState()
    val transactionSaved by viewModel.transactionSaved.collectAsState()
    val showExpenseSuccess by viewModel.showExpenseSuccess.collectAsState()
    
    var selectedCategory: ExpenseCategory? by remember { mutableStateOf(null) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    var totalAmount by remember { mutableStateOf("") }
    var cashAmount by remember { mutableStateOf("") }
    var bankAmount by remember { mutableStateOf("") }
    var cardAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Category field position tracking
    val density = LocalDensity.current
    var boxPosition by remember { mutableStateOf(Offset.Zero) }
    var categoryFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var categoryFieldHeight by remember { mutableStateOf(0.dp) }
    var categoryFieldWidth by remember { mutableStateOf(0.dp) }
    
    // Fetch categories when bottom sheet opens
    LaunchedEffect(Unit) {
        // Always try to fetch when bottom sheet opens (will skip if already loading or loaded)
        viewModel.fetchCategories()
    }
    
    // Also fetch when dropdown opens if categories are empty
    LaunchedEffect(categoryExpanded) {
        if (categoryExpanded && categories.isEmpty() && !isLoadingCategories) {
            viewModel.fetchCategories()
        }
    }
    
    // Close bottom sheet when saving completes (in the gap between saving dialog closing and saved dialog appearing)
    LaunchedEffect(isSaving, showExpenseSuccess) {
        if (!isSaving && showExpenseSuccess && transactionSaved) {
            // Close bottom sheet in the transition period
            viewModel.resetTransactionSaved()
            onDismiss()
        }
    }
    
    // Auto-dismiss success dialog after showing
    LaunchedEffect(showExpenseSuccess) {
        if (showExpenseSuccess) {
            kotlinx.coroutines.delay(1200)
            viewModel.dismissExpenseSuccess()
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
    val isPaymentSplitValid = matchesTotal
    val canSave = isCategoryValid && isTotalAmountValid && isPaymentSplitValid

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    boxPosition = coordinates.positionInRoot()
                }
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .windowInsetsPadding(WindowInsets.ime),
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
                        fontSize = 20.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDismiss) {
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
                val displayValue = if (selected != null) {
                    selected.category
                } else {
                    categorySearchQuery
                }
                Box(
                    modifier = Modifier.fillMaxWidth()
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
                            .onGloballyPositioned { coordinates ->
                                categoryFieldPosition = coordinates.positionInRoot()
                                categoryFieldHeight = with(density) { coordinates.size.height.toDp() }
                                categoryFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
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
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Text(
                            text = "$",
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
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

                ExpenseAmountRowAndroid(
                    label = "Cash",
                    dotColor = getAromexSuccessColor(isDarkTheme),
                    value = cashAmount,
                    isDarkTheme = isDarkTheme,
                    onValueChange = { cashAmount = it }
                )
                ExpenseAmountRowAndroid(
                    label = "Bank",
                    dotColor = Color(0xFF2196F3),
                    value = bankAmount,
                    isDarkTheme = isDarkTheme,
                    onValueChange = { bankAmount = it }
                )
                ExpenseAmountRowAndroid(
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
                        .heightIn(min = 80.dp),
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
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    modifier = Modifier.weight(1f)
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
                    modifier = Modifier.weight(1f),
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
                rootPosition = boxPosition,
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
    
    // Expense Save Confirmation Dialog
    if (isSaving || showExpenseSuccess) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isSaving,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Saving Expense...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = showExpenseSuccess,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = getAromexSuccessColor(isDarkTheme),
                            modifier = Modifier.size(40.dp)
        )
        Text(
                            text = "Expense Added Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
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