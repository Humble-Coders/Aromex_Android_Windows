package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors

@Composable
fun WindowsHomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit = {}
) {
    var sidebarExpanded by remember { mutableStateOf(true) }
    var showAddEntityDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var lastSavedType by remember { mutableStateOf<EntityType?>(null) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading, isSaving, error) {
        if (isSaving && !isLoading) {
            if (error == null) {
                // Close add entity dialog immediately when showing success
                showAddEntityDialog = false
                // Wait for exit animation to complete before showing success
                kotlinx.coroutines.delay(300)
                showSuccess = true
            }
            isSaving = false
        }
    }
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(2500) // Show success dialog for 2.5 seconds
            showSuccess = false
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            expanded = sidebarExpanded,
            onToggle = { sidebarExpanded = !sidebarExpanded },
            onNavigate = onNavigate
        )

        // Main Content
        MainContent(
            viewModel = viewModel,
            onAddEntityClick = { showAddEntityDialog = true },
            modifier = Modifier
                .fillMaxSize()
                .background(AromexColors.BackgroundGrey)
        )
    }

    if (showAddEntityDialog) {
        AddEntityDialog(
            onDismiss = { showAddEntityDialog = false },
            onSave = { entity ->
                lastSavedType = entity.type
                isSaving = true
                viewModel.addEntity(entity)
            }
        )
    }

    if (isSaving || showSuccess) {
        val typeLabel = when (lastSavedType) {
            EntityType.CUSTOMER -> "Customer"
            EntityType.SUPPLIER -> "Supplier"
            EntityType.MIDDLEMAN -> "Middleman"
            null -> "Entity"
        }
        Dialog(onDismissRequest = {}) {
            Box(contentAlignment = Alignment.Center) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AromexColors.ForegroundWhite)
            ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSaving,
                            enter = fadeIn(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.8f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Saving $typeLabel...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AromexColors.TextDark
                                )
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showSuccess,
                            enter = fadeIn(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.8f,
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(200, easing = FastOutSlowInEasing)
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "$typeLabel Added Successfully!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AromexColors.TextDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Sidebar(
    expanded: Boolean,
    onToggle: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val menuItems = listOf(
        "Home" to Icons.Default.Home,
        "Transactions" to Icons.Default.SwapHoriz,
        "Purchase" to Icons.Default.ShoppingCart,
        "Sales" to Icons.AutoMirrored.Filled.TrendingUp,
        "Profiles" to Icons.Default.People,
        "Inventory" to Icons.Default.Inventory,
        "Balance Report" to Icons.Default.Assessment,
        "Histories" to Icons.Default.History,
        "Statistics" to Icons.Default.BarChart
    )

    var selectedItem by remember { mutableStateOf("Home") }

    Column(
        modifier = Modifier
            .width(if (expanded) 250.dp else 70.dp)
            .fillMaxHeight()
            .background(AromexColors.PrimaryBlue)
            .padding(16.dp)
    ) {
        // Logo and Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (expanded) {
                Text(
                    text = "AROMEX",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onToggle,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.MenuOpen else Icons.Default.Menu,
                    contentDescription = "Toggle Menu",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu Items
        menuItems.forEach { (item, icon) ->
            MenuItem(
                text = item,
                icon = icon,
                isSelected = selectedItem == item,
                expanded = expanded,
                onClick = {
                    selectedItem = item
                    onNavigate(item)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AromexColors.SelectedBlue else Color.Transparent)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        if (expanded) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun MainContent(
    viewModel: HomeViewModel,
    onAddEntityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountBalance by viewModel.accountBalance.collectAsState()
    val debtOverview by viewModel.debtOverview.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingBalanceType by remember { mutableStateOf<String?>(null) }
    var editingCurrentAmount by remember { mutableStateOf(0.0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Home",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = AromexColors.TextDark,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Financial Overview Section
        Text(
            text = "Financial Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AromexColors.TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Account Balances Card
            AccountBalancesCard(
                accountBalance = accountBalance,
                onEditClick = { balanceType, currentAmount ->
                    editingBalanceType = balanceType
                    editingCurrentAmount = currentAmount
                    showEditDialog = true
                },
                modifier = Modifier.weight(1f)
            )

            // Debt Overview Card
            DebtOverviewCard(
                debtOverview = debtOverview,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions Section
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AromexColors.TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                text = "Add Entity",
                icon = Icons.Default.PersonAdd,
                onClick = onAddEntityClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                text = "Add Product",
                icon = Icons.Default.AddBox,
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                text = "Add Expense",
                icon = Icons.Default.RemoveCircle,
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Edit Balance Dialog
    if (showEditDialog && editingBalanceType != null) {
        EditBalanceDialog(
            balanceType = editingBalanceType!!,
            currentAmount = editingCurrentAmount,
            onDismiss = { showEditDialog = false },
            onSave = { newAmount ->
                viewModel.updateSingleBalance(editingBalanceType!!, newAmount)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun AccountBalancesCard(
    accountBalance: com.humblecoders.aromex_android_windows.domain.model.AccountBalance,
    onEditClick: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AromexColors.ForegroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account Balances",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AromexColors.TextDark
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = AromexColors.TextGrey
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BalanceItem(
                label = "Bank Balance",
                amount = accountBalance.bankBalance,
                icon = Icons.Default.AccountBalance,
                onEditClick = { onEditClick("bank", accountBalance.bankBalance) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BalanceItem(
                label = "Cash",
                amount = accountBalance.cash,
                icon = Icons.Default.Money,
                onEditClick = { onEditClick("cash", accountBalance.cash) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BalanceItem(
                label = "Credit Card",
                amount = accountBalance.creditCard,
                icon = Icons.Default.CreditCard,
                onEditClick = { onEditClick("creditCard", accountBalance.creditCard) }
            )
        }
    }
}

@Composable
fun BalanceItem(
    label: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onEditClick: () -> Unit
) {
    val isNegative = amount < 0.0
    val rowColor = if (isNegative) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
    val accentColor = if (isNegative) Color(0xFFD32F2F) else Color(0xFF4CAF50)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(rowColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = AromexColors.TextDark
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = accentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = AromexColors.TextGrey,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(
                        onClick = onEditClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
            )
        }
    }
}

@Composable
fun DebtOverviewCard(
    debtOverview: com.humblecoders.aromex_android_windows.domain.model.DebtOverview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AromexColors.ForegroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debt Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AromexColors.TextDark
                )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = AromexColors.TextGrey
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DebtItem(
                label = "Total Owed",
                amount = debtOverview.totalOwed,
                color = Color(0xFFFFEBEE),
                textColor = Color(0xFFD32F2F),
                icon = Icons.Default.ArrowUpward
            )
            Spacer(modifier = Modifier.height(12.dp))
            DebtItem(
                label = "Total Due to Me",
                amount = debtOverview.totalDueToMe,
                color = Color(0xFFE8F5E9),
                textColor = Color(0xFF4CAF50),
                icon = Icons.Default.ArrowDownward
            )
        }
    }
}

@Composable
fun DebtItem(
    label: String,
    amount: Double,
    color: Color,
    textColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = AromexColors.TextDark
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .pointerHoverIcon(PointerIcon.Hand),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AromexColors.ForegroundWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AromexColors.TextDark
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AromexColors.TextGrey
            )
        }
    }
}

@Composable
fun EditBalanceDialog(
    balanceType: String,
    currentAmount: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var newAmount by remember { mutableStateOf(TextFieldValue(currentAmount.toString())) }
    val balanceTypeLabel = when (balanceType) {
        "bank" -> "Bank Balance"
        "cash" -> "Cash"
        "creditCard" -> "Credit Card"
        else -> balanceType
    }
    
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(500.dp)
                .padding(16.dp)
                .graphicsLayer {
                    translationY = slideOffset.value
                    this.alpha = alpha.value
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AromexColors.ForegroundWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = AromexColors.TextGrey,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Edit $balanceTypeLabel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AromexColors.TextDark
                    )
                }
                
                Text(
                    text = "Update your financial information",
                    fontSize = 14.sp,
                    color = AromexColors.TextGrey
                )

                // Current Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AromexColors.TextDark
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = AromexColors.BackgroundGrey)
                    ) {
                        Text(
                            text = "$${String.format("%.2f", currentAmount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AromexColors.TextDark,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // New Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "New Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AromexColors.TextDark
                    )
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newValue ->
                            val oldText = newAmount.text
                            val oldSelection = newAmount.selection

                            var filtered = newValue.text.filter { it.isDigit() || it == '.' || it == '-' }
                            val decimalCount = filtered.count { it == '.' }
                            if (decimalCount > 1) {
                                val firstDecimalIndex = filtered.indexOf('.')
                                filtered = filtered.substring(0, firstDecimalIndex + 1) +
                                        filtered.substring(firstDecimalIndex + 1).replace(".", "")
                            }

                            if (balanceType == "creditCard") {
                                val numericPart = filtered.replace("-", "")
                                val (resultText, cursorPos) =
                                    if (numericPart.isEmpty() || numericPart == ".") {
                                        Pair(numericPart, newValue.selection.start.coerceIn(0, numericPart.length))
                                    } else {
                                        val finalText =
                                            if (filtered.startsWith("-") &&
                                                filtered.substring(1) == numericPart &&
                                                numericPart.all { it.isDigit() || it == '.' }) {
                                                filtered
                                            } else {
                                                "-$numericPart"
                                            }
                                        val newCursor =
                                            if (filtered.startsWith("-") && filtered == finalText) {
                                                newValue.selection.start.coerceIn(0, finalText.length)
                                            } else {
                                                val cursorAdjustment =
                                                    if (!oldText.startsWith("-") && finalText.startsWith("-")) 1
                                                    else if (oldText.startsWith("-") && !finalText.startsWith("-")) -1
                                                    else 0
                                                (newValue.selection.start + cursorAdjustment).coerceIn(0, finalText.length)
                                            }
                                        Pair(finalText, newCursor)
                                    }
                                newAmount = TextFieldValue(text = resultText, selection = androidx.compose.ui.text.TextRange(cursorPos))
                            } else {
                                val resultText = filtered
                                val cursorPos = newValue.selection.start.coerceIn(0, resultText.length)
                                newAmount = TextFieldValue(text = resultText, selection = androidx.compose.ui.text.TextRange(cursorPos))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Enter amount", color = AromexColors.TextGrey) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AromexColors.ForegroundWhite,
                            unfocusedContainerColor = AromexColors.ForegroundWhite,
                            focusedBorderColor = AromexColors.TextGrey,
                            unfocusedBorderColor = AromexColors.TextGrey
                        ),
                        supportingText = {
                            Text(
                                text = "Enter the new amount for this account",
                                fontSize = 12.sp,
                                color = AromexColors.TextGrey
                            )
                        }
                    )
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        interactionSource = remember { MutableInteractionSource() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AromexColors.TextDark
                        )
                    ) {
                        Text("Cancel", color = AromexColors.TextDark)
                    }
                    Button(
                        onClick = {
                            val parsed = newAmount.text.toDoubleOrNull() ?: return@Button
                            val amount = if (balanceType == "creditCard") -kotlin.math.abs(parsed) else parsed
                            onSave(amount)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .pointerHoverIcon(PointerIcon.Hand),
                        colors = ButtonDefaults.buttonColors(containerColor = AromexColors.ButtonBlue),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", color = Color.White)
                    }
                }
            }
        }
    }
}
