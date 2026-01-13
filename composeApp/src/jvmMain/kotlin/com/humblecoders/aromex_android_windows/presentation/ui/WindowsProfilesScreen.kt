package com.humblecoders.aromex_android_windows.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import com.humblecoders.aromex_android_windows.presentation.viewmodel.ProfilesViewModel
import com.humblecoders.aromex_android_windows.ui.theme.AromexColors
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WindowsProfilesScreen(
    viewModel: ProfilesViewModel,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Customer") }
    
    // Collect entity counts from ViewModel
    val entities by viewModel.entities.collectAsState()
    val customerCount = viewModel.getEntityCount(EntityType.CUSTOMER)
    val supplierCount = viewModel.getEntityCount(EntityType.SUPPLIER)
    val middlemanCount = viewModel.getEntityCount(EntityType.MIDDLEMAN)
    
    // Start listening when Profiles screen is opened (whichever screen opens first loads the data)
    LaunchedEffect(Unit) {
        com.humblecoders.aromex_android_windows.data.repository.FirestoreEntityRepository.startListening()
    }

    // Determine selected entity type
    val selectedEntityType = when (selectedCategory) {
        "Customer" -> EntityType.CUSTOMER
        "Supplier" -> EntityType.SUPPLIER
        "Middleman" -> EntityType.MIDDLEMAN
        else -> EntityType.CUSTOMER
    }
    
    // Filter and search entities
    val filteredEntities = remember(entities, selectedEntityType, searchQuery) {
        viewModel.searchEntities(searchQuery, selectedEntityType)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
        // Title
        Text(
            text = "Profiles",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Horizontal Divider
        Divider(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            thickness = 1.dp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            placeholder = {
                Text(
                    text = "Search by name, phone, or balance...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite(),
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )

        // Category Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Button
            CategoryButton(
                title = "Customer",
                entityCount = customerCount,
                icon = Icons.Default.People,
                isSelected = selectedCategory == "Customer",
                borderColor = Color(0xFF2196F3), // Blue
                onClick = { selectedCategory = "Customer" },
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )

            // Supplier Button
            CategoryButton(
                title = "Supplier",
                entityCount = supplierCount,
                icon = Icons.Default.Store,
                isSelected = selectedCategory == "Supplier",
                borderColor = Color(0xFF4CAF50), // Green
                onClick = { selectedCategory = "Supplier" },
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )

            // Middleman Button
            CategoryButton(
                title = "Middleman",
                entityCount = middlemanCount,
                icon = Icons.Default.PeopleOutline,
                isSelected = selectedCategory == "Middleman",
                borderColor = Color(0xFFF44336), // Red
                onClick = { selectedCategory = "Middleman" },
                modifier = Modifier.weight(1f),
                isDarkTheme = isDarkTheme
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredEntities,
                key = { it.id }
            ) { entity ->
                ProfileRow(
                    entity = entity,
                    isDarkTheme = isDarkTheme,
                    onDeleteClick = {
                        viewModel.deleteEntity(entity.id)
                    }
                )
            }
        }
        }
    }
}

@Composable
fun CategoryButton(
    title: String,
    entityCount: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (isSelected) {
        borderColor
    } else {
        if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite()
    }

    val textColor = if (isSelected) {
        Color.White
    } else {
        if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
    }

    val iconColor = if (isSelected) {
        Color.White
    } else {
        borderColor
    }

    Card(
        modifier = modifier
            .height(65.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .pointerHoverIcon(PointerIcon.Hand)
            .then(
                if (!isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$entityCount entities",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isSelected) {
                        Color.White.copy(alpha = 0.9f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ProfileRow(
    entity: Entity,
    isDarkTheme: Boolean = false,
    onDeleteClick: () -> Unit
) {
    val balanceFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val formattedBalance = balanceFormat.format(entity.balance)
    
    // Determine balance color
    val balanceColor = when {
        entity.balance > 0 -> Color(0xFF4CAF50) // Green
        entity.balance < 0 -> Color(0xFFF44336) // Red
        else -> if (isDarkTheme) Color.White else Color.Black // Black/White for zero
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else AromexColors.ForegroundWhite()
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Name
            Text(
                text = entity.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Balance
            Text(
                text = formattedBalance,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = balanceColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Phone number or dash
            Text(
                text = if (entity.phone.isNotBlank()) entity.phone else "-",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = if (entity.phone.isNotBlank()) {
                    if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            // Delete icon
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(40.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${entity.name}",
                    tint = Color(0xFFF44336), // Red
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

