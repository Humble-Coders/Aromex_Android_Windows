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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType

@Composable
fun SearchableDropdown(
    items: List<Entity>,
    selectedItem: Entity?,
    searchQuery: String,
    expanded: Boolean,
    onItemSelected: (Entity) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onAddNew: (String) -> Unit,
    fieldPosition: Offset,
    fieldHeight: Dp,
    fieldWidth: Dp,
    rootPosition: Offset,
    density: Density,
    isDarkTheme: Boolean = false,
    typeFilter: EntityType? = null,
    placeholder: String = "Search...",
    getItemDisplayName: (Entity) -> String = { it.name },
    getItemId: (Entity) -> String = { it.id }
) {
    // Sort items: filtered type first, then others alphabetically
    val sortedItems = remember(items, typeFilter) {
        val filtered = if (typeFilter != null) {
            items.filter { it.type == typeFilter }.sortedBy { getItemDisplayName(it) } +
            items.filter { it.type != typeFilter }.sortedBy { getItemDisplayName(it) }
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
        val selected = selectedItem
        val query = searchQuery.lowercase()
        val filtered = if (searchQuery.isBlank()) {
            sortedItems
        } else {
            // If search query exactly matches selected item name and dropdown is open, show all
            if (expanded && selected != null && 
                searchQuery.equals(getItemDisplayName(selected), ignoreCase = true)) {
                sortedItems
            } else {
                sortedItems.filter { 
                    getItemDisplayName(it).lowercase().contains(query)
                }
            }
        }
        
        // Put selected item at the top if it exists in the filtered list
        if (selected != null) {
            val selectedInList = filtered.find { getItemId(it) == getItemId(selected) }
            if (selectedInList != null) {
                val withoutSelected = filtered.filter { getItemId(it) != getItemId(selected) }
                listOf(selected) + withoutSelected
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
                                    onAddNew(searchQuery)
                                    onExpandedChange(false)
                                }
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
                                            onAddNew(searchQuery)
                                            onExpandedChange(false)
                                        }
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
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp, end = 48.dp, top = 12.dp, bottom = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Role pill
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (item.type) {
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
                                                text = when (item.type) {
                                                    EntityType.CUSTOMER -> "Customer"
                                                    EntityType.SUPPLIER -> "Supplier"
                                                    EntityType.MIDDLEMAN -> "Middleman"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = when (item.type) {
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
                                IconButton(
                                    onClick = { /* TODO: Handle edit action */ },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
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

