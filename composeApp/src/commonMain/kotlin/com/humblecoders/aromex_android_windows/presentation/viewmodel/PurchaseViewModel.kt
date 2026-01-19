package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.EntityRepository
import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for Purchase screen.
 * Uses the shared EntityRepository singleton to access entities.
 * No need to start/stop listeners - the repository manages that.
 */
class PurchaseViewModel(
    private val entityRepository: EntityRepository,
    val specificationViewModel: SpecificationViewModel
) {
    /**
     * Expose the shared entities StateFlow directly from the repository.
     * When any screen loads entities, all screens see the update immediately.
     */
    val entities: StateFlow<List<Entity>> = entityRepository.entities
    
    /**
     * Filter entities by type (e.g., SUPPLIER for purchase screen)
     */
    fun getEntitiesByType(type: com.humblecoders.aromex_android_windows.domain.model.EntityType): List<Entity> {
        return entities.value.filter { it.type == type }
    }
}

