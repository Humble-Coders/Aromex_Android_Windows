package com.humblecoders.aromex_android_windows.presentation.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.humblecoders.aromex_android_windows.presentation.viewmodel.PurchaseViewModel
import com.humblecoders.aromex_android_windows.presentation.viewmodel.HomeViewModel
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import androidx.compose.runtime.collectAsState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// Custom savers for LocalDate and YearMonth
@RequiresApi(Build.VERSION_CODES.O)
private val LocalDateSaver = Saver<LocalDate, String>(
    save = { it.toString() },
    restore = { LocalDate.parse(it) }
)

@RequiresApi(Build.VERSION_CODES.O)
private val YearMonthSaver = Saver<YearMonth, String>(
    save = { it.toString() },
    restore = { 
        // Parse manually for API level 24 compatibility
        val parts = it.split("-")
        if (parts.size == 2) {
            YearMonth.of(parts[0].toInt(), parts[1].toInt())
        } else {
            // Fallback to current month if parsing fails
            YearMonth.now()
        }
    }
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarPopup(
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
    
    // Get the first day of the month and number of days
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 0 = Sunday, 1 = Monday, etc.
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // Get days from previous month
    val previousMonth = currentMonth.minusMonths(1)
    val daysInPreviousMonth = previousMonth.lengthOfMonth()
    
    Card(
        modifier = Modifier
            .width(320.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with "Select Date"
            Text(
                text = "Select Date",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onMonthChange(currentMonth.minusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous month",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                IconButton(
                    onClick = { onMonthChange(currentMonth.plusMonths(1)) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next month",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Days of Week Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar Grid
            Column {
                // Generate 6 weeks
                var dayCounter = 1 - firstDayOfWeek
                for (week in 0 until 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0..6) {
                            val dayToShow = dayCounter
                            val isCurrentMonth = dayToShow in 1..daysInMonth
                            val dayValue = when {
                                dayToShow < 1 -> {
                                    // Previous month
                                    daysInPreviousMonth + dayToShow
                                }
                                dayToShow > daysInMonth -> {
                                    // Next month
                                    dayToShow - daysInMonth
                                }
                                else -> dayToShow
                            }
                            
                            val dateForDay = when {
                                dayToShow < 1 -> previousMonth.atDay(dayValue)
                                dayToShow > daysInMonth -> currentMonth.plusMonths(1).atDay(dayValue)
                                else -> currentMonth.atDay(dayValue)
                            }
                            val isSelected = dateForDay == selectedDate
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.secondary
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            onDateSelected(dateForDay)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayValue.toString(),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> Color.White
                                            isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidPurchaseScreen(
    viewModel: PurchaseViewModel,
    homeViewModel: HomeViewModel,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    var orderNumber by rememberSaveable { mutableStateOf("ORD-85") }
    
    // Initialize date to today
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    var date by rememberSaveable { mutableStateOf(today.format(dateFormatter)) }
    var supplierExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedSupplier by rememberSaveable { mutableStateOf<Entity?>(null) }
    var supplierSearchQuery by rememberSaveable { mutableStateOf("") }
    
    // Get entities from viewmodel (using shared EntityRepository)
    val entities by viewModel.entities.collectAsState()
    
    // Calendar popup state - Initialize to today
    var calendarExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(today) }
    var currentMonth by rememberSaveable(stateSaver = YearMonthSaver) { mutableStateOf(YearMonth.from(today)) }
    
    // Add Product Sheet state
    var showAddProductSheet by rememberSaveable { mutableStateOf(false) }
    
    // Add Service Sheet state
    var showAddServiceSheet by rememberSaveable { mutableStateOf(false) }
    
    // Add Entity Dialog state
    var showAddEntityDialog by rememberSaveable { mutableStateOf(false) }
    var addEntityInitialName by rememberSaveable { mutableStateOf("") }
    var addEntityInitialType by rememberSaveable { mutableStateOf<EntityType?>(null) }
    
    // Track newly created entity to auto-select it
    var pendingEntitySelection by remember { mutableStateOf<Pair<String, EntityType>?>(null) }
    
    // Start listening when Purchase screen is opened (whichever screen opens first loads the data)
    LaunchedEffect(Unit) {
        com.humblecoders.aromex_android_windows.data.repository.FirestoreEntityRepository.startListening()
        // Fetch specification data (brands, capacities, carriers, colors, storageLocations, unitCosts)
        viewModel.specificationViewModel.fetchAllSpecifications()
    }
    
    // Collect specification data from ViewModel
    val brands by viewModel.specificationViewModel.brands.collectAsState()
    val capacities by viewModel.specificationViewModel.capacities.collectAsState()
    val carriers by viewModel.specificationViewModel.carriers.collectAsState()
    val colors by viewModel.specificationViewModel.colors.collectAsState()
    val storageLocations by viewModel.specificationViewModel.storageLocations.collectAsState()
    val unitCosts by viewModel.specificationViewModel.unitCosts.collectAsState()
    
    // Auto-select newly created entity when it appears in the list
    LaunchedEffect(entities, pendingEntitySelection) {
        val pending = pendingEntitySelection
        if (pending != null) {
            val (entityName, entityType) = pending
            // Find the entity that matches the name and type
            val newEntity = entities.find { it.name == entityName && it.type == entityType }
            if (newEntity != null) {
                selectedSupplier = newEntity
                supplierSearchQuery = newEntity.name
                pendingEntitySelection = null
            }
        }
    }
    
    // Root position tracking for dropdown
    var rootPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    
    // Supplier field position tracking - moved outside Column scope
    val density = LocalDensity.current
    var supplierFieldPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var textFieldHeight by remember { mutableStateOf(0.dp) }
    var supplierFieldWidth by remember { mutableStateOf(0.dp) }
    
    // Date field position tracking for calendar popup
    var dateFieldPosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var dateFieldHeight by remember { mutableStateOf(0.dp) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootPosition = coordinates.positionInRoot()
            }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Top Bar with Menu, Title, and Profile - matching home screen
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Purchase",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { /* Profile action - no logic */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Form Card with White Background
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite()
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Order Number Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Order number",
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
                        val isDark = isSystemInDarkTheme()
                        OutlinedTextField(
                            value = orderNumber,
                            onValueChange = { orderNumber = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Auto-generated order number",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Date Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Date",
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val isDark = isSystemInDarkTheme()
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 64.dp)
                                .onGloballyPositioned { coordinates ->
                                    dateFieldPosition = coordinates.positionInRoot()
                                    dateFieldHeight = with(density) { coordinates.size.height.toDp() }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.clickable { calendarExpanded = !calendarExpanded }
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The date when this purchase was made",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Supplier Section
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Supplier",
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
                        val selected = selectedSupplier // Store in local variable for smart cast
                        val displayValue = selected?.name ?: supplierSearchQuery
                        val isDark = isSystemInDarkTheme()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    supplierFieldPosition = coordinates.positionInRoot()
                                    textFieldHeight = with(density) { coordinates.size.height.toDp() }
                                    supplierFieldWidth = with(density) { coordinates.size.width.toDp() }
                                }
                        ) {
                            OutlinedTextField(
                                value = displayValue,
                                onValueChange = { newValue: String ->
                                    supplierSearchQuery = newValue
                                    // Clear selection if user types something different
                                    if (selected != null && newValue != selected.name) {
                                        selectedSupplier = null
                                    }
                                    supplierExpanded = true
                                },
                                placeholder = {
                                    if (selected == null) {
                                        Text(
                                            text = "Search supplier...",
                                            color = if (isDark)
                                                Color.White.copy(alpha = 0.6f)
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 64.dp)
                                    .onFocusChanged { focusState ->
                                        // Open dropdown when field gains focus (clicked)
                                        if (focusState.isFocused && !supplierExpanded) {
                                            supplierExpanded = true
                                        }
                                    },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                shape = RoundedCornerShape(12.dp),
                                // Use leadingIcon as spacer when selected
                                leadingIcon = if (selected != null) {
                                    {
                                        Box(modifier = Modifier.width(105.dp))
                                    }
                                } else null,
                                trailingIcon = {
                                    IconButton(onClick = { supplierExpanded = !supplierExpanded }) {
                                        Icon(
                                            imageVector = if (supplierExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (supplierExpanded) "Hide" else "Show"
                                        )
                                    }
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
                                    focusedPlaceholderColor = if (isDark)
                                        Color.White.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedPlaceholderColor = if (isDark)
                                        Color.White.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    cursorColor = if (isDark) Color.White else Color.Black
                                )
                            )
                            // Overlay content matching dropdown layout exactly - only show when supplier is selected
                            if (selected != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(start = 16.dp, end = 48.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Role pill - exactly matching dropdown
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (selected.type) {
                                            EntityType.CUSTOMER -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                            EntityType.SUPPLIER -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            EntityType.MIDDLEMAN -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                        },
                                        modifier = Modifier
                                            .width(85.dp)
                                            .height(24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = when (selected.type) {
                                                    EntityType.CUSTOMER -> "Customer"
                                                    EntityType.SUPPLIER -> "Supplier"
                                                    EntityType.MIDDLEMAN -> "Middleman"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = when (selected.type) {
                                                    EntityType.CUSTOMER -> Color(0xFF1976D2)
                                                    EntityType.SUPPLIER -> Color(0xFF388E3C)
                                                    EntityType.MIDDLEMAN -> Color(0xFFF57C00)
                                                },
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    // Supplier name text
                                    Text(
                                        text = selected.name,
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
                            text = "Select a supplier for this purchase",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // Supplier Dropdown - Inside the Card
                        AnimatedVisibility(
                            visible = supplierExpanded,
                            enter = fadeIn(tween(200)) + slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = tween(300)
                            ),
                            exit = fadeOut(tween(200)) + slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = tween(300)
                            )
                        ) {
                            // Filter and sort entities
                            val sortedEntities: List<Entity> = remember(entities) {
                                entities.filter { it.type == EntityType.SUPPLIER }.sortedBy { it.name } +
                                entities.filter { it.type != EntityType.SUPPLIER }.sortedBy { it.name }
                            }
                            
                            val hasExactMatch = remember(sortedEntities, supplierSearchQuery) {
                                if (supplierSearchQuery.isBlank()) {
                                    false
                                } else {
                                    sortedEntities.any { 
                                        it.name.equals(supplierSearchQuery, ignoreCase = true)
                                    }
                                }
                            }
                            
                            val filteredEntities: List<Entity> = remember(sortedEntities, supplierSearchQuery, selectedSupplier, supplierExpanded) {
                                val currentSelected = selectedSupplier // Local variable for smart cast
                                val query = supplierSearchQuery.lowercase()
                                val filtered: List<Entity> = if (supplierSearchQuery.isBlank()) {
                                    sortedEntities
                                } else {
                                    if (supplierExpanded && currentSelected != null &&
                                        supplierSearchQuery.equals(currentSelected.name, ignoreCase = true)) {
                                        sortedEntities
                                    } else {
                                        sortedEntities.filter { 
                                            it.name.lowercase().contains(query)
                                        }
                                    }
                                }
                                
                                if (currentSelected != null) {
                                    val selectedInList = filtered.find { it.id == currentSelected.id }
                                    if (selectedInList != null) {
                                        val withoutSelected = filtered.filter { it.id != currentSelected.id }
                                        listOf(currentSelected) + withoutSelected
                                    } else {
                                        filtered
                                    }
                                } else {
                                    filtered
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                if (filteredEntities.isEmpty()) {
                                    if (supplierSearchQuery.isNotBlank() && !hasExactMatch) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    addEntityInitialName = supplierSearchQuery
                                                    addEntityInitialType = EntityType.SUPPLIER
                                                    showAddEntityDialog = true
                                                    supplierExpanded = false
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Add new",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "Add '$supplierSearchQuery'",
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF4CAF50),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No items found",
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (!hasExactMatch && supplierSearchQuery.isNotBlank()) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable(
                                                            interactionSource = remember { MutableInteractionSource() },
                                                            indication = null
                                                        ) {
                                                            addEntityInitialName = supplierSearchQuery
                                                            addEntityInitialType = EntityType.SUPPLIER
                                                            showAddEntityDialog = true
                                                            supplierExpanded = false
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF4CAF50),
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Add,
                                                                    contentDescription = "Add new",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "Add '$supplierSearchQuery'",
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF4CAF50),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        items(filteredEntities) { entity ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null
                                                    ) {
                                                        selectedSupplier = entity
                                                        supplierSearchQuery = entity.name
                                                        supplierExpanded = false
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 16.dp, end = 48.dp, top = 12.dp, bottom = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = when (entity.type) {
                                                            EntityType.CUSTOMER -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                                            EntityType.SUPPLIER -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                                            EntityType.MIDDLEMAN -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                                        },
                                                        modifier = Modifier
                                                            .width(85.dp)
                                                            .height(24.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = when (entity.type) {
                                                                    EntityType.CUSTOMER -> "Customer"
                                                                    EntityType.SUPPLIER -> "Supplier"
                                                                    EntityType.MIDDLEMAN -> "Middleman"
                                                                },
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                color = when (entity.type) {
                                                                    EntityType.CUSTOMER -> Color(0xFF1976D2)
                                                                    EntityType.SUPPLIER -> Color(0xFF388E3C)
                                                                    EntityType.MIDDLEMAN -> Color(0xFFF57C00)
                                                                },
                                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = entity.name,
                                                        fontSize = 14.sp,
                                                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                    if (selectedSupplier != null && selectedSupplier!!.id == entity.id) {
                                                        Box(
                                                            modifier = Modifier.size(24.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Surface(
                                                                shape = CircleShape,
                                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                                modifier = Modifier.fillMaxSize()
                                                            ) {}
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons - Side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Product Button
                Button(
                    onClick = { showAddProductSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Product",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Add Service Button
                Button(
                    onClick = { showAddServiceSheet = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800) // Orange color
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Service",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            }
        }
        
        // Calendar Popup - Centered with blur background
        AnimatedVisibility(
            visible = calendarExpanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(200))
        ) {
            // Blur background overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { calendarExpanded = false }
                    .zIndex(999f)
            )
            
            // Centered calendar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1000f),
                contentAlignment = Alignment.Center
            ) {
                CalendarPopup(
                    selectedDate = selectedDate,
                    currentMonth = currentMonth,
                    onDateSelected = { dateSelected: LocalDate ->
                        selectedDate = dateSelected
                        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
                        date = dateSelected.format(formatter)
                        calendarExpanded = false
                    },
                    onMonthChange = { newMonth: YearMonth ->
                        currentMonth = newMonth
                    },
                    onDismiss = { calendarExpanded = false },
                    isDarkTheme = isDarkTheme
                )
            }
        }
        
        // Add Product Bottom Sheet
        if (showAddProductSheet) {
            AddProductSheet(
                isDarkTheme = isDarkTheme,
                specificationViewModel = viewModel.specificationViewModel,
                onDismiss = { showAddProductSheet = false }
            )
        }
        
        // Add Service Bottom Sheet
        if (showAddServiceSheet) {
            AddServiceSheet(
                isDarkTheme = isDarkTheme,
                onDismiss = { showAddServiceSheet = false }
            )
        }
        
        // Add Entity Bottom Sheet
        if (showAddEntityDialog) {
            AddEntitySheet(
                onDismiss = { 
                    showAddEntityDialog = false
                    addEntityInitialName = ""
                    addEntityInitialType = null
                },
                onSave = { entity ->
                    homeViewModel.addEntity(entity)
                    // Store entity info to auto-select when it appears in the list
                    pendingEntitySelection = Pair(entity.name, entity.type)
                    showAddEntityDialog = false
                    addEntityInitialName = ""
                    addEntityInitialType = null
                },
                viewModel = homeViewModel,
                initialName = addEntityInitialName,
                initialType = addEntityInitialType
            )
        }
    }
}

