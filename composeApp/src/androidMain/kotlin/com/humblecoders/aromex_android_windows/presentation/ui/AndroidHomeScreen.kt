package com.humblecoders.aromex_android_windows.presentation.ui

import android.R.attr.maxHeight
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme
import com.humblecoders.aromex_android_windows.ui.theme.getAromexSuccessColor
import com.humblecoders.aromex_android_windows.ui.theme.getAromexSuccessContainerColor
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.IntOffset
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ProfilesViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ExpenseViewModel
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AndroidHomeScreen(
    viewModel: HomeViewModel,
    purchaseViewModel: PurchaseViewModel,
    profilesViewModel: ProfilesViewModel,
    expenseViewModel: ExpenseViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("Home") }
    val showAddEntitySheet by viewModel.showAddEntitySheet.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSavingEntity by viewModel.isSavingEntity.collectAsState()
    val lastSavedType by viewModel.lastSavedEntityType.collectAsState()
    val showEntitySuccess by viewModel.showEntitySuccess.collectAsState()
    
    LaunchedEffect(showEntitySuccess) {
        if (showEntitySuccess) {
            kotlinx.coroutines.delay(1200)
            viewModel.dismissEntitySuccess()
        }
    }
    ;
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { route ->
                    currentScreen = route
                    onNavigate(route)
                    scope.launch { drawerState.close() }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        // Show different screens based on selection
        when (currentScreen) {
            "Purchase" -> {
                AndroidPurchaseScreen(
                    viewModel = purchaseViewModel,
                    homeViewModel = viewModel,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                )
            }
            "Profiles" -> {
                AndroidProfilesScreen(
                    viewModel = profilesViewModel,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .windowInsetsPadding(WindowInsets.systemBars)
                )
            }
            else -> {
                MainContent(
                    viewModel = viewModel,
                    expenseViewModel = expenseViewModel,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onAddEntityClick = { viewModel.showAddEntitySheet() },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                )
            }
        }
        
        if (showAddEntitySheet) {
            AddEntitySheet(
                onDismiss = { viewModel.dismissAddEntitySheet() },
                onSave = { entity ->
                    viewModel.addEntity(entity)
                },
                viewModel = viewModel
            )
        }
    }

    if (isSavingEntity || showEntitySuccess) {
        val typeLabel = when (lastSavedType) {
            EntityType.CUSTOMER -> "Customer"
            EntityType.SUPPLIER -> "Supplier"
            EntityType.MIDDLEMAN -> "Middleman"
            null -> "Entity"
        }

        Dialog(
            onDismissRequest = {},
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                // ================= SAVING =================
                AnimatedVisibility(
                    visible = isSavingEntity,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )

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

                // ================= SUCCESS =================
                AnimatedVisibility(
                    visible = showEntitySuccess,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = getAromexSuccessColor(), // already good
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

@Composable
fun DrawerContent(
    onNavigate: (String) -> Unit,
    onClose: () -> Unit
) {
    val menuItems = listOf(
        "Home" to Icons.Default.Home,
        "Transactions" to Icons.Default.SwapHoriz,
        "Purchase" to Icons.Default.ShoppingCart,
        "Sales" to Icons.Default.TrendingUp,
        "Profiles" to Icons.Default.People,
        "Inventory" to Icons.Default.Inventory,
        "Balance Report" to Icons.Default.Assessment,
        "Histories" to Icons.Default.History,
        "Scanner" to Icons.Default.CameraAlt,
        "Statistics" to Icons.Default.BarChart
    )

    var selectedItem by remember { mutableStateOf("Home") }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(MaterialTheme.colorScheme.primary)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AROMEX",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TextButton(onClick = onClose) {
                Text(
                    text = "Done",
                    color = Color(0xFF64B5F6),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = "Menu",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Divider(color = Color.White.copy(alpha = 0.3f))

        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        menuItems.forEach { (item, icon) ->
            MenuItem(
                text = item,
                icon = icon,
                isSelected = selectedItem == item,
                onClick = {
                    selectedItem = item
                    onNavigate(item)
                },
                isLocked = item == "Statistics"
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun MenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLocked: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick, enabled = !isLocked)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
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
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(20.dp)
            )
        }
        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MainContent(
    viewModel: HomeViewModel,
    expenseViewModel: ExpenseViewModel,
    onMenuClick: () -> Unit,
    onAddEntityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountBalance by viewModel.accountBalance.collectAsState()
    val debtOverview by viewModel.debtOverview.collectAsState()

    val showEditSheet by viewModel.showEditBalanceSheet.collectAsState()
    val editingBalanceType by viewModel.editingBalanceType.collectAsState()
    val editingCurrentAmount by viewModel.editingCurrentAmount.collectAsState()

    var showAddExpenseSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(48.dp)) // keeps title centered
            }
        }
    ) { paddingValues ->

        // 📜 Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Financial Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AromexColors.TextDark(),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            AccountBalancesCard(
                accountBalance = accountBalance,
                onEditClick = { balanceType, currentAmount ->
                    viewModel.showEditBalanceSheet(balanceType, currentAmount)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            DebtOverviewCard(
                debtOverview = debtOverview,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            QuickActionButton(
                text = "Add Entity",
                icon = Icons.Default.PersonAdd,
                onClick = onAddEntityClick,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickActionButton(
                text = "Add Product",
                icon = Icons.Default.AddBox,
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

        QuickActionButton(
            text = "Add Expense",
            icon = Icons.Default.RemoveCircle,
            onClick = { showAddExpenseSheet = true },
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Bottom Sheet stays outside Scaffold content
    if (showEditSheet && editingBalanceType != null) {
        EditBalanceBottomSheet(
            viewModel = viewModel,
            balanceType = editingBalanceType!!,
            currentAmount = editingCurrentAmount,
            onDismiss = { 
                viewModel.dismissEditBalanceSheet()
            }
        )
    }

    // Add Expense Bottom Sheet
    if (showAddExpenseSheet) {
        AddExpenseBottomSheet(
            viewModel = expenseViewModel,
            onDismiss = { showAddExpenseSheet = false }
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
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account Balances",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            BalanceItem(
                label = "Bank Balance",
                amount = accountBalance.bankBalance,
                icon = Icons.Default.AccountBalance,
                onEditClick = { onEditClick("bank", accountBalance.bankBalance) }
            )
            Spacer(modifier = Modifier.height(10.dp))
            BalanceItem(
                label = "Cash",
                amount = accountBalance.cash,
                icon = Icons.Default.Money,
                onEditClick = { onEditClick("cash", accountBalance.cash) }
            )
            Spacer(modifier = Modifier.height(10.dp))
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
    val isDarkTheme = isSystemInDarkTheme()

    val primaryTextColor = if (isDarkTheme) {
        Color.White
    } else {
        if (isNegative) Color(0xFFD32F2F) else Color(0xFF388E3C)
    }

    val secondaryTextColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val bgColor = if (isNegative) {
        if (isDarkTheme)
            MaterialTheme.colorScheme.errorContainer
        else
            Color(0xFFFFEBEE) // light red bg
    } else {
        if (isDarkTheme)
            getAromexSuccessContainerColor()
        else
            Color(0xFFE8F5E9) // light green bg
    }

    val iconBg = if (isNegative) {
        if (isDarkTheme)
            Color(0xFFB3261E) // brighter red icon bg
        else
            Color(0xFFFFCDD2)
    } else {
        if (isDarkTheme)
            Color(0xFF2E7D32) // brighter green icon bg
        else
            Color(0xFFC8E6C9)
    }



    val iconTint = if (isNegative) {
        if (isDarkTheme)
            Color(0xFFEF9A9A) // soft red for dark mode
        else
            Color(0xFFD32F2F) // strong red for light mode
    } else {
        if (isDarkTheme)
            Color(0xFFA5D6A7) // soft green for dark mode
        else
            Color(0xFF388E3C) // strong green for light mode
    }

    val editBg = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.White
    }

    val editIconTint = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color =  secondaryTextColor,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
//                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = "$${String.format("%.2f", amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryTextColor,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
                )
            }
        }
            val isDarkTheme = isSystemInDarkTheme()

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(editBg)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = editIconTint,
                    modifier = Modifier.size(16.dp)
                        .clickable(onClick = onEditClick)
                )
            }
//            tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,

    }
}

@Composable
fun DebtOverviewCard(
    debtOverview: com.humblecoders.aromex_android_windows.domain.model.DebtOverview,
    modifier: Modifier = Modifier
) {



    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debt Overview",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val isDarkTheme = isSystemInDarkTheme()
            DebtItem(
                label = "Total Owed",
                amount = debtOverview.totalOwed,
                color = if (isDarkTheme) MaterialTheme.colorScheme.errorContainer else Color(0xFFFFEBEE),
                textColor = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.error,
                icon = Icons.Default.ArrowUpward,
                isDarkTheme = isDarkTheme
            )
            Spacer(modifier = Modifier.height(8.dp))
            DebtItem(
                label = "Total Due to Me",
                amount = debtOverview.totalDueToMe,
                color = if (isDarkTheme) getAromexSuccessContainerColor() else Color(0xFFE8F5E9),
                textColor = if (isDarkTheme) Color.White else getAromexSuccessColor(),
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
    val iconTint = if (isDarkTheme) {
        if (amount < 0)
            Color(0xFFFFB4AB) // soft red (Material error pastel)
        else
            Color(0xFFB7F0C1) // soft mint green
    } else {
        textColor // your existing strong color
    }
    val iconBg = if (isDarkTheme) {
        Color.White.copy(alpha = 0.14f)
    } else {
        Color.White.copy(alpha = 0.65f)
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .padding(horizontal = 14.dp, vertical = 14.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))


        Column{
            Text(
                text = label,
                fontSize = 15.sp,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
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
    val isDarkTheme = isSystemInDarkTheme()

    val iconBg: Color
    val iconTint: Color

    when {
        text.contains("Entity", ignoreCase = true) -> {
            if (isDarkTheme) {
                iconBg = Color(0xFF1E3A5F)   // dark blue bg
                iconTint = Color(0xFFB6CCFF) // soft blue icon
            } else {
                iconBg = Color(0xFFE8F0FE)   // light blue bg
                iconTint = Color(0xFF1A73E8) // blue icon
            }
        }

        text.contains("Product", ignoreCase = true) -> {
            if (isDarkTheme) {
                iconBg = Color(0xFF3A2A5E)   // dark purple bg
                iconTint = Color(0xFFD7C9FF) // pastel purple icon
            } else {
                iconBg = Color(0xFFF3E8FF)   // light purple bg
                iconTint = Color(0xFF7E57C2) // purple icon
            }
        }

        text.contains("Expense", ignoreCase = true) -> {
            if (isDarkTheme) {
                iconBg = Color(0xFF4A1C1C)   // muted dark red bg
                iconTint = Color(0xFFFFB4AB) // soft red icon
            } else {
                iconBg = Color(0xFFFFEBEE)   // light red bg
                iconTint = Color(0xFFD32F2F) // red icon
            }
        }

        else -> {
            iconBg = MaterialTheme.colorScheme.surfaceVariant
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }


                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBalanceBottomSheet(
    viewModel: HomeViewModel,
    balanceType: String,
    currentAmount: Double,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // ✅ Visibility controller for iOS-style animation
    var visible by remember { mutableStateOf(false) }

    // ✅ Material sheet state (kept, but no snap usage)
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // ✅ iOS-style spring
    val iosSpring: FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = 0.85f,
        stiffness = 300f
    )


    // ---------------- YOUR EXISTING STATE ----------------
    var newAmount by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isSaving by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val balanceTypeLabel = when (balanceType) {
        "bank" -> "Bank Balance"
        "cash" -> "Cash"
        "creditCard" -> "Credit Card"
        else -> balanceType
    }

    val focusRequester = remember { FocusRequester() }

    val isSaveEnabled =
        newAmount.text.isNotBlank() &&
                newAmount.text.toDoubleOrNull() != null &&
                !isSaving &&
                !isSaved

    // ✅ iOS-style dismiss
    val handleDismiss: () -> Unit = {
        scope.launch {
            visible = false
            sheetState.hide()
            delay(260)
            onDismiss()
        }
    }
    LaunchedEffect(Unit) {
        visible = true
        focusRequester.requestFocus()
    }


    // ---------------- SAVE STATE TRACKING (UNCHANGED) ----------------
    var saveStartTime by remember { mutableStateOf<Long?>(null) }
    var previousError by remember { mutableStateOf<String?>(null) }
    var previousIsLoading by remember { mutableStateOf(false) }

    LaunchedEffect(error, isLoading) {
        if (isSaving && saveStartTime != null) {
            when {
                error != null && previousError == null -> {
                    isSaving = false
                    isSaved = false
                    errorMessage = error
                    saveStartTime = null
                }

                error == null && previousIsLoading && !isLoading -> {
                    isSaving = false
                    isSaved = true
                    errorMessage = null
                    saveStartTime = null
                    delay(800)
                    handleDismiss()
                }

                error == null && !isLoading -> {
                    val elapsed = System.currentTimeMillis() - (saveStartTime ?: 0)
                    if (elapsed > 1500) {
                        isSaving = false
                        isSaved = true
                        errorMessage = null
                        saveStartTime = null
                        delay(800)
                        handleDismiss()
                    }
                }
            }
        }
        previousError = error
        previousIsLoading = isLoading
    }


    val isDarkTheme = isSystemInDarkTheme()
    val boxBgColor = if (isDarkTheme) {
        Color(0xFF1C1F24) // soft dark surface (not pure black)
    } else {
        Color(0xFFF1F3F6) // your existing light bg
    }

    val boxBorderColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.12f) // subtle border in dark mode
    } else {
        Color(0xFFDADCE0)
    }

    val iconBgColor = if (isDarkTheme) {
        Color(0xFF2A2F36) // soft dark neutral
    } else {
        Color(0xFFE6EBF2) // your existing light bg
    }

    val iconTintColor = if (isDarkTheme) {
        Color.White.copy(alpha = 0.9f)
    } else {
        AromexColors.TextDark
    }
    // ================= iOS ANIMATION WRAPPER =================
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = iosSpring
        ) + fadeIn(
            animationSpec = tween(220)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = iosSpring
        ) + fadeOut(
            animationSpec = tween(180)
        )
    ) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                if (!isSaving) handleDismiss()
            },

            scrimColor = Color.Black.copy(alpha = 0.25f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {

            // ================= YOUR EXISTING CONTENT =================
            // ⬇️ NOTHING BELOW THIS LINE WAS CHANGED ⬇️

            /* KEEP YOUR ENTIRE COLUMN CONTENT EXACTLY AS YOU POSTED */
            val maxSheetHeight: Float = (maxHeight * 0.75f).toFloat()
            Column(
                modifier = Modifier

                    .fillMaxWidth()
                    .fillMaxHeight(0.90f)   // ✅ 3/4 HEIGHT
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
                    .verticalScroll(rememberScrollState()) // ✅ IMPORTANT
                    .windowInsetsPadding(WindowInsets.ime),
                verticalArrangement = Arrangement.spacedBy(24.dp)

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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint =iconTintColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Edit $balanceTypeLabel",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Update your financial information",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = handleDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider()

                // Current Amount Section
                Column(modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        //                    maxLines = 1,
                        //                    overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp)) // 💊 capsule feel
                            .background(boxBgColor)
                            .border(
                                width = 1.dp,
                                color = boxBorderColor,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 32.dp, vertical = 16.dp) // ⬆️ bigger & better
                    ) {
                        Text(
                            text = "$${String.format("%.2f", currentAmount)}",
                            fontSize = 22.sp, // ⬆️ slightly larger
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else AromexColors.TextDark
                        )
                    }
                }


                // New Amount Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "New Amount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
                    )
                    OutlinedTextField(
                        value = newAmount,
                        enabled = !isSaving && !isSaved,
                        onValueChange = { newValue ->

                            // 1️⃣ Allow only digits and one decimal
                            var filtered = newValue.text.filter { it.isDigit() || it == '.' }

                            val decimalCount = filtered.count { it == '.' }
                            if (decimalCount > 1) {
                                val firstDecimalIndex = filtered.indexOf('.')
                                filtered = filtered.substring(0, firstDecimalIndex + 1) +
                                        filtered.substring(firstDecimalIndex + 1).replace(".", "")
                            }

                            if (balanceType == "creditCard") {

                                val finalText = when {
                                    filtered.isEmpty() -> ""                 // empty
                                    filtered == "0" -> "0"                   // avoid "-0"
                                    else -> "-$filtered"                     // includes 0., 0.5, etc.
                                }

                                // Cursor always at end (stable, natural typing)
                                newAmount = TextFieldValue(
                                    text = finalText,
                                    selection = TextRange(finalText.length)
                                )

                            } else {
                                // Normal positive amount
                                val cursorPos = newValue.selection.start.coerceIn(0, filtered.length)

                                newAmount = TextFieldValue(
                                    text = filtered,
                                    selection = TextRange(cursorPos)
                                )
                            }
                        }

                        ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Enter amount", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),


                        supportingText = {
                            Text(
                                text = "Enter the new amount for this account",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))


                // Action Buttons
                val primaryButtonBg = if (isDarkTheme) {
                    AromexColors.ButtonBlue.copy(alpha = 0.9f) // slightly softened in dark mode
                } else {
                    AromexColors.ButtonBlue
                }

                val primaryButtonContent = Color.White
                val secondaryButtonBg = if (isDarkTheme) {
                    Color(0xFF1C1F24) // soft dark surface (same family as your boxes)
                } else {
                    Color.White
                }

                val secondaryButtonBorder = if (isDarkTheme) {
                    Color.White.copy(alpha = 0.18f)
                } else {
                    Color(0xFFD0D5DD)
                }

                val secondaryButtonText = if (isDarkTheme) {
                    Color.White
                } else {
                    Color.Black
                }

                // Error message display
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        // Hide keyboard first
                        keyboardController?.hide()

                        val parsed = newAmount.text.toDoubleOrNull() ?: return@Button
                        val amount = if (balanceType == "creditCard") -kotlin.math.abs(parsed) else parsed

                        // Clear previous error and reset states
                        errorMessage = null
                        isSaving = true
                        isSaved = false
                        saveStartTime = System.currentTimeMillis()
                        previousError = null
                        previousIsLoading = isLoading

                        // Call viewmodel to save
                        viewModel.updateSingleBalance(balanceType, amount)
                    },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryButtonBg,
                        contentColor = primaryButtonContent,
                        disabledContainerColor = primaryButtonBg.copy(alpha = 0.6f),
                        disabledContentColor = primaryButtonContent.copy(alpha = 0.6f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    when {
                        isSaving -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Saving...",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        isSaved -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Saved",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Save Changes",
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = handleDismiss,
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp), // ✅ pill shape
                    border = BorderStroke(
                        width = 1.2.dp,
                        color = Color(0xFFD0D5DD)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = secondaryButtonBg,
                        contentColor = secondaryButtonText
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }


            }


            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

