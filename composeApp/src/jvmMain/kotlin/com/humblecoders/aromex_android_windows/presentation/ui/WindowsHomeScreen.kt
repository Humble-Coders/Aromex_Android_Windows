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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.animation.core.Animatable
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// Windows-specific helper functions that use the theme state instead of system theme
@Composable
private fun getAromexSuccessColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF4CAF50)
}

@Composable
private fun getAromexSuccessContainerColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
}

@Composable
fun WindowsHomeScreen(
    viewModel: HomeViewModel,
    purchaseViewModel: PurchaseViewModel,
    onNavigate: (String) -> Unit = {},
    isDarkTheme: Boolean = false,
    onThemeToggle: () -> Unit = {}
) {
    var sidebarExpanded by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf("Home") }
    val showAddEntityDialog by viewModel.showAddEntitySheet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSavingEntity by viewModel.isSavingEntity.collectAsState()
    val lastSavedType by viewModel.lastSavedEntityType.collectAsState()
    val showEntitySuccess by viewModel.showEntitySuccess.collectAsState()

    LaunchedEffect(showEntitySuccess) {
        if (showEntitySuccess) {
            kotlinx.coroutines.delay(2500) // Show success dialog for 2.5 seconds
            viewModel.dismissEntitySuccess()
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar
        Sidebar(
            expanded = sidebarExpanded,
            onToggle = { sidebarExpanded = !sidebarExpanded },
            selectedItem = currentScreen,
            onNavigate = { screen ->
                currentScreen = screen
                onNavigate(screen)
            }
        )

        // Main Content - Show different screens based on selection
        when (currentScreen) {
            "Purchase" -> {
                WindowsPurchaseScreen(
                    viewModel = purchaseViewModel,
                    homeViewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
            else -> {
                MainContent(
                    viewModel = viewModel,
                    onAddEntityClick = { viewModel.showAddEntitySheet() },
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = onThemeToggle,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }

    if (showAddEntityDialog) {
        AddEntityDialog(
            onDismiss = { viewModel.dismissAddEntitySheet() },
            onSave = { entity ->
                viewModel.addEntity(entity)
            },
            viewModel = viewModel,
            isDarkTheme = isDarkTheme
        )
    }

    if (isSavingEntity || showEntitySuccess) {
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                            .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSavingEntity,
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
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showEntitySuccess,
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
                                    tint = getAromexSuccessColor(isDarkTheme),
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "$typeLabel Added Successfully!",
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
}

@Composable
fun Sidebar(
    expanded: Boolean,
    onToggle: () -> Unit,
    selectedItem: String,
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

    Column(
        modifier = Modifier
            .width(if (expanded) 250.dp else 70.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.primary)
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
                    imageVector = if (expanded) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
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
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
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
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
fun MainContent(
    viewModel: HomeViewModel,
    onAddEntityClick: () -> Unit,
    isDarkTheme: Boolean = false,
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accountBalance by viewModel.accountBalance.collectAsState()
    val debtOverview by viewModel.debtOverview.collectAsState()

    val showEditDialog by viewModel.showEditBalanceSheet.collectAsState()
    val editingBalanceType by viewModel.editingBalanceType.collectAsState()
    val editingCurrentAmount by viewModel.editingCurrentAmount.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                    contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Financial Overview Section
        Text(
            text = "Financial Overview",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
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
                    viewModel.showEditBalanceSheet(balanceType, currentAmount)
                },
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            // Debt Overview Card
            DebtOverviewCard(
                debtOverview = debtOverview,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions Section
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
                onClick = { /* No logic - UI only */ },
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
            isDarkTheme = isDarkTheme,
            onDismiss = { viewModel.dismissEditBalanceSheet() },
            onSave = { newAmount ->
                viewModel.updateSingleBalance(editingBalanceType!!, newAmount)
                viewModel.dismissEditBalanceSheet()
            }
        )
    }
}

@Composable
fun AccountBalancesCard(
    accountBalance: com.humblecoders.aromex_android_windows.domain.model.AccountBalance,
    onEditClick: (String, Double) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            BalanceItem(
                label = "Bank Balance",
                amount = accountBalance.bankBalance,
                icon = Icons.Default.AccountBalance,
                isDarkTheme = isDarkTheme,
                onEditClick = { onEditClick("bank", accountBalance.bankBalance) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BalanceItem(
                label = "Cash",
                amount = accountBalance.cash,
                icon = Icons.Default.Money,
                isDarkTheme = isDarkTheme,
                onEditClick = { onEditClick("cash", accountBalance.cash) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BalanceItem(
                label = "Credit Card",
                amount = accountBalance.creditCard,
                icon = Icons.Default.CreditCard,
                isDarkTheme = isDarkTheme,
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
    isDarkTheme: Boolean,
    onEditClick: () -> Unit
) {
    val isNegative = amount < 0.0
    val rowColor = if (isNegative) {
        if (isDarkTheme) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFEBEE)
    } else {
        if (isDarkTheme) getAromexSuccessContainerColor(isDarkTheme) else Color(0xFFE8F5E9)
    }
    val textColor = if (isDarkTheme) Color.White else if (isNegative) MaterialTheme.colorScheme.error else getAromexSuccessColor(isDarkTheme)
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
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DebtItem(
                label = "Total Owed",
                amount = debtOverview.totalOwed,
                color = if (isDarkTheme) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFEBEE),
                textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.error,
                icon = Icons.Default.ArrowUpward,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(12.dp))
            DebtItem(
                label = "Total Due to Me",
                amount = debtOverview.totalDueToMe,
                color = if (isDarkTheme) getAromexSuccessContainerColor(isDarkTheme) else Color(0xFFE8F5E9),
                textColor = if (isDarkTheme) Color.White else getAromexSuccessColor(isDarkTheme),
                icon = Icons.Default.ArrowDownward,
                isDarkTheme = isDarkTheme
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDarkTheme: Boolean = false
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
            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
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
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var newAmount by remember { mutableStateOf(TextFieldValue("")) }
    val balanceTypeLabel = when (balanceType) {
        "bank" -> "Bank Balance"
        "cash" -> "Cash"
        "creditCard" -> "Credit Card"
        else -> balanceType
    }
    
    val slideOffset = remember { Animatable(-1000f) }
    val alpha = remember { Animatable(0f) }
    val focusRequester = remember { FocusRequester() }
    
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
            launch {
                kotlinx.coroutines.delay(450) // Wait for dialog animation to complete
                focusRequester.requestFocus()
            }
        }
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .width(500.dp)
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Edit $balanceTypeLabel",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = "Update your financial information",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Current Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                    ) {
                        Text(
                            text = "$${String.format("%.2f", currentAmount)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // New Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "New Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                        ),
                        supportingText = {
                            Text(
                                text = "Enter the new amount for this account",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
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
                        colors = ButtonDefaults.buttonColors(containerColor = AromexColors.ButtonBlue()),
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Changes",
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
