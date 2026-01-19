package com.humblecoders.aromex_android_windows.data.repository

import com.humblecoders.aromex_android_windows.domain.model.Brand
import com.humblecoders.aromex_android_windows.domain.model.Capacity
import com.humblecoders.aromex_android_windows.domain.model.Carrier
import com.humblecoders.aromex_android_windows.domain.model.Color
import com.humblecoders.aromex_android_windows.domain.model.Model
import com.humblecoders.aromex_android_windows.domain.model.Product
import com.humblecoders.aromex_android_windows.domain.model.StorageLocation
import com.humblecoders.aromex_android_windows.domain.model.UnitCost
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for all specification-related collections.
 * Handles Brands, Capacities, Carriers, Colors, Models, StorageLocations, UnitCosts, and Products.
 */
interface SpecificationRepository {
    // Brands
    suspend fun fetchBrands(): Result<List<Brand>>
    suspend fun addBrand(brand: Brand): Flow<Result<String>>
    suspend fun updateBrand(brandId: String, brandName: String): Flow<Result<Unit>>
    suspend fun deleteBrand(brandId: String): Flow<Result<Unit>>
    
    // Capacities
    suspend fun fetchCapacities(): Result<List<Capacity>>
    suspend fun addCapacity(capacity: Capacity): Flow<Result<String>>
    suspend fun updateCapacity(capacityId: String, capacity: String): Flow<Result<Unit>>
    suspend fun deleteCapacity(capacityId: String): Flow<Result<Unit>>
    
    // Carriers
    suspend fun fetchCarriers(): Result<List<Carrier>>
    suspend fun addCarrier(carrier: Carrier): Flow<Result<String>>
    suspend fun updateCarrier(carrierId: String, carrierName: String): Flow<Result<Unit>>
    suspend fun deleteCarrier(carrierId: String): Flow<Result<Unit>>
    
    // Colors
    suspend fun fetchColors(): Result<List<Color>>
    suspend fun addColor(color: Color): Flow<Result<String>>
    suspend fun updateColor(colorId: String, colorName: String): Flow<Result<Unit>>
    suspend fun deleteColor(colorId: String): Flow<Result<Unit>>
    
    // Storage Locations
    suspend fun fetchStorageLocations(): Result<List<StorageLocation>>
    suspend fun addStorageLocation(storageLocation: StorageLocation): Flow<Result<String>>
    suspend fun updateStorageLocation(storageLocationId: String, storageLocation: String): Flow<Result<Unit>>
    suspend fun deleteStorageLocation(storageLocationId: String): Flow<Result<Unit>>
    
    // Models
    suspend fun fetchModels(): Result<List<Model>>
    suspend fun addModel(model: Model): Flow<Result<String>>
    suspend fun updateModel(modelId: String, brandName: String, modelName: String): Flow<Result<Unit>>
    suspend fun deleteModel(modelId: String): Flow<Result<Unit>>
    suspend fun getModelsByBrand(brandId: String): Result<List<Model>>
    
    // Unit Costs
    suspend fun fetchUnitCosts(): Result<List<UnitCost>>
    suspend fun addUnitCost(unitCost: UnitCost): Flow<Result<String>>
    suspend fun updateUnitCost(unitCostId: String, unitCost: String): Flow<Result<Unit>>
    suspend fun deleteUnitCost(unitCostId: String): Flow<Result<Unit>>
    
    // Products
    suspend fun fetchProducts(): Result<List<Product>>
    suspend fun addProduct(product: Product): Flow<Result<String>>
    suspend fun updateProduct(product: Product): Flow<Result<Unit>>
    suspend fun deleteProduct(productId: String): Flow<Result<Unit>>
}

