package com.humblecoders.aromex_android_windows.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Singleton repository for entities (Android implementation).
 * Maintains a single listener and shared StateFlow for all ViewModels.
 */
object FirestoreEntityRepository : EntityRepository {
    private var firestore: FirebaseFirestore? = null
    private var listener: ListenerRegistration? = null
    
    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    override val entities: StateFlow<List<Entity>> = _entities.asStateFlow()
    
    /**
     * Initialize the repository with Firestore instance.
     * Should be called once at app startup.
     */
    fun initialize(firestoreInstance: FirebaseFirestore) {
        if (firestore == null) {
            firestore = firestoreInstance
        }
    }
    
    override fun startListening() {
        val fs = firestore ?: run {
            println("[FirestoreEntityRepository] ❌ ERROR: Firestore not initialized. Call initialize() first.")
            return
        }
        
        // If listener already exists, don't create a new one
        if (listener != null) {
            return
        }
        
        listener = fs.collection("Entities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("[FirestoreEntityRepository] ❌ ERROR listening to Entities collection: ${error.message}")
                    _entities.value = emptyList()
                    return@addSnapshotListener
                }
                
                println("[FirestoreEntityRepository] 📥 Listener called - Received snapshot from Firestore")
                val entityList = mutableListOf<Entity>()
                if (snapshot != null && !snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        try {
                            val data = document.data
                            val typeString = data?.get("type") as? String ?: ""
                            val type = try {
                                EntityType.valueOf(typeString)
                            } catch (e: IllegalArgumentException) {
                                EntityType.CUSTOMER // Default fallback
                            }
                            
                            val balance = document.getDouble("balance") ?: 0.0
                            val balanceType = if (balance < 0) BalanceType.TO_GIVE else BalanceType.TO_RECEIVE
                            
                            val entity = Entity(
                                id = document.id,
                                type = type,
                                name = data?.get("name") as? String ?: "",
                                phone = data?.get("phone") as? String ?: "",
                                email = data?.get("email") as? String ?: "",
                                address = data?.get("address") as? String ?: "",
                                notes = data?.get("notes") as? String ?: "",
                                balance = balance,
                                balanceType = balanceType
                            )
                            entityList.add(entity)
                        } catch (e: Exception) {
                            println("[FirestoreEntityRepository] ❌ Error parsing entity document ${document.id}: ${e.message}")
                        }
                    }
                }
                
                // Sort entities alphabetically by name
                val sortedEntities = entityList.sortedBy { it.name.lowercase() }
                val customerCount = sortedEntities.count { it.type == EntityType.CUSTOMER }
                val supplierCount = sortedEntities.count { it.type == EntityType.SUPPLIER }
                val middlemanCount = sortedEntities.count { it.type == EntityType.MIDDLEMAN }
                
                println("[FirestoreEntityRepository] ✅ Data fetched:")
                println("   - Total entities: ${sortedEntities.size}")
                println("   - Customers: $customerCount")
                println("   - Suppliers: $supplierCount")
                println("   - Middlemen: $middlemanCount")
                
                _entities.value = sortedEntities
            }
    }
    
    override fun stopListening() {
        listener?.remove()
        listener = null
    }
    
    override suspend fun deleteEntity(entityId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            if (entityId.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Entity ID cannot be blank"))
            }
            
            fs.collection("Entities").document(entityId).delete().await()
            
            println("[FirestoreEntityRepository] ✅ Entity deleted: $entityId")
            // The listener will automatically update the StateFlow when Firestore changes
            Result.success(Unit)
        } catch (e: Exception) {
            println("[FirestoreEntityRepository] ❌ Error deleting entity $entityId: ${e.message}")
            Result.failure(e)
        }
    }
}

