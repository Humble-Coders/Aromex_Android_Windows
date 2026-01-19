package com.humblecoders.aromex_android_windows.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.humblecoders.aromex_android_windows.domain.model.ExpenseCategory
import com.humblecoders.aromex_android_windows.domain.model.ExpenseTransaction
import com.humblecoders.aromex_android_windows.domain.model.PaymentSplit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Singleton repository for expenses (Android implementation).
 * Categories are fetched only when dropdown is opened (lazy loading).
 */
object FirestoreExpenseRepository : ExpenseRepository {
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
    
    override suspend fun fetchCategories(): Result<List<ExpenseCategory>> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            println("[FirestoreExpenseRepository] 📥 Fetching ExpenseCategories from Firestore")
            val snapshot = fs.collection("ExpenseCategories").get().await()
            
            val categoryList = mutableListOf<ExpenseCategory>()
            snapshot.documents.forEach { document ->
                try {
                    val data = document.data
                    val category = ExpenseCategory(
                        id = document.id,
                        category = data?.get("category") as? String ?: ""
                    )
                    categoryList.add(category)
                } catch (e: Exception) {
                    println("[FirestoreExpenseRepository] ❌ Error parsing category document ${document.id}: ${e.message}")
                }
            }
            
            // Sort categories alphabetically
            val sortedCategories = categoryList.sortedBy { it.category.lowercase() }
            
            println("[FirestoreExpenseRepository] ✅ Categories fetched: ${sortedCategories.size} categories")
            
            Result.success(sortedCategories)
        } catch (e: Exception) {
            println("[FirestoreExpenseRepository] ❌ Error fetching categories: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun addCategory(name: String): Result<ExpenseCategory> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            val data: Map<String, Any> = mapOf(
                "category" to name
            )
            
            val docRef = fs.collection("ExpenseCategories").document()
            docRef.set(data).await()
            
            val created = ExpenseCategory(
                id = docRef.id,
                category = name
            )
            
            println("[FirestoreExpenseRepository] ✅ Category created with ID: ${docRef.id}")
            Result.success(created)
        } catch (e: Exception) {
            println("[FirestoreExpenseRepository] ❌ Error adding category: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun saveExpenseTransaction(transaction: ExpenseTransaction): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fs = firestore ?: return@withContext Result.failure(
                IllegalStateException("Firestore not initialized")
            )
            
            val timestamp = if (transaction.createdAt.isBlank()) {
                formatTimestamp(System.currentTimeMillis())
            } else {
                transaction.createdAt
            }
            
            val transactionData: Map<String, Any> = mapOf(
                "categoryId" to transaction.categoryId,
                "categoryName" to transaction.categoryName,
                "createdAt" to timestamp,
                "notes" to transaction.notes,
                "paymentSplit" to mapOf(
                    "bank" to transaction.paymentSplit.bank,
                    "cash" to transaction.paymentSplit.cash,
                    "creditCard" to transaction.paymentSplit.creditCard
                ),
                "totalAmount" to transaction.totalAmount
            )
            
            val docRef = fs.collection("ExpenseTransactions").document()
            docRef.set(transactionData).await()
            
            println("[FirestoreExpenseRepository] ✅ Expense transaction saved with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            println("[FirestoreExpenseRepository] ❌ Error saving expense transaction: ${e.message}")
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

