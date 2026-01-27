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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

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
    var brandSearchQuery by remember { mutableStateOf("") }
    var brandExpanded by remember { mutableStateOf(false) }
    var brandFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var brandFieldHeight by remember { mutableStateOf(0.dp) }
    var brandFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedModel by remember { mutableStateOf<DropdownItem?>(null) }
    var modelSearchQuery by remember { mutableStateOf("") }
    var modelExpanded by remember { mutableStateOf(false) }
    var modelFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var modelFieldHeight by remember { mutableStateOf(0.dp) }
    var modelFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedCapacity by remember { mutableStateOf<DropdownItem?>(null) }
    var capacitySearchQuery by remember { mutableStateOf("") }
    var capacityExpanded by remember { mutableStateOf(false) }
    var capacityFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var capacityFieldHeight by remember { mutableStateOf(0.dp) }
    var capacityFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedCarrier by remember { mutableStateOf<DropdownItem?>(null) }
    var carrierSearchQuery by remember { mutableStateOf("") }
    var carrierExpanded by remember { mutableStateOf(false) }
    var carrierFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var carrierFieldHeight by remember { mutableStateOf(0.dp) }
    var carrierFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedColor by remember { mutableStateOf<DropdownItem?>(null) }
    var colorSearchQuery by remember { mutableStateOf("") }
    var colorExpanded by remember { mutableStateOf(false) }
    var colorFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var colorFieldHeight by remember { mutableStateOf(0.dp) }
    var colorFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedStatus by remember { mutableStateOf<DropdownItem?>(null) }
    var statusSearchQuery by remember { mutableStateOf("") }
    var statusExpanded by remember { mutableStateOf(false) }
    var statusFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var statusFieldHeight by remember { mutableStateOf(0.dp) }
    var statusFieldWidth by remember { mutableStateOf(0.dp) }
    var selectedStorageLocation by remember { mutableStateOf<DropdownItem?>(null) }
    var storageLocationSearchQuery by remember { mutableStateOf("") }
    var storageLocationExpanded by remember { mutableStateOf(false) }
    var storageLocationFieldPosition by remember { mutableStateOf(Offset.Zero) }
    var storageLocationFieldHeight by remember { mutableStateOf(0.dp) }
    var storageLocationFieldWidth by remember { mutableStateOf(0.dp) }
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
            brandSearchQuery = ""
            brandExpanded = false
            selectedModel = null
            modelSearchQuery = ""
            modelExpanded = false
            selectedCapacity = null
            capacitySearchQuery = ""
            capacityExpanded = false
            selectedCarrier = null
            carrierSearchQuery = ""
            carrierExpanded = false
            selectedColor = null
            colorSearchQuery = ""
            colorExpanded = false
            selectedStatus = DropdownItem("active", "Active") // Set Active as default
            statusSearchQuery = "Active"
            statusExpanded = false
            selectedStorageLocation = null
            storageLocationSearchQuery = ""
            storageLocationExpanded = false
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

    // Initialize status to "Active" when screen first opens
    LaunchedEffect(Unit) {
        if (selectedStatus == null) {
            selectedStatus = DropdownItem("active", "Active")
            statusSearchQuery = "Active"
        }
    }

    // Fetch models when brand is selected
    LaunchedEffect(selectedBrand?.id) {
        val currentBrand = selectedBrand
        if (currentBrand == null) {
            println("[AddProductSheet] 🔄 Brand deselected - clearing models")
            modelsForBrand = emptyList()
            selectedModel = null
            modelSearchQuery = ""
            modelExpanded = false
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
                            modelSearchQuery = ""
                            modelExpanded = false
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

    // Focus management - focus requesters for all fields
    val brandFocusRequester = remember { FocusRequester() }
    val modelFocusRequester = remember { FocusRequester() }
    val capacityFocusRequester = remember { FocusRequester() }
    val carrierFocusRequester = remember { FocusRequester() }
    val colorFocusRequester = remember { FocusRequester() }
    val statusFocusRequester = remember { FocusRequester() }
    val storageLocationFocusRequester = remember { FocusRequester() }
    val imeiFocusRequester = remember { FocusRequester() }
    val unitCostFocusRequester = remember { FocusRequester() }

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

    // Discard dialog state
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }

    // Check if any fields have content (exclude default status "Active")
    val hasChanges = selectedBrand != null || selectedModel != null || selectedCapacity != null ||
            selectedCarrier != null || selectedColor != null || 
            (selectedStatus != null && selectedStatus?.id != "active") ||
            selectedStorageLocation != null || imeiText.isNotBlank() || unitCostText.isNotBlank()

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

    // Focus brand field when screen opens
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        brandFocusRequester.requestFocus()
    }

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
                            text = "Add Product",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Save/Next Button
                val isAllFieldsFilled = selectedBrand != null && selectedModel != null &&
                        selectedCapacity != null && selectedCarrier != null &&
                        selectedColor != null && selectedStatus != null &&
                        selectedStorageLocation != null && imeiText.isNotBlank() &&
                        unitCostText.isNotBlank()

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

                fun moveToNextField() {
                    when {
                        selectedBrand == null -> brandFocusRequester.requestFocus()
                        selectedModel == null -> modelFocusRequester.requestFocus()
                        selectedCapacity == null -> capacityFocusRequester.requestFocus()
                        selectedCarrier == null -> carrierFocusRequester.requestFocus()
                        selectedColor == null -> colorFocusRequester.requestFocus()
                        selectedStatus == null -> statusFocusRequester.requestFocus()
                        selectedStorageLocation == null -> storageLocationFocusRequester.requestFocus()
                        imeiText.isBlank() -> imeiFocusRequester.requestFocus()
                        unitCostText.isBlank() -> unitCostFocusRequester.requestFocus()
                        else -> {
                            // All fields filled, save the product
                            saveProduct()
                        }
                    }
                }

                TextButton(
                    onClick = {
                        if (isAllFieldsFilled) {
                            saveProduct()
                        }
                    },
                    enabled = isAllFieldsFilled
                ) {
                    val textColor = when {
                        isAllFieldsFilled && isSystemInDarkTheme() ->
                            Color(0xFF6EA8FF)   // bright blue (dark)

                        isAllFieldsFilled && !isSystemInDarkTheme() ->
                            Color(0xFF2563EB)   // blue (light)

                        !isAllFieldsFilled && isSystemInDarkTheme() ->
                            Color(0xFF5A5A5A)   // muted gray (dark)

                        else ->
                            Color(0xFFB0B0B0)   // light gray (light)
                    }

                    Text(
                        text = "Save",
                        color = textColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
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
                    .verticalScroll(scrollState)
                    .onGloballyPositioned { coordinates ->
                        rootPosition = coordinates.positionInRoot()
                    }
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                // Title and Subtitle
                Text(
                    text = "Add New Product",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Enter product details to add to inventory",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

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
                    Spacer(modifier = Modifier.height(16.dp))
                }

                /* ---------- FIELDS - ONE PER ROW ---------- */
                // Brand Field - Using OutlinedTextField + SearchableDropdown pattern
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Brand",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedBrand
                    val displayValue = (selected?.name ?: brandSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                brandFieldPosition = coordinates.positionInRoot()
                                brandFieldHeight = with(density) { coordinates.size.height.toDp() }
                                brandFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                brandSearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedBrand = null
                                }
                                brandExpanded = true
                                handleDropdownExpansion("brand", true)
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
                                .focusRequester(brandFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !brandExpanded) {
                                        brandExpanded = true
                                        handleDropdownExpansion("brand", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { modelFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    brandExpanded = !brandExpanded
                                    handleDropdownExpansion("brand", !brandExpanded)
                                }) {
                                    Icon(
                                        imageVector = if (brandExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (brandExpanded) "Hide" else "Show"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Brand Dropdown - Inside Column flow to push content down
                    if (brandExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Container positioned inline in Column flow
                        var dropdownContainerPosition by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    dropdownContainerPosition = coordinates.positionInRoot()
                                }
                        ) {
                            // Calculate fieldPosition so dropdown appears at container top
                            // SearchableDropdown calculates: offset = (fieldPosition - rootPosition) + fieldHeight + 4.dp
                            // To make dropdown appear at container top (offset = 0,0):
                            // fieldPosition.x = dropdownContainerPosition.x
                            // fieldPosition.y = dropdownContainerPosition.y - fieldHeight - 4.dp
                            val adjustedFieldPosition = Offset(
                                x = dropdownContainerPosition.x,
                                y = dropdownContainerPosition.y - with(density) { brandFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = brands,
                                selectedItem = selectedBrand,
                                searchQuery = brandSearchQuery,
                                expanded = brandExpanded,
                                onItemSelected = { item ->
                                    selectedBrand = item
                                    brandSearchQuery = item.name.trim()
                                    brandExpanded = false
                                    handleDropdownExpansion("brand", false)
                                    // Jump to model field after selection
                                    modelFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    brandSearchQuery = newValue
                                    if (selectedBrand != null && newValue != selectedBrand?.name) {
                                        selectedBrand = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    brandExpanded = expanded
                                    handleDropdownExpansion("brand", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    specificationViewModel.addBrand(searchQuery) { createdBrand ->
                                        val newItem = DropdownItem(createdBrand.id, createdBrand.brandName)
                                        selectItem(newItem)
                                        selectedBrand = newItem
                                        brandSearchQuery = createdBrand.brandName.trim()
                                        // Jump to model field after adding
                                        modelFocusRequester.requestFocus()
                                    }
                                    brandExpanded = false
                                    handleDropdownExpansion("brand", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = brandFieldHeight,
                                fieldWidth = brandFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "brand") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Model Field - Using OutlinedTextField + SearchableDropdown pattern (same as Brand)
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Model",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selectedModelLocal = selectedModel
                    val displayValue = (selectedModelLocal?.name ?: modelSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                modelFieldPosition = coordinates.positionInRoot()
                                modelFieldHeight = with(density) { coordinates.size.height.toDp() }
                                modelFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                if (selectedBrand != null) {
                                    modelSearchQuery = newValue
                                    if (selectedModelLocal != null && newValue != selectedModelLocal.name) {
                                        selectedModel = null
                                    }
                                    modelExpanded = true
                                    handleDropdownExpansion("model", true)
                                }
                            },
                            enabled = selectedBrand != null,
                            placeholder = {
                                if (selectedModelLocal == null) {
                                    Text(
                                        text = if (selectedBrand != null) "Choose an option" else "Select a brand first",
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
                                .focusRequester(modelFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (selectedBrand != null && focusState.isFocused && !modelExpanded) {
                                        modelExpanded = true
                                        handleDropdownExpansion("model", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { capacityFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isBrandLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            if (selectedBrand != null) {
                                                modelExpanded = !modelExpanded
                                                handleDropdownExpansion("model", !modelExpanded)
                                            }
                                        },
                                        enabled = selectedBrand != null
                                    ) {
                                        Icon(
                                            imageVector = if (modelExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (modelExpanded) "Hide" else "Show"
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Model Dropdown - Inside Column flow to push content down
                    if (modelExpanded && selectedBrand != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Container positioned inline in Column flow
                        var dropdownContainerPosition by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    dropdownContainerPosition = coordinates.positionInRoot()
                                }
                        ) {
                            // Calculate fieldPosition so dropdown appears at container top
                            val adjustedFieldPosition = Offset(
                                x = dropdownContainerPosition.x,
                                y = dropdownContainerPosition.y - with(density) { modelFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = models,
                                selectedItem = selectedModel,
                                searchQuery = modelSearchQuery,
                                expanded = modelExpanded,
                                onItemSelected = { item ->
                                    selectedModel = item
                                    modelSearchQuery = item.name.trim()
                                    modelExpanded = false
                                    handleDropdownExpansion("model", false)
                                    // Jump to capacity field after selection
                                    capacityFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    modelSearchQuery = newValue
                                    if (selectedModelLocal != null && newValue != selectedModelLocal.name) {
                                        selectedModel = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    modelExpanded = expanded
                                    handleDropdownExpansion("model", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    // Save new model to backend (requires brand)
                                    val currentBrand = selectedBrand
                                    if (currentBrand != null) {
                                        val brandsState = specificationViewModel.brands.value
                                        val brandName = brandsState.find { it.id == currentBrand.id }?.brandName ?: currentBrand.name
                                        specificationViewModel.addModel(brandName, searchQuery) { createdModel ->
                                            val newItem = DropdownItem(createdModel.id, createdModel.modelName)
                                            selectItem(newItem)
                                            selectedModel = newItem
                                            modelSearchQuery = createdModel.modelName.trim()
                                            // Update models list
                                            modelsForBrand = modelsForBrand + createdModel
                                            // Jump to capacity field after adding
                                            capacityFocusRequester.requestFocus()
                                        }
                                    }
                                    modelExpanded = false
                                    handleDropdownExpansion("model", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = modelFieldHeight,
                                fieldWidth = modelFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "model") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Capacity Field
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Capacity",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedCapacity
                    val displayValue = (selected?.name ?: capacitySearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                capacityFieldPosition = coordinates.positionInRoot()
                                capacityFieldHeight = with(density) { coordinates.size.height.toDp() }
                                capacityFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                capacitySearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedCapacity = null
                                }
                                capacityExpanded = true
                                handleDropdownExpansion("capacity", true)
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
                                .focusRequester(capacityFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !capacityExpanded) {
                                        capacityExpanded = true
                                        handleDropdownExpansion("capacity", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { carrierFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // GB/TB unit selector buttons - Improved design
                                    CapacityUnitSelector(
                                        selectedUnit = selectedCapacityUnit,
                                        isDarkTheme = isDarkTheme
                                    ) { unit ->
                                        selectedCapacityUnit = unit
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(onClick = {
                                        capacityExpanded = !capacityExpanded
                                        handleDropdownExpansion("capacity", !capacityExpanded)
                                    }) {
                                        Icon(
                                            imageVector = if (capacityExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (capacityExpanded) "Hide" else "Show"
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Capacity Dropdown - Inside Column flow to push content down
                    if (capacityExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                y = dropdownContainerPosition.y - with(density) { capacityFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = capacities,
                                selectedItem = selectedCapacity,
                                searchQuery = capacitySearchQuery,
                                expanded = capacityExpanded,
                                onItemSelected = { item ->
                                    selectedCapacity = item
                                    capacitySearchQuery = item.name.trim()
                                    capacityExpanded = false
                                    handleDropdownExpansion("capacity", false)
                                    carrierFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    capacitySearchQuery = newValue
                                    if (selectedCapacity != null && newValue != selectedCapacity?.name) {
                                        selectedCapacity = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    capacityExpanded = expanded
                                    handleDropdownExpansion("capacity", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    specificationViewModel.addCapacity(searchQuery) { createdCapacity ->
                                        val newItem = DropdownItem(createdCapacity.id, createdCapacity.capacity)
                                        selectItem(newItem)
                                        selectedCapacity = newItem
                                        capacitySearchQuery = createdCapacity.capacity.trim()
                                    }
                                    capacityExpanded = false
                                    handleDropdownExpansion("capacity", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = capacityFieldHeight,
                                fieldWidth = capacityFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "capacity") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val isDark = isSystemInDarkTheme()
                    OutlinedTextField(
                        value = imeiText,
                        onValueChange = { imeiText = it },
                        placeholder = {
                            Text(
                                "Enter IMEI or Serial num",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .focusRequester(imeiFocusRequester),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { unitCostFocusRequester.requestFocus() }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = if (isDark) Color.White else Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Carrier Field
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Carrier",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedCarrier
                    val displayValue = (selected?.name ?: carrierSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                carrierFieldPosition = coordinates.positionInRoot()
                                carrierFieldHeight = with(density) { coordinates.size.height.toDp() }
                                carrierFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                carrierSearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedCarrier = null
                                }
                                carrierExpanded = true
                                handleDropdownExpansion("carrier", true)
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
                                .focusRequester(carrierFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !carrierExpanded) {
                                        carrierExpanded = true
                                        handleDropdownExpansion("carrier", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { colorFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isCarrierLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(onClick = {
                                        carrierExpanded = !carrierExpanded
                                        handleDropdownExpansion("carrier", !carrierExpanded)
                                    }) {
                                        Icon(
                                            imageVector = if (carrierExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (carrierExpanded) "Hide" else "Show"
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Carrier Dropdown - Inside Column flow to push content down
                    if (carrierExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                y = dropdownContainerPosition.y - with(density) { carrierFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = carriers,
                                selectedItem = selectedCarrier,
                                searchQuery = carrierSearchQuery,
                                expanded = carrierExpanded,
                                onItemSelected = { item ->
                                    selectedCarrier = item
                                    carrierSearchQuery = item.name.trim()
                                    carrierExpanded = false
                                    handleDropdownExpansion("carrier", false)
                                    colorFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    carrierSearchQuery = newValue
                                    if (selectedCarrier != null && newValue != selectedCarrier?.name) {
                                        selectedCarrier = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    carrierExpanded = expanded
                                    handleDropdownExpansion("carrier", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    specificationViewModel.addCarrier(searchQuery) { createdCarrier ->
                                        val newItem = DropdownItem(createdCarrier.id, createdCarrier.carrierName)
                                        selectItem(newItem)
                                        selectedCarrier = newItem
                                        carrierSearchQuery = createdCarrier.carrierName.trim()
                                    }
                                    carrierExpanded = false
                                    handleDropdownExpansion("carrier", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = carrierFieldHeight,
                                fieldWidth = carrierFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "carrier") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Color Field
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Color",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedColor
                    val displayValue = (selected?.name ?: colorSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                colorFieldPosition = coordinates.positionInRoot()
                                colorFieldHeight = with(density) { coordinates.size.height.toDp() }
                                colorFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                colorSearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedColor = null
                                }
                                colorExpanded = true
                                handleDropdownExpansion("color", true)
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
                                .focusRequester(colorFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !colorExpanded) {
                                        colorExpanded = true
                                        handleDropdownExpansion("color", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { statusFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isColorLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(onClick = {
                                        colorExpanded = !colorExpanded
                                        handleDropdownExpansion("color", !colorExpanded)
                                    }) {
                                        Icon(
                                            imageVector = if (colorExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (colorExpanded) "Hide" else "Show"
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Color Dropdown - Inside Column flow to push content down
                    if (colorExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                y = dropdownContainerPosition.y - with(density) { colorFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = colors,
                                selectedItem = selectedColor,
                                searchQuery = colorSearchQuery,
                                expanded = colorExpanded,
                                onItemSelected = { item ->
                                    selectedColor = item
                                    colorSearchQuery = item.name.trim()
                                    colorExpanded = false
                                    handleDropdownExpansion("color", false)
                                    statusFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    colorSearchQuery = newValue
                                    if (selectedColor != null && newValue != selectedColor?.name) {
                                        selectedColor = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    colorExpanded = expanded
                                    handleDropdownExpansion("color", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    specificationViewModel.addColor(searchQuery) { createdColor ->
                                        val newItem = DropdownItem(createdColor.id, createdColor.colorName)
                                        selectItem(newItem)
                                        selectedColor = newItem
                                        colorSearchQuery = createdColor.colorName.trim()
                                    }
                                    colorExpanded = false
                                    handleDropdownExpansion("color", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = colorFieldHeight,
                                fieldWidth = colorFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "color") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedStatus
                    val displayValue = (selected?.name ?: statusSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                statusFieldPosition = coordinates.positionInRoot()
                                statusFieldHeight = with(density) { coordinates.size.height.toDp() }
                                statusFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                statusSearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedStatus = null
                                }
                                statusExpanded = true
                                handleDropdownExpansion("status", true)
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
                                .focusRequester(statusFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !statusExpanded) {
                                        statusExpanded = true
                                        handleDropdownExpansion("status", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { storageLocationFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    statusExpanded = !statusExpanded
                                    handleDropdownExpansion("status", !statusExpanded)
                                }) {
                                    Icon(
                                        imageVector = if (statusExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = if (statusExpanded) "Hide" else "Show"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Status Dropdown - Inside Column flow to push content down
                    if (statusExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                y = dropdownContainerPosition.y - with(density) { statusFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = statusOptions,
                                selectedItem = selectedStatus,
                                searchQuery = statusSearchQuery,
                                expanded = statusExpanded,
                                onItemSelected = { item ->
                                    selectedStatus = item
                                    statusSearchQuery = item.name.trim()
                                    statusExpanded = false
                                    handleDropdownExpansion("status", false)
                                    storageLocationFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    statusSearchQuery = newValue
                                    if (selectedStatus != null && newValue != selectedStatus?.name) {
                                        selectedStatus = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    statusExpanded = expanded
                                    handleDropdownExpansion("status", expanded)
                                },
                                onAddNew = { _, _ -> }, // No add new functionality for status
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = statusFieldHeight,
                                fieldWidth = statusFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val isDark = isSystemInDarkTheme()
                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { newValue ->
                            // Filter to allow only digits, decimal point
                            var filtered = newValue.filter { it.isDigit() || it == '.' }
                            // Ensure only one decimal point
                            val decimalCount = filtered.count { it == '.' }
                            if (decimalCount > 1) {
                                val firstDecimalIndex = filtered.indexOf('.')
                                filtered = filtered.take(firstDecimalIndex + 1) +
                                        filtered.substring(firstDecimalIndex + 1).replace(".", "")
                            }
                            unitCostText = filtered
                        },
                        placeholder = {
                            Text(
                                "Enter unit cost",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .focusRequester(unitCostFocusRequester),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { imeiFocusRequester.requestFocus() }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = if (isDark) Color.White else Color.Black
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Storage Location Field
                Column(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Storage Location",
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
                    Spacer(modifier = Modifier.height(8.dp))
                    val selected = selectedStorageLocation
                    val displayValue = (selected?.name ?: storageLocationSearchQuery).trim()
                    val isDark = isSystemInDarkTheme()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                storageLocationFieldPosition = coordinates.positionInRoot()
                                storageLocationFieldHeight = with(density) { coordinates.size.height.toDp() }
                                storageLocationFieldWidth = with(density) { coordinates.size.width.toDp() }
                            }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue: String ->
                                storageLocationSearchQuery = newValue
                                if (selected != null && newValue != selected.name) {
                                    selectedStorageLocation = null
                                }
                                storageLocationExpanded = true
                                handleDropdownExpansion("storageLocation", true)
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
                                .focusRequester(storageLocationFocusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused && !storageLocationExpanded) {
                                        storageLocationExpanded = true
                                        handleDropdownExpansion("storageLocation", true)
                                    }
                                },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { imeiFocusRequester.requestFocus() }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isStorageLocationLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    IconButton(onClick = {
                                        storageLocationExpanded = !storageLocationExpanded
                                        handleDropdownExpansion("storageLocation", !storageLocationExpanded)
                                    }) {
                                        Icon(
                                            imageVector = if (storageLocationExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = if (storageLocationExpanded) "Hide" else "Show"
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                cursorColor = if (isDark) Color.White else Color.Black
                            )
                        )
                    }

                    // Storage Location Dropdown - Inside Column flow to push content down
                    if (storageLocationExpanded) {
                        Spacer(modifier = Modifier.height(4.dp))
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
                                y = dropdownContainerPosition.y - with(density) { storageLocationFieldHeight.toPx() } - with(density) { 4.dp.toPx() }
                            )
                            SearchableDropdown(
                                items = storageLocations,
                                selectedItem = selectedStorageLocation,
                                searchQuery = storageLocationSearchQuery,
                                expanded = storageLocationExpanded,
                                onItemSelected = { item ->
                                    selectedStorageLocation = item
                                    storageLocationSearchQuery = item.name.trim()
                                    storageLocationExpanded = false
                                    handleDropdownExpansion("storageLocation", false)
                                    imeiFocusRequester.requestFocus()
                                },
                                onSearchQueryChange = { newValue ->
                                    storageLocationSearchQuery = newValue
                                    if (selectedStorageLocation != null && newValue != selectedStorageLocation?.name) {
                                        selectedStorageLocation = null
                                    }
                                },
                                onExpandedChange = { expanded ->
                                    storageLocationExpanded = expanded
                                    handleDropdownExpansion("storageLocation", expanded)
                                },
                                onAddNew = { searchQuery, selectItem ->
                                    specificationViewModel.addStorageLocation(searchQuery) { createdStorageLocation ->
                                        val newItem = DropdownItem(createdStorageLocation.id, createdStorageLocation.storageLocation)
                                        selectItem(newItem)
                                        selectedStorageLocation = newItem
                                        storageLocationSearchQuery = createdStorageLocation.storageLocation.trim()
                                    }
                                    storageLocationExpanded = false
                                    handleDropdownExpansion("storageLocation", false)
                                },
                                fieldPosition = adjustedFieldPosition,
                                fieldHeight = storageLocationFieldHeight,
                                fieldWidth = storageLocationFieldWidth,
                                rootPosition = dropdownContainerPosition,
                                density = density,
                                isDarkTheme = isDarkTheme,
                                placeholder = "Choose an option",
                                getItemDisplayName = { it.name },
                                getItemId = { it.id },
                                showRolePill = false,
                                onEditItem = { item -> showEditItemDialog(item.id, item.name, "storage location") }
                            )
                        }
                    }
                }
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


                Spacer(modifier = Modifier.height(24.dp))
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
    onEditItem: ((DropdownItem) -> Unit)? = null,
    focusRequester: FocusRequester? = null
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

        // Capacity field with dropdown + unit selector
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
                focusRequester = focusRequester,
                isLoading = isLoading
            )

            // 👉 NEW unit selector replacing old pills
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 48.dp)
            ) {
                CapacityUnitSelector(
                    selectedUnit = selectedUnit,
                    isDarkTheme = isDarkTheme,
                    onUnitSelected = onUnitSelected
                )
            }
        }
    }
}

@Composable
fun CapacityUnitSelector(
    selectedUnit: String,
    isDarkTheme: Boolean,
    onUnitSelected: (String) -> Unit
) {
    val containerColor =
        if (isDarkTheme)
            MaterialTheme.colorScheme.surface
        else
            Color(0xFFF4F4F4)

    val borderColor =
        if (isDarkTheme)
            MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    val accentOrange = Color(0xFFFF8A00)

    val animatedOffset by animateDpAsState(
        targetValue = if (selectedUnit == "GB") 0.dp else 52.dp,
        animationSpec = tween(260),
        label = "unitOffset"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = Modifier
            .height(38.dp)
            .width(104.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Box {
            // 🔶 Sliding orange indicator
            Box(
                modifier = Modifier
                    .offset(x = animatedOffset)
                    .padding(4.dp)
                    .width(44.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentOrange)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("GB", "TB").forEach { unit ->

                    val selected = selectedUnit == unit

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onUnitSelected(unit)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unit,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected)
                                Color.White
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

