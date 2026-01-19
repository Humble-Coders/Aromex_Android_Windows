package com.humblecoders.aromex_android_windows.presentation.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.collectAsState
import com.humblecoders.aromex_android_windows.presentation.viewmodel.SpecificationViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.animation.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// Helper function for success color based on theme
@Composable
private fun getAromexSuccessColor(isDarkTheme: Boolean): Color {
    return if (isDarkTheme) Color(0xFF66BB6A) else Color(0xFF4CAF50)
}

// Simple data class for dropdown items
data class DropdownItem(
    val id: String,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductSheet(
    isDarkTheme: Boolean = false,
    specificationViewModel: SpecificationViewModel,
    onDismiss: () -> Unit
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
    
    // Models state - fetched when brand is selected (declared early so it can be used in LaunchedEffect)
    var modelsForBrand by remember { mutableStateOf<List<com.humblecoders.aromex_android_windows.domain.model.Model>>(emptyList()) }
    
    // Track if we've triggered a data fetch for this sheet session
    var fetchTriggered by remember { mutableStateOf(false) }
    // Track if fields have been reset initially (to prevent resetting after adding items)
    var fieldsReset by remember { mutableStateOf(false) }
    
    // Fetch data when sheet opens
    LaunchedEffect(Unit) {
        if (!fetchTriggered) {
            println("[AddProductSheet] 📥 Fetching specification data for sheet...")
            fetchTriggered = true
            fieldsReset = false
            specificationViewModel.fetchAllSpecifications()
        }
    }
    
    // Individual field loading states using per-collection loading states (Improvement #4)
    val isBrandLoading = brandsLoading
    val isCapacityLoading = capacitiesLoading
    val isCarrierLoading = carriersLoading
    val isColorLoading = colorsLoading
    val isStorageLocationLoading = storageLocationsLoading
    
    // Reset all fields when sheet opens (only after initial data is loaded, not after subsequent operations)
    LaunchedEffect(isLoading) {
        if (!isLoading && fetchTriggered && !fieldsReset) {
            println("[AddProductSheet] 🚀 Add Product Sheet opened - resetting all fields")
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
            
            // Clear models
            modelsForBrand = emptyList()
            
            // Clear error and success messages
            specificationViewModel.clearError()
            specificationViewModel.clearSuccessMessage()
            
            // Mark fields as reset to prevent resetting again after adding items
            fieldsReset = true
        }
    }
    
    // Auto-dismiss success message (without closing sheet)
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            delay(2500) // Show success message for 2.5 seconds
            specificationViewModel.clearSuccessMessage()
        }
    }
    
    // Collect data from SpecificationViewModel
    val brandsState by specificationViewModel.brands.collectAsState()
    val capacitiesState by specificationViewModel.capacities.collectAsState()
    val carriersState by specificationViewModel.carriers.collectAsState()
    val colorsState by specificationViewModel.colors.collectAsState()
    val storageLocationsState by specificationViewModel.storageLocations.collectAsState()
    
    // Log when data is fetched
    LaunchedEffect(brandsState.size, capacitiesState.size, carriersState.size, colorsState.size, storageLocationsState.size) {
        println("[AddProductSheet] 📊 Data fetched - Brands: ${brandsState.size}, Capacities: ${capacitiesState.size}, Carriers: ${carriersState.size}, Colors: ${colorsState.size}, StorageLocations: ${storageLocationsState.size}")
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
    
    // Fetch models when brand is selected
    LaunchedEffect(selectedBrand?.id) {
        val currentBrand = selectedBrand
        if (currentBrand == null) {
            println("[AddProductSheet] 🔄 Brand deselected - clearing models")
            modelsForBrand = emptyList()
            selectedModel = null
        } else {
            println("[AddProductSheet] 🔍 Fetching models for brand: ${currentBrand.name} (ID: ${currentBrand.id})")
            // Fetch models for the selected brand
            val result = specificationViewModel.fetchModelsByBrand(currentBrand.id)
            result.fold(
                onSuccess = { models: List<com.humblecoders.aromex_android_windows.domain.model.Model> ->
                    println("[AddProductSheet] ✅ Models fetched successfully: ${models.size} models found for brand '${currentBrand.name}'")
                    modelsForBrand = models
                    // Reset model selection if current model doesn't belong to new brand
                    val currentModelId = selectedModel?.id
                    if (currentModelId != null) {
                        val modelStillValid = models.any { it.id == currentModelId }
                        if (!modelStillValid) {
                            println("[AddProductSheet] ⚠️ Current model selection invalid for new brand - clearing selection")
                            selectedModel = null
                        }
                    }
                },
                onFailure = { error ->
                    println("[AddProductSheet] ❌ Failed to fetch models for brand '${currentBrand.name}': ${error.message}")
                    modelsForBrand = emptyList()
                    selectedModel = null
                }
            )
        }
    }
    
    val models = modelsForBrand.map { DropdownItem(it.id, it.modelName) }
    
    // Root position for dropdown positioning
    var rootPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Focus management - focus brand field when dialog opens
    val brandFocusRequester = remember { FocusRequester() }
    
    // Track which dropdown is expanded to ensure only one is open at a time
    var expandedDropdownId by remember { mutableStateOf<String?>(null) }
    
    // Edit dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var editDialogTitle by remember { mutableStateOf("") }
    var editDialogCurrentValue by remember { mutableStateOf("") }
    var editItemId by remember { mutableStateOf<String?>(null) }
    var editItemType by remember { mutableStateOf<String?>(null) }
    
    // Helper function to handle dropdown expansion
    fun handleDropdownExpansion(dropdownId: String, isExpanded: Boolean) {
        if (isExpanded) {
            expandedDropdownId = dropdownId
        } else if (expandedDropdownId == dropdownId) {
            expandedDropdownId = null
        }
    }
    
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
        
        // Update selected items if they match
        if (selectedBrand?.id == id) selectedBrand = DropdownItem(id, newValue)
        if (selectedCapacity?.id == id) selectedCapacity = DropdownItem(id, newValue)
        if (selectedCarrier?.id == id) selectedCarrier = DropdownItem(id, newValue)
        if (selectedColor?.id == id) selectedColor = DropdownItem(id, newValue)
        if (selectedStorageLocation?.id == id) selectedStorageLocation = DropdownItem(id, newValue)
        if (selectedStatus?.id == id) selectedStatus = DropdownItem(id, newValue)
        if (selectedModel?.id == id) {
            selectedModel = DropdownItem(id, newValue)
            // Update models list
            modelsForBrand = modelsForBrand.map { if (it.id == id) it.copy(modelName = newValue) else it }
        }
        
        showEditDialog = false
    }
    
    // Focus brand field when sheet opens
    LaunchedEffect(Unit) {
        brandFocusRequester.requestFocus()
    }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    rootPosition = coordinates.positionInRoot()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Header
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
                            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
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

                /* ---------- FIELDS - ONE PER ROW ---------- */
                SearchableDropdownField(
                    items = brands,
                    selectedItem = selectedBrand,
                    onItemSelected = { selectedBrand = it },
                    onAddNew = { query, selectItem ->
                        specificationViewModel.addBrand(query) { createdBrand ->
                            val newItem = DropdownItem(createdBrand.id, createdBrand.brandName)
                        selectItem(newItem)
                        selectedBrand = newItem
                        }
                    },
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "brand") },
                    modifier = Modifier.fillMaxWidth(),
                    rootPosition = rootPosition,
                    isDarkTheme = isDarkTheme,
                    placeholder = "Choose an option",
                    label = "Brand",
                    required = true,
                    getItemDisplayName = { it.name },
                    getItemId = { it.id },
                    showRolePill = false,
                    fieldHeight = 60.dp,
                    focusRequester = brandFocusRequester,
                    externalExpanded = expandedDropdownId == "brand",
                    onExpandedChange = { isExpanded -> handleDropdownExpansion("brand", isExpanded) },
                    isLoading = isBrandLoading
                )
                SearchableDropdownField(
                    items = models,
                    selectedItem = selectedModel,
                    onItemSelected = { selectedModel = it },
                    onAddNew = { query, selectItem ->
                        // Save new model to backend (requires brand)
                        val currentBrand = selectedBrand
                        if (currentBrand != null) {
                            val brandsState = specificationViewModel.brands.value
                            val brandName = brandsState.find { it.id == currentBrand.id }?.brandName ?: currentBrand.name
                            specificationViewModel.addModel(brandName, query) { createdModel ->
                                val newItem = DropdownItem(createdModel.id, createdModel.modelName)
                        selectItem(newItem)
                        selectedModel = newItem
                                // Update models list
                                modelsForBrand = modelsForBrand + createdModel
                            }
                        }
                    },
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "model") },
                    modifier = Modifier.fillMaxWidth(),
                    rootPosition = rootPosition,
                    isDarkTheme = isDarkTheme,
                    placeholder = "Select a brand first",
                    label = "Model",
                    required = true,
                    getItemDisplayName = { it.name },
                    getItemId = { it.id },
                    showRolePill = false,
                    fieldHeight = 60.dp,
                    enabled = selectedBrand != null,
                    externalExpanded = if (selectedBrand != null) expandedDropdownId == "model" else false,
                    onExpandedChange = { isExpanded -> 
                        if (selectedBrand != null) {
                            handleDropdownExpansion("model", isExpanded)
                        }
                    },
                    onSearchQueryChange = { newValue ->
                        // Clear selection if user types something different
                        val currentModel = selectedModel // Local variable for smart cast
                        if (currentModel != null && newValue != currentModel.name) {
                            selectedModel = null
                        }
                    },
                    isLoading = isBrandLoading
                )
                CapacityField(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.fillMaxWidth(),
                    selectedCapacity = selectedCapacity,
                    onCapacitySelected = { selectedCapacity = it },
                    selectedUnit = selectedCapacityUnit,
                    onUnitSelected = { selectedCapacityUnit = it },
                    capacities = capacities,
                    rootPosition = rootPosition,
                    expandedDropdownId = expandedDropdownId,
                    onExpandedChange = { isExpanded -> handleDropdownExpansion("capacity", isExpanded) },
                    specificationViewModel = specificationViewModel,
                    isLoading = isCapacityLoading,
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "capacity") }
                )
                // IMEI Field
                Column(Modifier.fillMaxWidth()) {
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
                SearchableDropdownField(
                    items = carriers,
                    selectedItem = selectedCarrier,
                    onItemSelected = { selectedCarrier = it },
                    onAddNew = { query, selectItem ->
                        specificationViewModel.addCarrier(query) { createdCarrier ->
                            val newItem = DropdownItem(createdCarrier.id, createdCarrier.carrierName)
                        selectItem(newItem)
                        selectedCarrier = newItem
                        }
                    },
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "carrier") },
                    modifier = Modifier.fillMaxWidth(),
                    rootPosition = rootPosition,
                    isDarkTheme = isDarkTheme,
                    placeholder = "Choose an option",
                    label = "Carrier",
                    required = true,
                    getItemDisplayName = { it.name },
                    getItemId = { it.id },
                    showRolePill = false,
                    fieldHeight = 60.dp,
                    externalExpanded = expandedDropdownId == "carrier",
                    onExpandedChange = { isExpanded -> handleDropdownExpansion("carrier", isExpanded) },
                    isLoading = isCarrierLoading
                )
                SearchableDropdownField(
                    items = colors,
                    selectedItem = selectedColor,
                    onItemSelected = { selectedColor = it },
                    onAddNew = { query, selectItem ->
                        specificationViewModel.addColor(query) { createdColor ->
                            val newItem = DropdownItem(createdColor.id, createdColor.colorName)
                        selectItem(newItem)
                        selectedColor = newItem
                        }
                    },
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "color") },
                    modifier = Modifier.fillMaxWidth(),
                    rootPosition = rootPosition,
                    isDarkTheme = isDarkTheme,
                    placeholder = "Choose an option",
                    label = "Color",
                    required = true,
                    getItemDisplayName = { it.name },
                    getItemId = { it.id },
                    showRolePill = false,
                    fieldHeight = 60.dp,
                    externalExpanded = expandedDropdownId == "color",
                    onExpandedChange = { isExpanded -> handleDropdownExpansion("color", isExpanded) },
                    isLoading = isColorLoading
                )
                // Status Field
                Column(Modifier.fillMaxWidth()) {
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
                    var statusExpanded by remember { mutableStateOf(false) }
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
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { statusExpanded = !statusExpanded }
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
                Column(Modifier.fillMaxWidth()) {
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
                SearchableDropdownField(
                    items = storageLocations,
                    selectedItem = selectedStorageLocation,
                    onItemSelected = { selectedStorageLocation = it },
                    onAddNew = { query, selectItem ->
                        specificationViewModel.addStorageLocation(query) { createdStorageLocation ->
                            val newItem = DropdownItem(createdStorageLocation.id, createdStorageLocation.storageLocation)
                            selectItem(newItem)
                            selectedStorageLocation = newItem
                        }
                    },
                    onEditItem = { item -> showEditItemDialog(item.id, item.name, "storage location") },
                    modifier = Modifier.fillMaxWidth(),
                    rootPosition = rootPosition,
                    isDarkTheme = isDarkTheme,
                    placeholder = "Choose an option",
                    label = "Storage Location",
                    required = true,
                    getItemDisplayName = { it.name },
                    getItemId = { it.id },
                    showRolePill = false,
                    fieldHeight = 60.dp,
                    externalExpanded = expandedDropdownId == "storageLocation",
                    onExpandedChange = { isExpanded -> handleDropdownExpansion("storageLocation", isExpanded) },
                    isLoading = isStorageLocationLoading
                )

                Spacer(Modifier.height(12.dp))

                /* ---------- ACTION BUTTONS ---------- */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 15.sp,
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
                        val brandsState = specificationViewModel.brands.value
                        val modelsState = modelsForBrand
                        val capacitiesState = specificationViewModel.capacities.value
                        val carriersState = specificationViewModel.carriers.value
                        val colorsState = specificationViewModel.colors.value
                        val storageLocationsState = specificationViewModel.storageLocations.value
                        
                        val brand = brandsState.find { it.id == selectedBrand?.id }
                        val model = modelsState.find { it.id == selectedModel?.id }
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
                            
                            // Close the sheet
                            onDismiss()
                        }
                    }
                    
                    Button(
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp),
                        onClick = { saveProduct() },
                        shape = RoundedCornerShape(10.dp),
                        enabled = isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color(0xFFCCCCCC)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Add Product",
                            color = if (!isSaveEnabled && isDarkTheme) Color(0xFF424242) else Color.Unspecified,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
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

@Composable
private fun Field(
    label: String,
    placeholder: String,
    isDarkTheme: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color.White else AromexColors.TextDark()
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
                .height(50.dp),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.BackgroundGrey(),
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
            )
        )
    }
}

@Composable
private fun CapacityField(
    isDarkTheme: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    selectedCapacity: DropdownItem?,
    onCapacitySelected: (DropdownItem?) -> Unit,
    selectedUnit: String,
    onUnitSelected: (String) -> Unit,
    capacities: List<DropdownItem>,
    rootPosition: Offset,
    expandedDropdownId: String?,
    onExpandedChange: (Boolean) -> Unit,
    specificationViewModel: SpecificationViewModel,
    isLoading: Boolean = false,
    onEditItem: ((DropdownItem) -> Unit)? = null
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Capacity *",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else AromexColors.TextDark()
            )
        }
        Spacer(Modifier.height(8.dp))
        // Capacity field with inline GB/TB buttons
        Box(modifier = Modifier.fillMaxWidth()) {
            SearchableDropdownField(
                items = capacities,
                selectedItem = selectedCapacity,
                onItemSelected = { onCapacitySelected(it) },
                onAddNew = { query, selectItem ->
                    specificationViewModel.addCapacity(query) { createdCapacity ->
                        val newItem = DropdownItem(createdCapacity.id, createdCapacity.capacity)
                        selectItem(newItem)
                        onCapacitySelected(newItem)
                    }
                },
                onEditItem = onEditItem,
                modifier = Modifier.fillMaxWidth(),
                rootPosition = rootPosition,
                isDarkTheme = isDarkTheme,
                placeholder = "Choose an option",
                label = null,
                required = false,
                getItemDisplayName = { it.name },
                getItemId = { it.id },
                showRolePill = false,
                fieldHeight = 60.dp,
                externalExpanded = expandedDropdownId == "capacity",
                onExpandedChange = onExpandedChange,
                isLoading = isLoading
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
                            .height(32.dp)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit,
                            color = if (selectedUnit == unit) Color.White else Color(0xFF424242),
                            fontWeight = if (selectedUnit == unit) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

