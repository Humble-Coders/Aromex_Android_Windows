package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow as TextOverflowCompose
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.runtime.collectAsState
import com.humblecoders.aromex_android_windows.presentation.viewmodel.SpecificationViewModel


// Simple data class for dropdown items
data class DropdownItem(
    val id: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductCard(
    showCard: Boolean,
    isDarkTheme: Boolean = false,
    specificationViewModel: SpecificationViewModel,
    onClose: () -> Unit,
    onSaveStarted: () -> Unit = {}
) {
    // Dropdown state variables
    var selectedBrand by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedModel by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedCapacity by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedCarrier by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedColor by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedStatus by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedStorageLocation by remember { mutableStateOf<DropdownItem?>(null) }
    var selectedCapacityUnit by remember { mutableStateOf("GB") }
    
    // Text field state variables
    var imeiText by remember { mutableStateOf("") }
    var unitCostText by remember { mutableStateOf("") }
    
    // Loading and error states
    val isLoading by specificationViewModel.isLoading.collectAsState()
    val errorMessage by specificationViewModel.error.collectAsState()
    val successMessage by specificationViewModel.successMessage.collectAsState()
    
    // Per-collection loading states (Improvement #4)
    val brandsLoading by specificationViewModel.brandsLoading.collectAsState()
    val capacitiesLoading by specificationViewModel.capacitiesLoading.collectAsState()
    val carriersLoading by specificationViewModel.carriersLoading.collectAsState()
    val colorsLoading by specificationViewModel.colorsLoading.collectAsState()
    val storageLocationsLoading by specificationViewModel.storageLocationsLoading.collectAsState()
    
    // Collect data from SpecificationViewModel
    val brandsState by specificationViewModel.brands.collectAsState()
    val capacitiesState by specificationViewModel.capacities.collectAsState()
    val carriersState by specificationViewModel.carriers.collectAsState()
    val colorsState by specificationViewModel.colors.collectAsState()
    val storageLocationsState by specificationViewModel.storageLocations.collectAsState()
    
    // Log when data is fetched
    LaunchedEffect(brandsState.size, capacitiesState.size, carriersState.size, colorsState.size, storageLocationsState.size) {
        println("[AddProductDialog] 📊 Data fetched - Brands: ${brandsState.size}, Capacities: ${capacitiesState.size}, Carriers: ${carriersState.size}, Colors: ${colorsState.size}, StorageLocations: ${storageLocationsState.size}")
    }
    
    // Convert to DropdownItem format
    val brands = brandsState.map { DropdownItem(it.id, it.brandName) }
    val capacities = capacitiesState.map { DropdownItem(it.id, it.capacity) }
    val carriers = carriersState.map { DropdownItem(it.id, it.carrierName) }
    val colors = colorsState.map { DropdownItem(it.id, it.colorName) }
    val storageLocations = storageLocationsState.map { DropdownItem(it.id, it.storageLocation) }
    
    // Status dropdown options
    val statusOptions = listOf(
        DropdownItem("active", "Active"),
        DropdownItem("inactive", "Inactive")
    )
    
    // Models state - fetched when brand is selected
    var modelsForBrand by remember { mutableStateOf<List<com.humblecoders.aromex_android_windows.domain.model.Model>>(emptyList()) }
    
    // Fetch models when brand is selected
    LaunchedEffect(selectedBrand?.id) {
        val currentBrand = selectedBrand
        if (currentBrand == null) {
            println("[AddProductDialog] 🔄 Brand deselected - clearing models")
            modelsForBrand = emptyList()
            selectedModel = null
        } else {
            println("[AddProductDialog] 🔍 Fetching models for brand: ${currentBrand.name} (ID: ${currentBrand.id})")
            // Fetch models for the selected brand
            specificationViewModel.fetchModelsByBrand(currentBrand.id).fold(
                onSuccess = { models ->
                    println("[AddProductDialog] ✅ Models fetched successfully: ${models.size} models found for brand '${currentBrand.name}'")
                    modelsForBrand = models
                    // Reset model selection if current model doesn't belong to new brand
                    val currentModelId = selectedModel?.id
                    if (currentModelId != null) {
                        val modelStillValid = models.any { it.id == currentModelId }
                        if (!modelStillValid) {
                            println("[AddProductDialog] ⚠️ Current model selection invalid for new brand - clearing selection")
                            selectedModel = null
                        }
                    }
                },
                onFailure = { error ->
                    println("[AddProductDialog] ❌ Failed to fetch models for brand '${currentBrand.name}': ${error.message}")
                    modelsForBrand = emptyList()
                    selectedModel = null
                }
            )
        }
    }
    
    val models = modelsForBrand.map { DropdownItem(it.id, it.modelName) }
    
    // Search queries and expanded states
    var brandSearchQuery by remember { mutableStateOf("") }
    var modelSearchQuery by remember { mutableStateOf("") }
    var capacitySearchQuery by remember { mutableStateOf("") }
    var carrierSearchQuery by remember { mutableStateOf("") }
    var colorSearchQuery by remember { mutableStateOf("") }
    var storageLocationSearchQuery by remember { mutableStateOf("") }
    
    var brandExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }
    var capacityExpanded by remember { mutableStateOf(false) }
    var carrierExpanded by remember { mutableStateOf(false) }
    var colorExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }
    var storageLocationExpanded by remember { mutableStateOf(false) }
    
    // Track if we've triggered a data fetch for this dialog session
    var fetchTriggered by remember { mutableStateOf(false) }
    
    // Fetch data when dialog opens - only fetch if data is not already in StateFlow
    LaunchedEffect(showCard) {
        if (showCard && !fetchTriggered) {
            println("[AddProductDialog] 📥 Checking and fetching specification data if needed...")
            fetchTriggered = true
            // Only fetch if data doesn't exist in StateFlow - check StateFlow first
            specificationViewModel.fetchAllSpecifications(forceRefresh = false)
        }
        if (!showCard) {
            // Reset fetch trigger when dialog closes
            fetchTriggered = false
        }
    }
    
    // Individual field loading states using per-collection loading states (Improvement #4)
    // Use the granular loading states from ViewModel instead of calculating locally
    val isBrandLoading = brandsLoading
    val isCapacityLoading = capacitiesLoading
    val isCarrierLoading = carriersLoading
    val isColorLoading = colorsLoading
    val isStorageLocationLoading = storageLocationsLoading
    
    // Reset all fields when dialog opens
    LaunchedEffect(showCard) {
        if (showCard) {
            println("[AddProductDialog] 🚀 Add Product Dialog opened - resetting all fields")
            // Reset all dropdown selections
            selectedBrand = null
            selectedModel = null
            selectedCapacity = null
            selectedCarrier = null
            selectedColor = null
            selectedStatus = null
            selectedStorageLocation = null
            selectedCapacityUnit = "GB"
            
            // Reset text fields
            imeiText = ""
            unitCostText = ""
            
            // Reset search queries
            brandSearchQuery = ""
            modelSearchQuery = ""
            capacitySearchQuery = ""
            carrierSearchQuery = ""
            colorSearchQuery = ""
            storageLocationSearchQuery = ""
            
            // Reset expanded states
            brandExpanded = false
            modelExpanded = false
            capacityExpanded = false
            carrierExpanded = false
            colorExpanded = false
            statusExpanded = false
            storageLocationExpanded = false
            
            // Clear models
            modelsForBrand = emptyList()
            
            // Clear error and success messages
            specificationViewModel.clearError()
            specificationViewModel.clearSuccessMessage()
        }
    }
    
    // Focus management - focus brand field when dialog opens
    val brandFocusRequester = remember { FocusRequester() }
    
    // Focus brand field when dialog opens
    LaunchedEffect(showCard) {
        if (showCard) {
            kotlinx.coroutines.delay(100) // Wait for dialog animation
            brandFocusRequester.requestFocus()
        }
    }
    
    // Helper function to handle dropdown expansion - ensures only one is open
    fun handleDropdownExpansion(
        dropdownId: String,
        isExpanded: Boolean,
        onExpandedChange: (Boolean) -> Unit
    ) {
        if (isExpanded) {
            // Close all other dropdowns
            if (dropdownId != "brand") brandExpanded = false
            if (dropdownId != "model") modelExpanded = false
            if (dropdownId != "capacity") capacityExpanded = false
            if (dropdownId != "carrier") carrierExpanded = false
            if (dropdownId != "color") colorExpanded = false
            if (dropdownId != "status") statusExpanded = false
            if (dropdownId != "storageLocation") storageLocationExpanded = false
            onExpandedChange(true)
        } else {
            onExpandedChange(false)
        }
    }
    
    // Edit dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var editDialogTitle by remember { mutableStateOf("") }
    var editDialogCurrentValue by remember { mutableStateOf("") }
    var editItemId by remember { mutableStateOf<String?>(null) }
    var editItemType by remember { mutableStateOf<String?>(null) }
    
    // Helper function to show edit dialog
    fun showEditItemDialog(itemId: String, itemName: String, itemType: String) {
        editItemId = itemId
        editItemType = itemType
        editDialogCurrentValue = itemName
        editDialogTitle = "Edit ${itemType.replaceFirstChar { it.uppercaseChar() }}"
        showEditDialog = true
    }
    
    // Handle edit save
    fun handleEditSave(newValue: String) {
        val id = editItemId ?: return
        val type = editItemType ?: return
        
        when (type) {
            "brand" -> specificationViewModel.updateBrand(id, newValue)
            "capacity" -> specificationViewModel.updateCapacity(id, newValue)
            "carrier" -> specificationViewModel.updateCarrier(id, newValue)
            "color" -> specificationViewModel.updateColor(id, newValue)
            "storage location" -> specificationViewModel.updateStorageLocation(id, newValue)
            "model" -> {
                val currentBrand = selectedBrand
                if (currentBrand != null) {
                    val brandName = brandsState.find { it.id == currentBrand.id }?.brandName ?: currentBrand.name
                    specificationViewModel.updateModel(id, brandName, newValue)
                }
            }
        }
        
        // Update selected items and search queries if they match
        if (selectedBrand?.id == id) {
            selectedBrand = DropdownItem(id, newValue)
            brandSearchQuery = newValue
        }
        if (selectedModel?.id == id) {
            selectedModel = DropdownItem(id, newValue)
            modelSearchQuery = newValue
            // Update models list
            modelsForBrand = modelsForBrand.map { if (it.id == id) it.copy(modelName = newValue) else it }
        }
        if (selectedCapacity?.id == id) {
            selectedCapacity = DropdownItem(id, newValue)
            capacitySearchQuery = newValue
        }
        if (selectedCarrier?.id == id) {
            selectedCarrier = DropdownItem(id, newValue)
            carrierSearchQuery = newValue
        }
        if (selectedColor?.id == id) {
            selectedColor = DropdownItem(id, newValue)
            colorSearchQuery = newValue
        }
        if (selectedStorageLocation?.id == id) {
            selectedStorageLocation = DropdownItem(id, newValue)
            storageLocationSearchQuery = newValue
        }
        if (selectedStatus?.id == id) selectedStatus = DropdownItem(id, newValue)
        if (selectedColor?.id == id) colorSearchQuery = newValue
        if (selectedStorageLocation?.id == id) storageLocationSearchQuery = newValue
        
        showEditDialog = false
    }
    
    // Data is now fetched from SpecificationViewModel above
    
    // Root position and density for dropdown positioning
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    
    // Field position tracking
    var brandFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var brandFieldHeight by remember { mutableStateOf(0.dp) }
    var brandFieldWidth by remember { mutableStateOf(0.dp) }
    
    var modelFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var modelFieldHeight by remember { mutableStateOf(0.dp) }
    var modelFieldWidth by remember { mutableStateOf(0.dp) }
    
    var capacityFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var capacityFieldHeight by remember { mutableStateOf(0.dp) }
    var capacityFieldWidth by remember { mutableStateOf(0.dp) }
    
    var carrierFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var carrierFieldHeight by remember { mutableStateOf(0.dp) }
    var carrierFieldWidth by remember { mutableStateOf(0.dp) }
    
    var colorFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var colorFieldHeight by remember { mutableStateOf(0.dp) }
    var colorFieldWidth by remember { mutableStateOf(0.dp) }
    
    var storageLocationFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var storageLocationFieldHeight by remember { mutableStateOf(0.dp) }
    var storageLocationFieldWidth by remember { mutableStateOf(0.dp) }
    
    // Show dialog when requested - loading indicators will show on fields while data loads
    val shouldShowDialog = showCard
    
    AnimatedVisibility(
        visible = shouldShowDialog,
        enter = fadeIn(tween(300)) +
                scaleIn(
                    animationSpec = tween(300),
                    initialScale = 0.9f
                ),
        exit = fadeOut(tween(300)) +
                scaleOut(
                    animationSpec = tween(300),
                    targetScale = 0.9f
                )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(10000f)
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        rootPosition = coordinates.positionInRoot()
                    }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.9f)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(16.dp)
                ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {

                    /* ---------- HEADER ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFF2F80ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Add Product",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color.White else Color.Black
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Divider()
                    
                    // Error message display
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 14.sp
                            )
                        }
                    }
                    

                    /* ---------- ROW 1 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Brand Field
                        DropdownField(
                            label = "Brand",
                            required = true,
                            selectedItem = selectedBrand,
                            searchQuery = brandSearchQuery,
                            onSearchQueryChange = { newValue ->
                                brandSearchQuery = newValue
                                if (selectedBrand != null && newValue != selectedBrand?.name) {
                                    selectedBrand = null
                                }
                                handleDropdownExpansion("brand", true) { brandExpanded = it }
                            },
                            expanded = brandExpanded,
                            onExpandedChange = { handleDropdownExpansion("brand", it) { brandExpanded = it } },
                            placeholder = "Choose an option",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(brandFocusRequester),
                            isLoading = isBrandLoading,
                            onPositionChanged = { position, height, width ->
                                brandFieldPosition = position
                                brandFieldHeight = height
                                brandFieldWidth = width
                            }
                        )
                        // Model Field
                        DropdownField(
                            label = "Model",
                            required = true,
                            selectedItem = selectedModel,
                            searchQuery = modelSearchQuery,
                            onSearchQueryChange = { newValue ->
                                if (selectedBrand != null) {
                                    modelSearchQuery = newValue
                                    if (selectedModel != null && newValue != selectedModel?.name) {
                                        selectedModel = null
                                    }
                                    handleDropdownExpansion("model", true) { modelExpanded = it }
                                }
                            },
                            expanded = modelExpanded && selectedBrand != null,
                            onExpandedChange = { 
                                if (selectedBrand != null) {
                                    handleDropdownExpansion("model", it) { modelExpanded = it }
                                }
                            },
                            placeholder = "Select a brand first",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            enabled = selectedBrand != null,
                            isLoading = isBrandLoading,
                            onPositionChanged = { position, height, width ->
                                modelFieldPosition = position
                                modelFieldHeight = height
                                modelFieldWidth = width
                            }
                        )
                        // Capacity Field
                        CapacityFieldManual(
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            selectedCapacity = selectedCapacity,
                            capacitySearchQuery = capacitySearchQuery,
                            onCapacitySearchQueryChange = { newValue ->
                                capacitySearchQuery = newValue
                                if (selectedCapacity != null && newValue != selectedCapacity?.name) {
                                    selectedCapacity = null
                                }
                                handleDropdownExpansion("capacity", true) { capacityExpanded = it }
                            },
                            capacityExpanded = capacityExpanded,
                            onCapacityExpandedChange = { handleDropdownExpansion("capacity", it) { capacityExpanded = it } },
                            onCapacitySelected = { selectedCapacity = it },
                            selectedUnit = selectedCapacityUnit,
                            onUnitSelected = { selectedCapacityUnit = it },
                            capacities = capacities,
                            onPositionChanged = { position, height, width ->
                                capacityFieldPosition = position
                                capacityFieldHeight = height
                                capacityFieldWidth = width
                            },
                            specificationViewModel = specificationViewModel,
                            isLoading = isCapacityLoading
                        )
                    }

                    /* ---------- ROW 2 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // IMEI Field
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "IMEI/Serial",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
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
                                value = imeiText,
                                onValueChange = { imeiText = it },
                                placeholder = { Text("Enter IMEI or Serial num") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                    unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                                )
                            )
                        }
                        // Carrier Field
                        DropdownField(
                            label = "Carrier",
                            required = true,
                            selectedItem = selectedCarrier,
                            searchQuery = carrierSearchQuery,
                            onSearchQueryChange = { newValue ->
                                carrierSearchQuery = newValue
                                if (selectedCarrier != null && newValue != selectedCarrier?.name) {
                                    selectedCarrier = null
                                }
                                handleDropdownExpansion("carrier", true) { carrierExpanded = it }
                            },
                            expanded = carrierExpanded,
                            onExpandedChange = { handleDropdownExpansion("carrier", it) { carrierExpanded = it } },
                            placeholder = "Choose an option",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            isLoading = isCarrierLoading,
                            onPositionChanged = { position, height, width ->
                                carrierFieldPosition = position
                                carrierFieldHeight = height
                                carrierFieldWidth = width
                            }
                        )
                        // Color Field
                        DropdownField(
                            label = "Color",
                            required = true,
                            selectedItem = selectedColor,
                            searchQuery = colorSearchQuery,
                            onSearchQueryChange = { newValue ->
                                colorSearchQuery = newValue
                                if (selectedColor != null && newValue != selectedColor?.name) {
                                    selectedColor = null
                                }
                                handleDropdownExpansion("color", true) { colorExpanded = it }
                            },
                            expanded = colorExpanded,
                            onExpandedChange = { handleDropdownExpansion("color", it) { colorExpanded = it } },
                            placeholder = "Choose an option",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            isLoading = isColorLoading,
                            onPositionChanged = { position, height, width ->
                                colorFieldPosition = position
                                colorFieldHeight = height
                                colorFieldWidth = width
                            }
                        )
                    }

                    /* ---------- ROW 3 ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Status Field
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Status",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " *",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = statusExpanded,
                                    onExpandedChange = { statusExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = selectedStatus?.name ?: "",
                                        onValueChange = { },
                                        readOnly = true,
                                        placeholder = {
                                            if (selectedStatus == null) {
                                                Text(
                                                    text = "Choose an option",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflowCompose.Ellipsis
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { statusExpanded = !statusExpanded },
                                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                            ) {
                                                Icon(
                                                    imageVector = if (statusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                                    contentDescription = if (statusExpanded) "Hide" else "Show"
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                        shape = RoundedCornerShape(10.dp),
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
                                    ExposedDropdownMenu(
                                        expanded = statusExpanded,
                                        onDismissRequest = { statusExpanded = false }
                                    ) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Column {
                                                statusOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = { 
                                                            Text(
                                                                option.name,
                                                                color = if (isDarkTheme) Color.White else Color.Black
                                                            ) 
                                                        },
                                                        onClick = {
                                                            selectedStatus = option
                                                            statusExpanded = false
                                                        },
                                                        colors = MenuDefaults.itemColors(
                                                            textColor = if (isDarkTheme) Color.White else Color.Black
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Unit Cost Field
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Unit Cost",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
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
                                value = unitCostText,
                                onValueChange = { unitCostText = it },
                                placeholder = { Text("Enter unit cost (e.g., 299.99)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                    unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                                )
                            )
                        }
                        // Storage Location Field
                        DropdownField(
                            label = "Storage Location",
                            required = true,
                            selectedItem = selectedStorageLocation,
                            searchQuery = storageLocationSearchQuery,
                            onSearchQueryChange = { newValue ->
                                storageLocationSearchQuery = newValue
                                if (selectedStorageLocation != null && newValue != selectedStorageLocation?.name) {
                                    selectedStorageLocation = null
                                }
                                handleDropdownExpansion("storageLocation", true) { storageLocationExpanded = it }
                            },
                            expanded = storageLocationExpanded,
                            onExpandedChange = { handleDropdownExpansion("storageLocation", it) { storageLocationExpanded = it } },
                            placeholder = "Choose an option",
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            isLoading = isStorageLocationLoading,
                            onPositionChanged = { position, height, width ->
                                storageLocationFieldPosition = position
                                storageLocationFieldHeight = height
                                storageLocationFieldWidth = width
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    /* ---------- ACTIONS ---------- */
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            onClick = onClose,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Text(
                                text = "Cancel",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Validation: Check if all required fields are filled
                        val isSaveEnabled = selectedBrand != null &&
                                selectedModel != null &&
                                selectedCapacity != null &&
                                selectedCarrier != null &&
                                selectedColor != null &&
                                selectedStatus != null &&
                                selectedStorageLocation != null &&
                                imeiText.isNotBlank() &&
                                unitCostText.isNotBlank() &&
                                !isLoading
                        
                        // Function to create and save product
                        fun saveProduct() {
                            val brand = brandsState.find { it.id == selectedBrand?.id }
                            val model = modelsForBrand.find { it.id == selectedModel?.id }
                            val capacity = capacitiesState.find { it.id == selectedCapacity?.id }
                            val carrier = carriersState.find { it.id == selectedCarrier?.id }
                            val color = colorsState.find { it.id == selectedColor?.id }
                            val storageLocation = storageLocationsState.find { it.id == selectedStorageLocation?.id }
                            val currentStatus = selectedStatus
                            
                            if (brand != null && model != null && capacity != null && 
                                carrier != null && color != null && storageLocation != null && currentStatus != null) {
                                val unitCostValue = try {
                                    unitCostText.trim().toDouble()
                                } catch (e: Exception) {
                                    0.0
                                }
                                
                                val product = com.humblecoders.aromex_android_windows.domain.model.Product(
                                    brandId = brand.id,
                                    brandName = brand.brandName,
                                    capacityId = capacity.id,
                                    capacity = capacity.capacity,
                                    capacityUnit = selectedCapacityUnit,
                                    carrierId = carrier.id,
                                    carrierName = carrier.carrierName,
                                    colorId = color.id,
                                    colorName = color.colorName,
                                    IMEI = imeiText.trim(),
                                    modelId = model.id,
                                    modelName = model.modelName,
                                    status = currentStatus.name,
                                    storageLocationId = storageLocation.id,
                                    storageLocation = storageLocation.storageLocation,
                                    unitCost = unitCostValue
                                )
                                
                                onSaveStarted()
                                onClose()
                            }
                        }
                        
                        Button(
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp)
                                .pointerHoverIcon(PointerIcon.Hand),
                            onClick = { saveProduct() },
                            enabled = isSaveEnabled,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = Color(0xFFCCCCCC)
                            ),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Add Product",
                                color = if (!isSaveEnabled && isDarkTheme) Color(0xFF424242) else Color.Unspecified,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            
            // Dropdowns rendered outside Card (like AddExpenseDialog)
            // Brand Dropdown
            SearchableDropdown(
                items = brands,
                selectedItem = selectedBrand,
                searchQuery = brandSearchQuery,
                expanded = brandExpanded,
                onItemSelected = { item ->
                    selectedBrand = item
                    brandSearchQuery = item.name
                    brandExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    brandSearchQuery = newValue
                    if (selectedBrand != null && newValue != selectedBrand?.name) {
                        selectedBrand = null
                    }
                },
                onExpandedChange = { brandExpanded = it },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addBrand(query) { createdBrand ->
                        val newItem = DropdownItem(createdBrand.id, createdBrand.brandName)
                        selectItem(newItem)
                        selectedBrand = newItem
                        brandSearchQuery = createdBrand.brandName
                    }
                    brandExpanded = false
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "brand") },
                fieldPosition = brandFieldPosition,
                fieldHeight = brandFieldHeight,
                fieldWidth = brandFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Model Dropdown
            SearchableDropdown(
                items = models,
                selectedItem = selectedModel,
                searchQuery = modelSearchQuery,
                expanded = modelExpanded,
                onItemSelected = { item ->
                    selectedModel = item
                    modelSearchQuery = item.name
                    modelExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    modelSearchQuery = newValue
                    if (selectedModel != null && newValue != selectedModel?.name) {
                        selectedModel = null
                    }
                },
                onExpandedChange = { modelExpanded = it },
                onAddNew = { query, selectItem ->
                    // Save new model to backend (requires brand)
                    val currentBrand = selectedBrand
                    if (currentBrand != null) {
                        val brandName = brandsState.find { it.id == currentBrand.id }?.brandName ?: currentBrand.name
                        specificationViewModel.addModel(brandName, query) { createdModel ->
                            val newItem = DropdownItem(createdModel.id, createdModel.modelName)
                            selectItem(newItem)
                            selectedModel = newItem
                            modelSearchQuery = createdModel.modelName
                            // Update models list
                            modelsForBrand = modelsForBrand + createdModel
                        }
                        modelExpanded = false
                    }
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "model") },
                fieldPosition = modelFieldPosition,
                fieldHeight = modelFieldHeight,
                fieldWidth = modelFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Select a brand first",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Capacity Dropdown
            SearchableDropdown(
                items = capacities,
                selectedItem = selectedCapacity,
                searchQuery = capacitySearchQuery,
                expanded = capacityExpanded,
                onItemSelected = { item ->
                    selectedCapacity = item
                    capacitySearchQuery = item.name
                    capacityExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    capacitySearchQuery = newValue
                    if (selectedCapacity != null && newValue != selectedCapacity?.name) {
                        selectedCapacity = null
                    }
                },
                onExpandedChange = { capacityExpanded = it },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addCapacity(query) { createdCapacity ->
                        val newItem = DropdownItem(createdCapacity.id, createdCapacity.capacity)
                        selectItem(newItem)
                        selectedCapacity = newItem
                        capacitySearchQuery = createdCapacity.capacity
                    }
                    capacityExpanded = false
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "capacity") },
                fieldPosition = capacityFieldPosition,
                fieldHeight = capacityFieldHeight,
                fieldWidth = capacityFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Carrier Dropdown
            SearchableDropdown(
                items = carriers,
                selectedItem = selectedCarrier,
                searchQuery = carrierSearchQuery,
                expanded = carrierExpanded,
                onItemSelected = { item ->
                    selectedCarrier = item
                    carrierSearchQuery = item.name
                    carrierExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    carrierSearchQuery = newValue
                    if (selectedCarrier != null && newValue != selectedCarrier?.name) {
                        selectedCarrier = null
                    }
                },
                onExpandedChange = { carrierExpanded = it },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addCarrier(query) { createdCarrier ->
                        val newItem = DropdownItem(createdCarrier.id, createdCarrier.carrierName)
                        selectItem(newItem)
                        selectedCarrier = newItem
                        carrierSearchQuery = createdCarrier.carrierName
                    }
                    carrierExpanded = false
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "carrier") },
                fieldPosition = carrierFieldPosition,
                fieldHeight = carrierFieldHeight,
                fieldWidth = carrierFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Color Dropdown
            SearchableDropdown(
                items = colors,
                selectedItem = selectedColor,
                searchQuery = colorSearchQuery,
                expanded = colorExpanded,
                onItemSelected = { item ->
                    selectedColor = item
                    colorSearchQuery = item.name
                    colorExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    colorSearchQuery = newValue
                    if (selectedColor != null && newValue != selectedColor?.name) {
                        selectedColor = null
                    }
                },
                onExpandedChange = { colorExpanded = it },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addColor(query) { createdColor ->
                        val newItem = DropdownItem(createdColor.id, createdColor.colorName)
                        selectItem(newItem)
                        selectedColor = newItem
                        colorSearchQuery = createdColor.colorName
                    }
                    colorExpanded = false
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "color") },
                fieldPosition = colorFieldPosition,
                fieldHeight = colorFieldHeight,
                fieldWidth = colorFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Storage Location Dropdown
            SearchableDropdown(
                items = storageLocations,
                selectedItem = selectedStorageLocation,
                searchQuery = storageLocationSearchQuery,
                expanded = storageLocationExpanded,
                onItemSelected = { item ->
                    selectedStorageLocation = item
                    storageLocationSearchQuery = item.name
                    storageLocationExpanded = false
                },
                onSearchQueryChange = { newValue ->
                    storageLocationSearchQuery = newValue
                    if (selectedStorageLocation != null && newValue != selectedStorageLocation?.name) {
                        selectedStorageLocation = null
                    }
                },
                onExpandedChange = { storageLocationExpanded = it },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addStorageLocation(query) { createdStorageLocation ->
                        val newItem = DropdownItem(createdStorageLocation.id, createdStorageLocation.storageLocation)
                        selectItem(newItem)
                        selectedStorageLocation = newItem
                        storageLocationSearchQuery = createdStorageLocation.storageLocation
                    }
                    storageLocationExpanded = false
                },
                onEditItem = { item -> showEditItemDialog(item.id, item.name, "storage location") },
                fieldPosition = storageLocationFieldPosition,
                fieldHeight = storageLocationFieldHeight,
                fieldWidth = storageLocationFieldWidth,
                rootPosition = rootPosition,
                density = density,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false
            )
            
            // Edit Item Dialog
            if (showEditDialog) {
                EditItemDialog(
                    title = editDialogTitle,
                    currentValue = editDialogCurrentValue,
                    isDarkTheme = isDarkTheme,
                    onDismiss = { showEditDialog = false },
                    onSave = { newValue -> handleEditSave(newValue) }
                )
            }
            }
        }
    }
}

@Composable
private fun DropdownField(
    label: String,
    required: Boolean,
    selectedItem: DropdownItem?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    placeholder: String,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onPositionChanged: (Offset, Dp, Dp) -> Unit
) {
    val density = LocalDensity.current
    val selected = selectedItem
    val displayValue = selected?.name ?: searchQuery
    
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflowCompose.Ellipsis
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .onGloballyPositioned { coordinates ->
                    onPositionChanged(
                        coordinates.positionInRoot(),
                        with(density) { coordinates.size.height.toDp() },
                        with(density) { coordinates.size.width.toDp() }
                    )
                }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = { if (enabled) onSearchQueryChange(it) },
                enabled = enabled,
                placeholder = {
                    if (selected == null) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflowCompose.Ellipsis
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && !expanded) {
                            onExpandedChange(true)
                        }
                    },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = { if (enabled) onExpandedChange(!expanded) },
                            enabled = enabled,
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = if (expanded) "Hide" else "Show"
                            )
                        }
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
        }
    }
}

@Composable
private fun CapacityFieldManual(
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier,
    selectedCapacity: DropdownItem?,
    capacitySearchQuery: String,
    onCapacitySearchQueryChange: (String) -> Unit,
    capacityExpanded: Boolean,
    onCapacityExpandedChange: (Boolean) -> Unit,
    onCapacitySelected: (DropdownItem?) -> Unit,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    capacities: List<DropdownItem>,
    onPositionChanged: (Offset, Dp, Dp) -> Unit,
    specificationViewModel: SpecificationViewModel,
    isLoading: Boolean = false
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val selected = selectedCapacity
    val displayValue = selected?.name ?: capacitySearchQuery
    
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Capacity *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else Color.Black
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .onGloballyPositioned { coordinates ->
                    onPositionChanged(
                        coordinates.positionInRoot(),
                        with(density) { coordinates.size.height.toDp() },
                        with(density) { coordinates.size.width.toDp() }
                    )
                }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = onCapacitySearchQueryChange,
                placeholder = {
                    if (selected == null) {
                        Text(
                            text = "Choose an option",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflowCompose.Ellipsis
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState: FocusState ->
                        if (focusState.isFocused && !capacityExpanded) {
                            onCapacityExpandedChange(true)
                        }
                    },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = { onCapacityExpandedChange(!capacityExpanded) },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Icon(
                                imageVector = if (capacityExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = if (capacityExpanded) "Hide" else "Show"
                            )
                        }
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
            
            // Inline GB/TB buttons positioned inside the field
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp), // Space for dropdown arrow
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("GB", "TB").forEach { unit ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (selectedUnit == unit) Color(0xFF2F80ED)
                                else if (isDarkTheme) Color(0xFFFAFAFA)
                                else Color(0xFFF2F2F2)
                            )
                            .border(
                                width = if (selectedUnit == unit) 0.dp else 1.dp,
                                color = if (selectedUnit == unit) Color.Transparent else Color(0xFFE8E8E8),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onUnitSelected(unit) }
                            .pointerHoverIcon(PointerIcon.Hand)
                            .height(32.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit,
                            fontSize = 12.sp,
                            fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedUnit == unit) Color.White else Color(0xFF424242)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    placeholder: String,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else Color.Black
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            enabled = false,
            placeholder = {
                Text(
                    placeholder,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Left
                )
            },
            textStyle = TextStyle(
                textAlign = TextAlign.Left,
                color = if (isDarkTheme) Color.White else Color.Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
            )
        )
    }
}

