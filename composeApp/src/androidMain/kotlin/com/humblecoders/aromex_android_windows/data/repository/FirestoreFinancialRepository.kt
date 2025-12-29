package com.humblecoders.aromex_android_windows.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
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
    
    override fun updateAccountBalance(accountBalance: AccountBalance): Flow<Result<Unit>> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Update each document separately
            firestore.collection(accountBalanceCollection)
                .document("bank")
                .set(mapOf(
                    "amount" to accountBalance.bankBalance,
                    "updatedAt" to currentTime
                ))
                .await()
            
            firestore.collection(accountBalanceCollection)
                .document("cash")
                .set(mapOf(
                    "amount" to accountBalance.cash,
                    "updatedAt" to currentTime
                ))
                .await()
            
            firestore.collection(accountBalanceCollection)
                .document("creditCard")
                .set(mapOf(
                    "amount" to accountBalance.creditCard,
                    "updatedAt" to currentTime
                ))
                .await()
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    override fun updateSingleBalance(balanceType: String, amount: Double): Flow<Result<Unit>> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            
            firestore.collection(accountBalanceCollection)
                .document(balanceType)
                .set(mapOf(
                    "amount" to amount,
                    "updatedAt" to currentTime
                ))
                .await()
            
            emit(Result.success(Unit))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

