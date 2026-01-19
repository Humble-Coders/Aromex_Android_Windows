package com.humblecoders.aromex_android_windows.data.repository

import com.humblecoders.aromex_android_windows.domain.model.ExpenseCategory
import com.humblecoders.aromex_android_windows.domain.model.ExpenseTransaction

/**
 * Repository interface for expense categories and transactions.
 */
interface ExpenseRepository {
    /**
     * Fetches all expense categories from Firestore.
     * Should be called when the dropdown is opened.
     * @return Result containing list of categories or error
     */
    suspend fun fetchCategories(): Result<List<ExpenseCategory>>
    
    /**
     * Adds a new expense category to Firestore.
     * @param name The category name to create
     * @return Result containing the created category or error
     */
    suspend fun addCategory(name: String): Result<ExpenseCategory>
    
    /**
     * Saves an expense transaction to Firestore.
     * @return Result indicating success or failure
     */
    suspend fun saveExpenseTransaction(transaction: ExpenseTransaction): Result<String>
}

