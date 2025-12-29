package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun AndroidHomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavigate = { route ->
                    onNavigate(route)
                    scope.launch { drawerState.close() }
                },
                onClose = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        MainContent(
            viewModel = viewModel,
            onMenuClick = { scope.launch { drawerState.open() } },
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .windowInsetsPadding(WindowInsets.systemBars)
        )
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
            .background(Color(0xFF1E3A5F))
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
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onClose) {
                Text(
                    text = "Done",
                    color = Color(0xFF64B5F6)
                )
            }
        }

        Text(
            text = "Menu",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
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
            .background(if (isSelected) Color(0xFF2E5A8F) else Color.Transparent)
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
                fontSize = 16.sp
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
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accountBalance by viewModel.accountBalance.collectAsState()
    
    var showEditSheet by remember { mutableStateOf(false) }
    var editingBalanceType by remember { mutableStateOf<String?>(null) }
    var editingCurrentAmount by remember { mutableStateOf(0.0) }

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
                    tint = Color.Black
                )
            }
            Text(
                text = "Home",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Balances Card
        AccountBalancesCard(
            accountBalance = accountBalance,
            onEditClick = { balanceType, currentAmount ->
                editingBalanceType = balanceType
                editingCurrentAmount = currentAmount
                showEditSheet = true
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Debt Overview Card
//        DebtOverviewCard(
//            debtOverview = debtOverview,
//            modifier = Modifier.fillMaxWidth()
//        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Actions Section
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        QuickActionButton(
            text = "Add Entity",
            icon = Icons.Default.PersonAdd,
            onClick = { /* TODO */ },
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
            onClick = { /* TODO */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // Edit Balance Bottom Sheet
    if (showEditSheet && editingBalanceType != null) {
        EditBalanceBottomSheet(
            balanceType = editingBalanceType!!,
            currentAmount = editingCurrentAmount,
            onDismiss = { showEditSheet = false },
            onSave = { newAmount ->
                viewModel.updateSingleBalance(editingBalanceType!!, newAmount)
                showEditSheet = false
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account Balances",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            BalanceItem(
                label = "Bank Balance",
                amount = accountBalance.bankBalance,
                icon = Icons.Default.AccountBalance,
                onEditClick = { onEditClick("bank", accountBalance.bankBalance) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BalanceItem(
                label = "Cash",
                amount = accountBalance.cash,
                icon = Icons.Default.Money,
                onEditClick = { onEditClick("cash", accountBalance.cash) }
            )
            Spacer(modifier = Modifier.height(8.dp))
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 15.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onEditClick)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Debt Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            DebtItem(
                label = "Total Owed",
                amount = debtOverview.totalOwed,
                color = Color(0xFFFFEBEE),
                textColor = Color(0xFFD32F2F),
                icon = Icons.Default.ArrowUpward
            )
            Spacer(modifier = Modifier.height(8.dp))
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
            fontSize = 15.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$${String.format("%.2f", amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBalanceBottomSheet(
    balanceType: String,
    currentAmount: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var newAmountText by remember { mutableStateOf(currentAmount.toString()) }
    val balanceTypeLabel = when (balanceType) {
        "bank" -> "Bank Balance"
        "cash" -> "Cash"
        "creditCard" -> "Credit Card"
        else -> balanceType
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Edit $balanceTypeLabel",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Update your financial information",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Gray
                    )
                }
            }
            
            // Current Amount Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Current Amount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Text(
                        text = "$${String.format("%.2f", currentAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // New Amount Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "New Amount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = newAmountText,
                    onValueChange = { newAmountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Enter amount") },
                    supportingText = {
                        Text(
                            text = "Enter the new amount for this account",
                            fontSize = 12.sp,
                            color = Color.Gray
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
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val amount = newAmountText.toDoubleOrNull() ?: return@Button
                        onSave(amount)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
