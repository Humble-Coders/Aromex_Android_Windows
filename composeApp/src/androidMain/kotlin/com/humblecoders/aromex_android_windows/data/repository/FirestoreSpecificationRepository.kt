package com.humblecoders.aromex_android_windows.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.humblecoders.aromex_android_windows.domain.model.Brand
import com.humblecoders.aromex_android_windows.domain.model.Capacity
import com.humblecoders.aromex_android_windows.domain.model.Carrier
import com.humblecoders.aromex_android_windows.domain.model.Color
import com.humblecoders.aromex_android_windows.domain.model.Model
import com.humblecoders.aromex_android_windows.domain.model.Product
import com.humblecoders.aromex_android_windows.domain.model.StorageLocation
import com.humblecoders.aromex_android_windows.domain.model.UnitCost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Singleton repository for all specification collections (Android implementation).
 * Uses normal fetch operations instead of listeners.
 */
object FirestoreSpecificationRepository : SpecificationRepository {
    private var firestore: FirebaseFirestore? = null
    
    /**
     * Initialize the repository with Firestore instance.
     * Should be called once at app startup.
     */
    fun initialize(firestoreInstance: FirebaseFirestore) {
        if (firestore == null) {
            firestore = firestoreInstance
        }
    }
    
    // Fetch functions
    override suspend fun fetchBrands(): Result<List<Brand>> = withContext(Dispatchers.IO) {
        fetchCollection("Brands") { doc ->
            Brand(
                id = doc.id,
                brandName = doc.data?.get("brandName") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchCapacities(): Result<List<Capacity>> = withContext(Dispatchers.IO) {
        fetchCollection("Capacities") { doc ->
            Capacity(
                id = doc.id,
                capacity = doc.data?.get("capacity") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchCarriers(): Result<List<Carrier>> = withContext(Dispatchers.IO) {
        fetchCollection("Carriers") { doc ->
            Carrier(
                id = doc.id,
                carrierName = doc.data?.get("carrierName") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchColors(): Result<List<Color>> = withContext(Dispatchers.IO) {
        fetchCollection("Colors") { doc ->
            Color(
                id = doc.id,
                colorName = doc.data?.get("colorName") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchStorageLocations(): Result<List<StorageLocation>> = withContext(Dispatchers.IO) {
        fetchCollection("StorageLocations") { doc ->
            StorageLocation(
                id = doc.id,
                storageLocation = doc.data?.get("storageLocation") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchModels(): Result<List<Model>> = withContext(Dispatchers.IO) {
        fetchCollection("Models") { doc ->
            Model(
                id = doc.id,
                brandName = doc.data?.get("brandName") as? String ?: "",
                modelName = doc.data?.get("modelName") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchUnitCosts(): Result<List<UnitCost>> = withContext(Dispatchers.IO) {
        fetchCollection("UnitCosts") { doc ->
            UnitCost(
                id = doc.id,
                unitCost = doc.data?.get("unitCost") as? String ?: ""
            )
        }
    }
    
    override suspend fun fetchProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        fetchCollection("Products") { doc ->
            val data = doc.data
            Product(
                id = doc.id,
                brandId = data?.get("brandId") as? String ?: "",
                brandName = data?.get("brandName") as? String ?: "",
                capacityId = data?.get("capacityId") as? String ?: "",
                capacity = data?.get("capacity") as? String ?: "",
                capacityUnit = data?.get("capacityUnit") as? String ?: "",
                carrierId = data?.get("carrierId") as? String ?: "",
                carrierName = data?.get("carrierName") as? String ?: "",
                colorId = data?.get("colorId") as? String ?: "",
                colorName = data?.get("colorName") as? String ?: "",
                IMEI = data?.get("IMEI") as? String ?: "",
                modelId = data?.get("modelId") as? String ?: "",
                modelName = data?.get("modelName") as? String ?: "",
                status = data?.get("status") as? String ?: "",
                storageLocationId = data?.get("storageLocationId") as? String ?: "",
                storageLocation = data?.get("storageLocation") as? String ?: "",
                unitCost = (data?.get("unitCost") as? Number)?.toDouble() ?: 0.0,
                updatedAt = when (val timestampValue = data?.get("updatedAt")) {
                    is String -> timestampValue
                    is Number -> formatTimestamp(timestampValue.toLong())
                    else -> formatTimestamp(System.currentTimeMillis())
                }
            )
        }
    }
    
    override suspend fun getModelsByBrand(brandId: String): Result<List<Model>> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            // First fetch brands to get brand name
            val brandsResult = fetchBrands()
            val brandName = brandsResult.getOrNull()?.find { it.id == brandId }?.brandName ?: ""
            
            if (brandName.isEmpty()) {
                return@withContext Result.success(emptyList())
            }
            
            // Fetch models and filter by brand name
            val modelsResult = fetchModels()
            return@withContext modelsResult.map { models ->
                models.filter { it.brandName == brandName }
            }
        } catch (e: Exception) {
            println("[FirestoreSpecificationRepository] ❌ Error getting models by brand: ${e.message}")
            return@withContext Result.failure(e)
        }
    }
    
    // Helper function to fetch a collection
    private suspend fun <T> fetchCollection(
        collectionName: String,
        mapper: (com.google.firebase.firestore.QueryDocumentSnapshot) -> T
    ): Result<List<T>> {
        try {
            val fs = firestore ?: return Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            println("[FirestoreSpecificationRepository] 📥 Fetching $collectionName from Firestore")
            val snapshot = fs.collection(collectionName).get().await()
            
            val items = mutableListOf<T>()
            if (!snapshot.isEmpty) {
                snapshot.documents.forEach { document ->
                    try {
                        val queryDoc = document as? com.google.firebase.firestore.QueryDocumentSnapshot
                        if (queryDoc != null) {
                            items.add(mapper(queryDoc))
                        }
                    } catch (e: Exception) {
                        println("[FirestoreSpecificationRepository] ❌ Error parsing $collectionName document ${document.id}: ${e.message}")
                    }
                }
            }
            
            println("[FirestoreSpecificationRepository] ✅ $collectionName fetched: ${items.size} items")
            return Result.success(items)
        } catch (e: Exception) {
            println("[FirestoreSpecificationRepository] ❌ Error fetching $collectionName: ${e.message}")
            return Result.failure(e)
        }
    }
    
    // Brands CRUD
    override suspend fun addBrand(brand: Brand): Flow<Result<String>> = flow {
        emit(addDocument("Brands", mapOf("brandName" to brand.brandName)))
    }
    
    override suspend fun updateBrand(brandId: String, brandName: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("Brands", brandId, mapOf("brandName" to brandName)))
    }
    
    override suspend fun deleteBrand(brandId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Brands", brandId))
    }
    
    // Capacities CRUD
    override suspend fun addCapacity(capacity: Capacity): Flow<Result<String>> = flow {
        emit(addDocument("Capacities", mapOf("capacity" to capacity.capacity)))
    }
    
    override suspend fun updateCapacity(capacityId: String, capacity: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("Capacities", capacityId, mapOf("capacity" to capacity)))
    }
    
    override suspend fun deleteCapacity(capacityId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Capacities", capacityId))
    }
    
    // Carriers CRUD
    override suspend fun addCarrier(carrier: Carrier): Flow<Result<String>> = flow {
        emit(addDocument("Carriers", mapOf("carrierName" to carrier.carrierName)))
    }
    
    override suspend fun updateCarrier(carrierId: String, carrierName: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("Carriers", carrierId, mapOf("carrierName" to carrierName)))
    }
    
    override suspend fun deleteCarrier(carrierId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Carriers", carrierId))
    }
    
    // Colors CRUD
    override suspend fun addColor(color: Color): Flow<Result<String>> = flow {
        emit(addDocument("Colors", mapOf("colorName" to color.colorName)))
    }
    
    override suspend fun updateColor(colorId: String, colorName: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("Colors", colorId, mapOf("colorName" to colorName)))
    }
    
    override suspend fun deleteColor(colorId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Colors", colorId))
    }
    
    // Storage Locations CRUD
    override suspend fun addStorageLocation(storageLocation: StorageLocation): Flow<Result<String>> = flow {
        emit(addDocument("StorageLocations", mapOf("storageLocation" to storageLocation.storageLocation)))
    }
    
    override suspend fun updateStorageLocation(storageLocationId: String, storageLocation: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("StorageLocations", storageLocationId, mapOf("storageLocation" to storageLocation)))
    }
    
    override suspend fun deleteStorageLocation(storageLocationId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("StorageLocations", storageLocationId))
    }
    
    // Models CRUD
    override suspend fun addModel(model: Model): Flow<Result<String>> = flow {
        emit(addDocument("Models", mapOf(
            "brandName" to model.brandName,
            "modelName" to model.modelName
        )))
    }
    
    override suspend fun updateModel(modelId: String, brandName: String, modelName: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("Models", modelId, mapOf(
            "brandName" to brandName,
            "modelName" to modelName
        )))
    }
    
    override suspend fun deleteModel(modelId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Models", modelId))
    }
    
    // Unit Costs CRUD
    override suspend fun addUnitCost(unitCost: UnitCost): Flow<Result<String>> = flow {
        emit(addDocument("UnitCosts", mapOf("unitCost" to unitCost.unitCost)))
    }
    
    override suspend fun updateUnitCost(unitCostId: String, unitCost: String): Flow<Result<Unit>> = flow {
        emit(updateDocument("UnitCosts", unitCostId, mapOf("unitCost" to unitCost)))
    }
    
    override suspend fun deleteUnitCost(unitCostId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("UnitCosts", unitCostId))
    }
    
    // Products CRUD
    override suspend fun addProduct(product: Product): Flow<Result<String>> = flow {
        val timestamp = if (product.updatedAt.isBlank()) {
            formatTimestamp(System.currentTimeMillis())
        } else {
            product.updatedAt
        }
        emit(addDocument("Products", mapOf(
            "brandId" to product.brandId,
            "brandName" to product.brandName,
            "capacityId" to product.capacityId,
            "capacity" to product.capacity,
            "capacityUnit" to product.capacityUnit,
            "carrierId" to product.carrierId,
            "carrierName" to product.carrierName,
            "colorId" to product.colorId,
            "colorName" to product.colorName,
            "IMEI" to product.IMEI,
            "modelId" to product.modelId,
            "modelName" to product.modelName,
            "status" to product.status,
            "storageLocationId" to product.storageLocationId,
            "storageLocation" to product.storageLocation,
            "unitCost" to product.unitCost,
            "updatedAt" to timestamp
        )))
    }
    
    override suspend fun updateProduct(product: Product): Flow<Result<Unit>> = flow {
        emit(updateDocument("Products", product.id, mapOf(
            "brandId" to product.brandId,
            "brandName" to product.brandName,
            "capacityId" to product.capacityId,
            "capacity" to product.capacity,
            "capacityUnit" to product.capacityUnit,
            "carrierId" to product.carrierId,
            "carrierName" to product.carrierName,
            "colorId" to product.colorId,
            "colorName" to product.colorName,
            "IMEI" to product.IMEI,
            "modelId" to product.modelId,
            "modelName" to product.modelName,
            "status" to product.status,
            "storageLocationId" to product.storageLocationId,
            "storageLocation" to product.storageLocation,
            "unitCost" to product.unitCost,
            "updatedAt" to formatTimestamp(System.currentTimeMillis())
        )))
    }
    
    override suspend fun deleteProduct(productId: String): Flow<Result<Unit>> = flow {
        emit(deleteDocument("Products", productId))
    }
    
    // Helper functions
    private suspend fun addDocument(collectionName: String, data: Map<String, Any>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            val docRef = fs.collection(collectionName).document()
            docRef.set(data).await()
            println("[FirestoreSpecificationRepository] ✅ Added $collectionName document: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            println("[FirestoreSpecificationRepository] ❌ Error adding $collectionName: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun updateDocument(collectionName: String, documentId: String, data: Map<String, Any>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            fs.collection(collectionName).document(documentId).update(data).await()
            println("[FirestoreSpecificationRepository] ✅ Updated $collectionName document: $documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("[FirestoreSpecificationRepository] ❌ Error updating $collectionName document $documentId: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun deleteDocument(collectionName: String, documentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            fs.collection(collectionName).document(documentId).delete().await()
            println("[FirestoreSpecificationRepository] ✅ Deleted $collectionName document: $documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("[FirestoreSpecificationRepository] ❌ Error deleting $collectionName document $documentId: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Formats a timestamp in milliseconds to a formatted string.
     * Format: "January 09, 2026 at 1:05:33 AM UTC+5:30"
     */
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a 'UTC'XXX", java.util.Locale.ENGLISH)
        val formatted = dateFormat.format(java.util.Date(timestamp))
        // Remove leading zero from timezone offset (e.g., +05:30 -> +5:30)
        return formatted.replace("UTC+0", "UTC+").replace("UTC-0", "UTC-")
    }
}
