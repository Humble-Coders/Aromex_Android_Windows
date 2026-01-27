package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
    val isDark = isSystemInDarkTheme()
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
            modifier = Modifier
                .widthIn(min = 120.dp)
                .defaultMinSize(minHeight = 64.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Text(
                    text = "$",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDark)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = if (isDark)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isDark)
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = if (isDark) Color.White else Color.Black
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
    
    // Focus requesters for keyboard navigation
    val categoryFocusRequester = remember { FocusRequester() }
    val totalAmountFocusRequester = remember { FocusRequester() }
    
    // Discard dialog state
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    
    // Check if any fields have content
    val hasChanges = selectedCategory != null || totalAmount.isNotBlank() || 
            cashAmount.isNotBlank() || bankAmount.isNotBlank() || 
            cardAmount.isNotBlank() || notes.isNotBlank()
    
    // Category field position tracking
    var boxPosition by remember { mutableStateOf(Offset.Zero) }
    var categoryFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var categoryFieldHeight by remember { mutableStateOf(0.dp) }
    var categoryFieldWidth by remember { mutableStateOf(0.dp) }
    
    // Fetch categories when bottom sheet opens and focus category field
    LaunchedEffect(Unit) {
        // Always try to fetch when bottom sheet opens (will skip if already loading or loaded)
        viewModel.fetchCategories()
        // Focus category field when screen opens
        kotlinx.coroutines.delay(100)
        categoryFocusRequester.requestFocus()
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

    // Root position tracking for dropdown
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Scroll state for tracking scroll position
    val scrollState = rememberScrollState()
    
    // Track scroll position to show/hide top bar - show when scrolled past initial header
    val showTopBar by remember {
        derivedStateOf {
            scrollState.value > 80 // Show top bar when scrolled more than 80 pixels
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Top Bar with Cancel, Title, and Save - Title animates on scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel Button - Always visible
                TextButton(
                    onClick = {
                        if (hasChanges) {
                            showDiscardDialog = true
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Text(
                        text = "Cancel",
                        color = if (isSystemInDarkTheme()) {
                            Color(0xFF6EA8FF)
                        } else {
                            Color(0xFF2563EB)
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Title - Animated visibility on scroll
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    @Suppress("RemoveRedundantQualifierName")
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showTopBar,
                        enter = slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(
                            animationSpec = tween(300)
                        ),
                        exit = slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(300)
                        ) + fadeOut(
                            animationSpec = tween(300)
                        )
                    ) {
                        Text(
                            text = "Add Expense",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Save Button - Always visible
                TextButton(
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
                    enabled = canSave && !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = if (isSystemInDarkTheme()) {
                                Color(0xFF6EA8FF)
                            } else {
                                Color(0xFF2563EB)
                            },
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save",
                            color = if (canSave) {
                                if (isSystemInDarkTheme()) {
                                    Color(0xFF6EA8FF)
                                } else {
                                    Color(0xFF2563EB)
                                }
                            } else {
                                if (isSystemInDarkTheme()) {
                                    Color(0xFF9AA4B2).copy(alpha = 0.6f)
                                } else {
                                    Color(0xFF94A3B8)
                                }
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            // Divider - Always visible
            Divider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    ) { paddingValues ->
        val density = LocalDensity.current
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .windowInsetsPadding(WindowInsets.ime)
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coordinates ->
                        rootPosition = coordinates.positionInRoot()
                        boxPosition = coordinates.positionInRoot()
                    }
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Title and Subtitle
                Text(
                    text = "Add Expense",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Record a new expense transaction",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Category Field
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    val selected = selectedCategory
                    val displayValue = (selected?.category ?: categorySearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    val density = LocalDensity.current
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
                                if (selected != null && newValue != selected.category) {
                                    selectedCategory = null
                                }
                                categoryExpanded = true
                            },
                            placeholder = {
                                if (selected == null) {
                                    Text(
                                        text = "Choose an option",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp)
                                .focusRequester(categoryFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !categoryExpanded) {
                                        categoryExpanded = true
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { totalAmountFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                                    Icon(
                                        imageVector = if (categoryExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (categoryExpanded) "Hide" else "Show"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Category Dropdown - Inside Column flow to push content down
                    if (categoryExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val density = LocalDensity.current
                        var dropdownContainerPosition by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    dropdownContainerPosition = coordinates.positionInRoot()
                                }
                        ) {
                            val adjustedFieldPosition = Offset(
                                x = dropdownContainerPosition.x,
                                y = dropdownContainerPosition.y - with(density) { categoryFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = categories,
                                selectedItem = selectedCategory,
                                searchQuery = categorySearchQuery,
                                expanded = categoryExpanded,
                                onItemSelected = { category ->
                                    selectedCategory = category
                                    categorySearchQuery = category.category.trim()
                                    categoryExpanded = false
                                    // Move focus to total amount field after selection
                                    totalAmountFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    categorySearchQuery = newValue
                                    if (selectedCategory != null && newValue != selectedCategory?.category) {
                                        selectedCategory = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    categoryExpanded = expanded
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
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = categoryFieldHeight,
                                fieldWidth = categoryFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                typeFilter = null,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.category },
                                getItemId = { it.id },
                                getItemType = null,
                                showRolePill = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Total Amount
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total Amount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
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
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { input ->
                            totalAmount = input.filter { it.isDigit() || it == '.' }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .focusRequester(totalAmountFocusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { /* Can move to notes or stay here */ }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Text(
                                text = "$",
                                fontSize = 16.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        placeholder = {
                            Text(
                                text = "0.00",
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
                            cursorColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Payment Split
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Payment Split",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = " *",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        // Indicator on the right side
                        if (total > 0) {
                            val difference = splitTotal - total
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (matchesTotal) {
                                    // Matches total - show green checkmark and text
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = getAromexSuccessColor(isDarkTheme),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Matches total",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = getAromexSuccessColor(isDarkTheme)
                                    )
                                } else if (difference > 0) {
                                    // Over by amount - show error text
                                    Text(
                                        text = "Over by: $${String.format("%.2f", difference)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    // Remaining amount - show orange text
                                    Text(
                                        text = "Remaining: $${String.format("%.2f", kotlin.math.abs(difference))}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFF9800)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    ExpenseAmountRowAndroid(
                        label = "Cash",
                        dotColor = getAromexSuccessColor(isDarkTheme),
                        value = cashAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { cashAmount = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExpenseAmountRowAndroid(
                        label = "Bank",
                        dotColor = Color(0xFF2196F3),
                        value = bankAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { bankAmount = it }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ExpenseAmountRowAndroid(
                        label = "Credit Card",
                        dotColor = Color(0xFF9C27B0),
                        value = cardAmount,
                        isDarkTheme = isDarkTheme,
                        onValueChange = { cardAmount = it }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Notes
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        text = "Notes (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = {
                            Text(
                                text = "Add notes about this expense",
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
                            cursorColor = if (isSystemInDarkTheme()) Color.White else Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Discard Changes?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Are you sure you want to discard your changes?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Discard",
                        color = if (isSystemInDarkTheme()) {
                            Color(0xFFFF8A80)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = if (isSystemInDarkTheme()) {
                            Color(0xFF6EA8FF)
                        } else {
                            Color(0xFF2563EB)
                        }
                    )
                }
            }
        )
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
