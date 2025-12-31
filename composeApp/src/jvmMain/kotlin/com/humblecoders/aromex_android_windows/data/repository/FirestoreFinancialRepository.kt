package com.humblecoders.aromex_android_windows.data.repository

import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.EventListener
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QuerySnapshot
import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class FirestoreFinancialRepository(
    private val firestore: Firestore
) : FinancialRepository {
    
    private val accountBalanceCollection = "Balances"
    
    override fun getAccountBalance(): Flow<AccountBalance> = callbackFlow {
        var bankBalance = 0.0
        var cash = 0.0
        var creditCard = 0.0
        
        // Single collection listener for all balance documents
        val listener = firestore.collection(accountBalanceCollection)
            .addSnapshotListener(EventListener<QuerySnapshot> { snapshot, error ->
                if (error != null) {
                    println("Error listening to Balances collection: ${error.message}")
                    return@EventListener
                }
                
                if (snapshot != null) {
                    // Process all documents in the snapshot
                    snapshot.documents.forEach { document ->
                        val data = document.data
                        when (document.id) {
                            "bank" -> {
                                bankBalance = (data?.get("amount") as? Number)?.toDouble() ?: 0.0
                            }
                            "cash" -> {
                                cash = (data?.get("amount") as? Number)?.toDouble() ?: 0.0
                            }
                            "creditCard" -> {
                                creditCard = (data?.get("amount") as? Number)?.toDouble() ?: 0.0
                            }
                        }
                    }
                    
                    // Emit the combined balance
                    trySend(AccountBalance(
                        bankBalance = bankBalance,
                        cash = cash,
                        creditCard = creditCard
                    ))
                }
            })
        
        awaitClose {
            // Clean up listener when flow is cancelled
            listener.remove()
        }
    }
    
    override fun getDebtOverview(): Flow<DebtOverview> = callbackFlow {
        var totalOwed = 0.0
        var totalDueToMe = 0.0
        val listener = firestore.collection("Entities")
            .addSnapshotListener(EventListener<QuerySnapshot> { snapshot, error ->
                if (error != null) {
                    trySend(DebtOverview(
                        totalOwed = totalOwed,
                        totalDueToMe = totalDueToMe
                    ))
                    return@EventListener
                }
                
                totalOwed = 0.0
                totalDueToMe = 0.0
                if (snapshot != null) {
                    snapshot.documents.forEach { document ->
                        val data = document.data
                        val amount = (data?.get("initialBalance") as? Number)?.toDouble() ?: 0.0
                        // Derive balanceType from initialBalance sign: negative = TO_GIVE, positive = TO_RECEIVE
                        if (amount < 0) {
                            totalOwed += kotlin.math.abs(amount)
                        } else {
                            totalDueToMe += amount
                        }
                    }
                }
                
                trySend(DebtOverview(
                    totalOwed = totalOwed,
                    totalDueToMe = totalDueToMe
                ))
            })
        
        awaitClose {
            listener.remove()
        }
    }
    
    override fun updateAccountBalance(accountBalance: AccountBalance): Flow<Result<Unit>> = flow {
        val result = withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                
                // Update each document separately
                val bankDocRef: DocumentReference = firestore.collection(accountBalanceCollection)
                    .document("bank")
                val bankData: Map<String, Any> = mapOf(
                    "amount" to accountBalance.bankBalance,
                    "updatedAt" to currentTime
                )
                val bankFuture = bankDocRef.set(bankData)
                bankFuture.get()
                
                val cashDocRef: DocumentReference = firestore.collection(accountBalanceCollection)
                    .document("cash")
                val cashData: Map<String, Any> = mapOf(
                    "amount" to accountBalance.cash,
                    "updatedAt" to currentTime
                )
                val cashFuture = cashDocRef.set(cashData)
                cashFuture.get()
                
                val creditCardDocRef: DocumentReference = firestore.collection(accountBalanceCollection)
                    .document("creditCard")
                val creditCardData: Map<String, Any> = mapOf(
                    "amount" to accountBalance.creditCard,
                    "updatedAt" to currentTime
                )
                val creditCardFuture = creditCardDocRef.set(creditCardData)
                creditCardFuture.get()
                
                Result.success(Unit)
            } catch (e: Exception) {
                println("Error updating account balance: ${e.message}")
                Result.failure(e)
            }
        }
        emit(result)
    }
    
    override fun updateSingleBalance(balanceType: String, amount: Double): Flow<Result<Unit>> = flow {
        val result = withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                
                val docRef: DocumentReference = firestore.collection(accountBalanceCollection)
                    .document(balanceType)
                
                val data: Map<String, Any> = mapOf(
                    "amount" to amount,
                    "updatedAt" to currentTime
                )
                val future = docRef.set(data)
                
                future.get()
                Result.success(Unit)
            } catch (e: Exception) {
                println("Error updating single balance: ${e.message}")
                Result.failure(e)
            }
        }
        emit(result)
    }

    override fun addEntity(entity: Entity): Flow<Result<Unit>> = flow {
        val result = withContext(Dispatchers.IO) {
            try {
                val docRef = firestore.collection("Entities").document()
                val updatedAt = formatTimestamp(System.currentTimeMillis())
                val entityData = mapOf(
                    "type" to entity.type.name,
                    "name" to entity.name,
                    "phone" to entity.phone,
                    "email" to entity.email,
                    "address" to entity.address,
                    "notes" to entity.notes,
                    "initialBalance" to entity.initialBalance,
                    "updatedAt" to updatedAt
                )
                val future = docRef.set(entityData)
                future.get()
                Result.success(Unit)
            } catch (e: Exception) {
                println("Error adding entity: ${e.message}")
                Result.failure(e)
            }
        }
        emit(result)
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a 'UTC'XXX", java.util.Locale.ENGLISH)
        val formatted = dateFormat.format(java.util.Date(timestamp))
        // Remove leading zero from timezone offset (e.g., +05:30 -> +5:30)
        return formatted.replace("UTC+0", "UTC+").replace("UTC-0", "UTC-")
    }
}

// pushing this in windows branch

