package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.EntityRepository
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for Profiles screen.
 * Uses the shared EntityRepository singleton to access entities.
 * No need to start/stop listeners - the repository manages that.
 */
class ProfilesViewModel(
    private val entityRepository: EntityRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    /**
     * Expose the shared entities StateFlow directly from the repository.
     * When any screen loads entities, all screens see the update immediately.
     */
    val entities: StateFlow<List<Entity>> = entityRepository.entities
    
    /**
     * Get entities filtered by type
     */
    fun getEntitiesByType(type: EntityType): StateFlow<List<Entity>> {
        return entities.map { entityList ->
            entityList.filter { it.type == type }
        } as StateFlow<List<Entity>>
    }
    
    /**
     * Get count of entities by type
     */
    fun getEntityCount(type: EntityType): Int {
        return entities.value.count { it.type == type }
    }
    
    /**
     * Search entities by name, phone, or balance
     */
    fun searchEntities(query: String, type: EntityType? = null): List<Entity> {
        val filteredByType = if (type != null) {
            entities.value.filter { it.type == type }
        } else {
            entities.value
        }
        
        if (query.isBlank()) {
            return filteredByType
        }
        
        val lowerQuery = query.lowercase()
        return filteredByType.filter { entity ->
            entity.name.lowercase().contains(lowerQuery) ||
            entity.phone.contains(query) ||
            entity.balance.toString().contains(query)
        }
    }
    
    /**
     * Delete an entity by its ID.
     * The repository listener will automatically update the StateFlow when deletion completes.
     */
    fun deleteEntity(entityId: String) {
        viewModelScope.launch {
            entityRepository.deleteEntity(entityId).fold(
                onSuccess = {
                    // Success - listener will automatically update the StateFlow
                },
                onFailure = { error ->
                    // Handle error if needed
                    println("[ProfilesViewModel] Error deleting entity: ${error.message}")
                }
            )
        }
    }
}

