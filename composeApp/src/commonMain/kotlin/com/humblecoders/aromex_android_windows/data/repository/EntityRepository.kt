package com.humblecoders.aromex_android_windows.data.repository

import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton repository interface for entities.
 * Provides a single source of truth for all entities across the app.
 */
interface EntityRepository {
    /**
     * StateFlow containing all entities.
     * This is shared across all ViewModels and screens.
     */
    val entities: StateFlow<List<Entity>>
    
    /**
     * Starts listening to entities if not already started.
     * Safe to call multiple times - only one listener will be active.
     */
    fun startListening()
    
    /**
     * Stops listening to entities.
     * Should be called when the app is closed or repository is no longer needed.
     */
    fun stopListening()
    
    /**
     * Deletes an entity by its ID.
     * The listener will automatically update the StateFlow when the deletion is complete.
     */
    suspend fun deleteEntity(entityId: String): Result<Unit>
}



