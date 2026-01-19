package com.humblecoders.aromex_android_windows.data.repository

import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QuerySnapshot
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestorePurchaseRepository(
    private val firestore: Firestore
) : PurchaseRepository {
    
    override fun getAllEntities(): Flow<List<Entity>> = callbackFlow {
        val listener = firestore.collection("Entities")
            .addSnapshotListener(EventListener<QuerySnapshot> { snapshot, error ->
                if (error != null) {
                    println("Error listening to Entities collection: ${error.message}")
                    trySend(emptyList())
                    return@EventListener
                }
                
                val entities = mutableListOf<Entity>()
                if (snapshot != null) {
                    snapshot.documents.forEach { document ->
                        try {
                            val data = document.data
                            val typeString = data?.get("type") as? String ?: ""
                            val type = try {
                                EntityType.valueOf(typeString)
                            } catch (e: IllegalArgumentException) {
                                EntityType.CUSTOMER // Default fallback
                            }
                            
                            val balance = (data?.get("balance") as? Number)?.toDouble() ?: 0.0
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
                            entities.add(entity)
                        } catch (e: Exception) {
                            println("Error parsing entity document ${document.id}: ${e.message}")
                        }
                    }
                }
                
                trySend(entities)
            })
        
        awaitClose {
            listener.remove()
        }
    }
}

