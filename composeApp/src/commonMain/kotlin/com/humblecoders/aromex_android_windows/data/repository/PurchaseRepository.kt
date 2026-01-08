package com.humblecoders.aromex_android_windows.data.repository

import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getAllEntities(): Flow<List<Entity>>
}

