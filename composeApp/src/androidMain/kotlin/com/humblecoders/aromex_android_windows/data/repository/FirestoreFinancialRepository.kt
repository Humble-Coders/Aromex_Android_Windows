package com.humblecoders.aromex_android_windows.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FirestoreFinancialRepository(
    private val firestore: FirebaseFirestore
) : FinancialRepository {
    
    private val accountBalanceCollection = "Balances"

    
    override fun getAccountBalance(): Flow<AccountBalance> = callbackFlow {
        var bankBalance = 0.0
        var cash = 0.0
        var creditCard = 0.0
        
        // Single collection listener for all balance documents
        val listener = firestore.collection(accountBalanceCollection)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error but don't stop - Firestore will use cached data in offline mode
                    println("Error listening to Balances collection: ${error.message}")
                    // Still emit current values (might be from cache)
                    trySend(AccountBalance(
                        bankBalance = bankBalance,
                        cash = cash,
                        creditCard = creditCard
                    ))
                    return@addSnapshotListener
                }
                
                if (snapshot != null && !snapshot.isEmpty) {
                    // Process all documents in the snapshot
                    snapshot.documents.forEach { document ->
                        when (document.id) {
                            "bank" -> {
                                bankBalance = document.getDouble("amount") ?: 0.0
                            }
                            "cash" -> {
                                cash = document.getDouble("amount") ?: 0.0
                            }
                            "creditCard" -> {
                                creditCard = document.getDouble("amount") ?: 0.0
                            }
                        }
                    }
                    
                    // Emit the combined balance
                    trySend(AccountBalance(
                        bankBalance = bankBalance,
                        cash = cash,
                        creditCard = creditCard
                    ))
                } else {
                    // No documents yet, emit default values
                    trySend(AccountBalance(
                        bankBalance = bankBalance,
                        cash = cash,
                        creditCard = creditCard
                    ))
                }
            }
        
        awaitClose {
            // Clean up listener when flow is cancelled
            listener.remove()
        }
    }
    
    override fun getDebtOverview(): Flow<com.humblecoders.aromex_android_windows.domain.model.DebtOverview> = callbackFlow {
        var totalOwed = 0.0
        var totalDueToMe = 0.0
        val listener = firestore.collection("Entities")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(com.humblecoders.aromex_android_windows.domain.model.DebtOverview(
                        totalOwed = totalOwed,
                        totalDueToMe = totalDueToMe
                    ))
                    return@addSnapshotListener
                }
                totalOwed = 0.0
                totalDueToMe = 0.0
                if (snapshot != null && !snapshot.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val amount = document.getDouble("initialBalance") ?: 0.0
                        // Derive balanceType from initialBalance sign: negative = TO_GIVE, positive = TO_RECEIVE
                        if (amount < 0) {
                            totalOwed += kotlin.math.abs(amount)
                        } else {
                            totalDueToMe += amount
                        }
                    }
                }
                trySend(com.humblecoders.aromex_android_windows.domain.model.DebtOverview(
                    totalOwed = totalOwed,
                    totalDueToMe = totalDueToMe
                ))
            }
        awaitClose { listener.remove() }
    }
    
    override fun updateAccountBalance(accountBalance: AccountBalance): Flow<Result<Unit>> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Use transaction to ensure all updates succeed or fail together
            firestore.runTransaction { transaction ->
                val bankDocRef = firestore.collection(accountBalanceCollection)
                    .document("bank")
                transaction.set(bankDocRef, mapOf(
                    "amount" to accountBalance.bankBalance,
                    "updatedAt" to currentTime
                ))
                
                val cashDocRef = firestore.collection(accountBalanceCollection)
                    .document("cash")
                transaction.set(cashDocRef, mapOf(
                    "amount" to accountBalance.cash,
                    "updatedAt" to currentTime
                ))
                
                val creditCardDocRef = firestore.collection(accountBalanceCollection)
                    .document("creditCard")
                transaction.set(creditCardDocRef, mapOf(
                    "amount" to accountBalance.creditCard,
                    "updatedAt" to currentTime
                ))
                
                null // Transaction function must return a value
            }.await()
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override fun updateSingleBalance(balanceType: String, amount: Double): Flow<Result<Unit>> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Use transaction for consistency and atomicity
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection(accountBalanceCollection)
                    .document(balanceType)
                transaction.set(docRef, mapOf(
                    "amount" to amount,
                    "updatedAt" to currentTime
                ))
                
                null // Transaction function must return a value
            }.await()
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun addEntity(entity: Entity): Flow<Result<Unit>> = flow {
        try {
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
            
            // Use transaction for atomicity
            firestore.runTransaction { transaction ->
                val docRef = firestore.collection("Entities").document()
                transaction.set(docRef, entityData)
                
                null // Transaction function must return a value
            }.await()
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a 'UTC'XXX", java.util.Locale.ENGLISH)
        val formatted = dateFormat.format(java.util.Date(timestamp))
        // Remove leading zero from timezone offset (e.g., +05:30 -> +5:30)
        return formatted.replace("UTC+0", "UTC+").replace("UTC-0", "UTC-")
    }
}

