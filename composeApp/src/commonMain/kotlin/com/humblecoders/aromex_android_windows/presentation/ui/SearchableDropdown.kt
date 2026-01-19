package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors

/**
 * A complete, self-contained searchable dropdown field component.
 * This includes the text field, overlay for selected items, and the dropdown menu.
 * 
 * @param items List of items to display in the dropdown
 * @param selectedItem Currently selected item (can be null)
 * @param onItemSelected Callback when an item is selected
 * @param onAddNew Callback when user wants to add a new item. The callback receives the search query
 *                  and a function to call with the newly created item to automatically select it.
 * @param modifier Modifier for the field container
 * @param rootPosition Position of the root container (for dropdown positioning)
 * @param isDarkTheme Whether dark theme is active
 * @param typeFilter Optional filter to show specific entity types first
 * @param placeholder Placeholder text for the field
 * @param label Optional label text above the field
 * @param helperText Optional helper text below the field
 * @param required Whether the field is required (shows asterisk)
 * @param getItemDisplayName Function to get display name from item
 * @param getItemId Function to get unique ID from item
 * @param getItemType Optional function to get entity type from item
 * @param showRolePill Whether to show role pill for entities (only works if getItemType is provided)
 * @param fieldHeight Height of the text field (default: 60.dp)
 */
@Composable
fun <T> SearchableDropdownField(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    onAddNew: (String, (T) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    rootPosition: Offset = Offset.Zero,
    isDarkTheme: Boolean = false,
    typeFilter: EntityType? = null,
    placeholder: String = "Search...",
    label: String? = null,
    helperText: String? = null,
    required: Boolean = false,
    getItemDisplayName: (T) -> String,
    getItemId: (T) -> String,
    getItemType: ((T) -> EntityType?)? = null,
    showRolePill: Boolean = true,
    fieldHeight: Dp = 60.dp,
    onSearchQueryChange: ((String) -> Unit)? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    externalExpanded: Boolean? = null,
    isLoading: Boolean = false,
    onEditItem: ((T) -> Unit)? = null
) {
    // Internal state management
    var searchQuery by remember { mutableStateOf("") }
    var internalExpanded by remember { mutableStateOf(false) }
    
    // Use external expanded state if provided, otherwise use internal
    val expanded = externalExpanded ?: internalExpanded
    
    // Track previous selected item to detect name changes
    var previousSelectedItemId by remember { mutableStateOf<String?>(null) }
    var previousSelectedItemName by remember { mutableStateOf<String?>(null) }
    
    // Sync searchQuery with selectedItem when it changes externally
    LaunchedEffect(selectedItem) {
        if (selectedItem != null) {
            val selectedId = getItemId(selectedItem)
            val selectedName = getItemDisplayName(selectedItem)
            
            // Check if the selected item's name changed (same ID, different name)
            val isNameChanged = previousSelectedItemId == selectedId && 
                               previousSelectedItemName != null && 
                               previousSelectedItemName != selectedName
            
            // Update searchQuery if:
            // 1. searchQuery is empty
            // 2. searchQuery matches the selected item name
            // 3. The item name was changed (edited) - force update to show new name
            if (searchQuery.isEmpty() || searchQuery == selectedName || isNameChanged) {
                searchQuery = selectedName
            }
            
            // Update tracking variables
            previousSelectedItemId = selectedId
            previousSelectedItemName = selectedName
        } else {
            // Reset tracking when nothing is selected
            previousSelectedItemId = null
            previousSelectedItemName = null
        }
    }
    
    // Position tracking
    val density = LocalDensity.current
    var fieldPosition by remember { mutableStateOf(Offset.Zero) }
    var fieldWidth by remember { mutableStateOf(0.dp) }
    var actualFieldHeight by remember { mutableStateOf(fieldHeight) }
    
    // Display value logic
    // Show searchQuery if user is typing (it differs from selected item), otherwise show selected item
    val selected = selectedItem // Store in local variable for smart cast
    val selectedName = if (selected != null) getItemDisplayName(selected) else ""
    val displayValue = if (selected != null && searchQuery == selectedName) {
        selectedName
    } else {
        searchQuery
    }
    
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
        }
        
        // Text Field with Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .onGloballyPositioned { coordinates ->
                    fieldPosition = coordinates.positionInRoot()
                    actualFieldHeight = with(density) { coordinates.size.height.toDp() }
                    fieldWidth = with(density) { coordinates.size.width.toDp() }
                }
        ) {
            OutlinedTextField(
                value = displayValue,
                onValueChange = { newValue: String ->
                    if (enabled) {
                        searchQuery = newValue
                        // Notify parent of search query change (parent can clear selection if needed)
                        onSearchQueryChange?.invoke(newValue)
                        if (externalExpanded == null) {
                            internalExpanded = true
                        }
                        onExpandedChange?.invoke(true)
                    }
                },
                enabled = enabled,
                placeholder = {
                    if (selected == null) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { focusState ->
                        // Open dropdown when field gains focus (clicked)
                        if (enabled && focusState.isFocused && !expanded) {
                            if (externalExpanded == null) {
                                internalExpanded = true
                            }
                            onExpandedChange?.invoke(true)
                        }
                    },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                shape = RoundedCornerShape(10.dp),
                // Use leadingIcon as spacer when selected and showRolePill is true
                leadingIcon = if (selected != null && showRolePill && getItemType != null) {
                    {
                        Box(modifier = Modifier.width(105.dp))
                    }
                } else null,
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(
                            onClick = { 
                                if (enabled) {
                                    val newExpanded = !expanded
                                    if (externalExpanded == null) {
                                        internalExpanded = newExpanded
                                    }
                                    onExpandedChange?.invoke(newExpanded)
                                }
                            },
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
            
            // Overlay content matching dropdown layout exactly - only show when item is selected AND we need role pill
            if (selected != null && showRolePill && getItemType != null) {
                val itemType = getItemType(selected)
                if (itemType != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .padding(start = 16.dp, end = 48.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Role pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (itemType) {
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
                                    text = when (itemType) {
                                        EntityType.CUSTOMER -> "Customer"
                                        EntityType.SUPPLIER -> "Supplier"
                                        EntityType.MIDDLEMAN -> "Middleman"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when (itemType) {
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
                        // Item name text
                        Text(
                            text = getItemDisplayName(selected),
                            fontSize = 14.sp,
                            color = if (isDarkTheme) Color.White else Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
        
        // Helper text
        if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                fontSize = 12.sp,
                color = if (isDarkTheme) Color(0xFFB0B0B0) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    
    // Dropdown component (positioned outside the Column)
    SearchableDropdown(
        items = items,
        selectedItem = selectedItem,
        searchQuery = searchQuery,
        expanded = expanded,
        onItemSelected = { item ->
            onItemSelected(item)
            searchQuery = getItemDisplayName(item)
            if (externalExpanded == null) {
                internalExpanded = false
            }
            onExpandedChange?.invoke(false)
        },
        onSearchQueryChange = { newValue ->
            searchQuery = newValue
            onSearchQueryChange?.invoke(newValue)
        },
        onExpandedChange = { isExpanded ->
            if (externalExpanded == null) {
                internalExpanded = isExpanded
            }
            onExpandedChange?.invoke(isExpanded)
        },
        onAddNew = { query, selectItem ->
            onAddNew(query) { newItem ->
                selectItem(newItem)
                onItemSelected(newItem)
            }
            if (externalExpanded == null) {
                internalExpanded = false
            }
            onExpandedChange?.invoke(false)
        },
        fieldPosition = fieldPosition,
        fieldHeight = actualFieldHeight,
        fieldWidth = fieldWidth,
        rootPosition = rootPosition,
        density = density,
        isDarkTheme = isDarkTheme,
        typeFilter = typeFilter,
        placeholder = placeholder,
        getItemDisplayName = getItemDisplayName,
        getItemId = getItemId,
        getItemType = getItemType,
        showRolePill = showRolePill,
        onEditItem = onEditItem
    )
}

/**
 * Internal dropdown component (used by SearchableDropdownField).
 * This is the actual dropdown menu that appears below the field.
 */
@Composable
fun <T> SearchableDropdown(
    items: List<T>,
    selectedItem: T?,
    searchQuery: String,
    expanded: Boolean,
    onItemSelected: (T) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddNew: (String, (T) -> Unit) -> Unit,
    fieldPosition: Offset,
    fieldHeight: Dp,
    fieldWidth: Dp,
    rootPosition: Offset,
    density: Density,
    isDarkTheme: Boolean = false,
    typeFilter: EntityType? = null,
    placeholder: String = "Search...",
    getItemDisplayName: (T) -> String,
    getItemId: (T) -> String,
    getItemType: ((T) -> EntityType?)? = null,
    showRolePill: Boolean = true,
    onEditItem: ((T) -> Unit)? = null
) {
    // Sort items: filtered type first, then others alphabetically
    val sortedItems = remember(items, typeFilter, getItemType) {
        val filtered = if (typeFilter != null && getItemType != null) {
            items.filter { getItemType(it) == typeFilter }.sortedBy { getItemDisplayName(it) } +
            items.filter { getItemType(it) != typeFilter }.sortedBy { getItemDisplayName(it) }
        } else {
            items.sortedBy { getItemDisplayName(it) }
        }
        filtered
    }
    
    // Check if search query exactly matches any item name
    val hasExactMatch = remember(sortedItems, searchQuery) {
        if (searchQuery.isBlank()) {
            false
        } else {
            sortedItems.any { 
                getItemDisplayName(it).equals(searchQuery, ignoreCase = true)
            }
        }
    }
    
    // Filter items based on search query (case-insensitive)
    val filteredItems = remember(sortedItems, searchQuery, selectedItem, expanded) {
        val query = searchQuery.lowercase()
        val filtered = if (searchQuery.isBlank()) {
            sortedItems
        } else {
            // If search query exactly matches selected item name and dropdown is open, show all
            if (expanded && selectedItem != null &&
                searchQuery.equals(getItemDisplayName(selectedItem), ignoreCase = true)) {
                sortedItems
            } else {
                sortedItems.filter { 
                    getItemDisplayName(it).lowercase().contains(query)
                }
            }
        }
        
        // Put selected item at the top if it exists in the filtered list
        if (selectedItem != null) {
            val selectedInList = filtered.find { getItemId(it) == getItemId(selectedItem) }
            if (selectedInList != null) {
                val withoutSelected = filtered.filter { getItemId(it) != getItemId(selectedItem) }
                listOf(selectedItem) + withoutSelected
            } else {
                filtered
            }
        } else {
            filtered
        }
    }
    
    // Dropdown - Outside the Column so it can overflow and overlap
    AnimatedVisibility(
        visible = expanded,
        enter = fadeIn(tween(200)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(300)
        ) + slideInVertically(
            initialOffsetY = { -it / 4 },
            animationSpec = tween(300)
        ),
        exit = fadeOut(tween(200)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(300)
        ) + slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(300)
        )
    ) {
        // Dropdown positioned below the field
        Box(
            modifier = Modifier
                .offset(
                    x = with(density) { (fieldPosition.x - rootPosition.x).toDp() },
                    y = with(density) { (fieldPosition.y - rootPosition.y).toDp() } + fieldHeight + 4.dp
                )
                .width(fieldWidth)
                .zIndex(1000f)
        ) {
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
                if (filteredItems.isEmpty()) {
                    if (searchQuery.isNotBlank() && !hasExactMatch) {
                        // Show "Add New" button when search query doesn't exactly match any item
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onAddNew(searchQuery) { newItem ->
                                        onItemSelected(newItem)
                                        onSearchQueryChange(getItemDisplayName(newItem))
                                    }
                                    onExpandedChange(false)
                                }
                                .pointerHoverIcon(PointerIcon.Hand)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Green circular icon with plus sign
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
                                    text = "Add '$searchQuery'",
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
                        // Show "Add New" button at the top if no exact match
                        if (!hasExactMatch && searchQuery.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onAddNew(searchQuery) { newItem ->
                                                onItemSelected(newItem)
                                                onSearchQueryChange(getItemDisplayName(newItem))
                                            }
                                            onExpandedChange(false)
                                        }
                                        .pointerHoverIcon(PointerIcon.Hand)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Green circular icon with plus sign
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
                                            text = "Add '$searchQuery'",
                                            fontSize = 14.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        items(filteredItems) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onItemSelected(item)
                                        onSearchQueryChange(getItemDisplayName(item))
                                        onExpandedChange(false)
                                    }
                                    .pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 48.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Role pill - only show if showRolePill is true and getItemType is provided
                                    if (showRolePill && getItemType != null) {
                                        val itemType = getItemType(item)
                                        if (itemType != null) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = when (itemType) {
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
                                                        text = when (itemType) {
                                                            EntityType.CUSTOMER -> "Customer"
                                                            EntityType.SUPPLIER -> "Supplier"
                                                            EntityType.MIDDLEMAN -> "Middleman"
                                                        },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = when (itemType) {
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
                                        }
                                    }
                                    // Item name
                                    Text(
                                        text = getItemDisplayName(item),
                                        fontSize = 14.sp,
                                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    // Tick icon for selected item with circle
                                    if (selectedItem != null && getItemId(selectedItem) == getItemId(item)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp),
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
                                // Edit icon for each item (extreme right edge)
                                if (onEditItem != null) {
                                    IconButton(
                                        onClick = {
                                            onEditItem(item)
                                            onExpandedChange(false)
                                        },
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 8.dp)
                                            .size(32.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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