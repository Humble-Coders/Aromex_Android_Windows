package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.ExpenseRepository
import com.humblecoders.aromex_android_windows.domain.model.ExpenseCategory
import com.humblecoders.aromex_android_windows.domain.model.ExpenseTransaction
import com.humblecoders.aromex_android_windows.domain.model.PaymentSplit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Expense management.
 * Handles fetching categories and saving expense transactions.
 */
class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _categories = MutableStateFlow<List<ExpenseCategory>>(emptyList())
    val categories: StateFlow<List<ExpenseCategory>> = _categories.asStateFlow()
    
    private val _isLoadingCategories = MutableStateFlow(false)
    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories.asStateFlow()
    
    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError.asStateFlow()
    
    private val _isSavingTransaction = MutableStateFlow(false)
    val isSavingTransaction: StateFlow<Boolean> = _isSavingTransaction.asStateFlow()
    
    private val _saveTransactionError = MutableStateFlow<String?>(null)
    val saveTransactionError: StateFlow<String?> = _saveTransactionError.asStateFlow()
    
    private val _transactionSaved = MutableStateFlow(false)
    val transactionSaved: StateFlow<Boolean> = _transactionSaved.asStateFlow()
    
    private val _showExpenseSuccess = MutableStateFlow(false)
    val showExpenseSuccess: StateFlow<Boolean> = _showExpenseSuccess.asStateFlow()
    
    /**
     * Fetches expense categories from Firestore.
     * @param forceRefresh If true, fetches even if categories are already loaded
     */
    fun fetchCategories(forceRefresh: Boolean = false) {
        if (!forceRefresh && (_categories.value.isNotEmpty() || _isLoadingCategories.value)) {
            return
        }
        
        viewModelScope.launch {
            _isLoadingCategories.value = true
            _categoryError.value = null
            
            expenseRepository.fetchCategories().fold(
                onSuccess = { fetchedCategories ->
                    _categories.value = fetchedCategories
                    _isLoadingCategories.value = false
                },
                onFailure = { error ->
                    _categoryError.value = error.message
                    _isLoadingCategories.value = false
                    println("[ExpenseViewModel] Error fetching categories: ${error.message}")
                }
            )
        }
    }
    
    /**
     * Adds a new expense category and updates the local list.
     * @param name The category name to create
     * @param onCategoryCreated Optional callback that receives the newly created category
     */
    fun addCategory(name: String, onCategoryCreated: ((ExpenseCategory) -> Unit)? = null) {
        if (name.isBlank()) return
        
        viewModelScope.launch {
            _isLoadingCategories.value = true
            _categoryError.value = null
            
            expenseRepository.addCategory(name).fold(
                onSuccess = { created ->
                    // Append and keep list sorted
                    val updated = (_categories.value + created)
                        .distinctBy { it.id }
                        .sortedBy { it.category.lowercase() }
                    _categories.value = updated
                    _isLoadingCategories.value = false
                    // Call the callback with the created category
                    onCategoryCreated?.invoke(created)
                },
                onFailure = { error ->
                    _categoryError.value = error.message
                    _isLoadingCategories.value = false
                    println("[ExpenseViewModel] Error adding category: ${error.message}")
                }
            )
        }
    }
    
    /**
     * Saves an expense transaction to Firestore.
     */
    fun saveExpenseTransaction(
        categoryId: String,
        categoryName: String,
        totalAmount: Double,
        paymentSplit: PaymentSplit,
        notes: String = ""
    ) {
        viewModelScope.launch {
            _isSavingTransaction.value = true
            _saveTransactionError.value = null
            _transactionSaved.value = false
            
            val transaction = ExpenseTransaction(
                categoryId = categoryId,
                categoryName = categoryName,
                createdAt = "", // Will be formatted by repository
                notes = notes,
                paymentSplit = paymentSplit,
                totalAmount = totalAmount
            )
            
            expenseRepository.saveExpenseTransaction(transaction).fold(
                onSuccess = { transactionId ->
                    // Keep saving loader visible for a bit longer
                    kotlinx.coroutines.delay(1000)
                    _isSavingTransaction.value = false
                    _showExpenseSuccess.value = true
                    _transactionSaved.value = true
                    println("[ExpenseViewModel] Transaction saved with ID: $transactionId")
                },
                onFailure = { error ->
                    _isSavingTransaction.value = false
                    _saveTransactionError.value = error.message
                    println("[ExpenseViewModel] Error saving transaction: ${error.message}")
                }
            )
        }
    }
    
    /**
     * Resets the transaction saved state.
     */
    fun resetTransactionSaved() {
        _transactionSaved.value = false
    }
    
    /**
     * Dismisses the expense success dialog.
     */
    fun dismissExpenseSuccess() {
        _showExpenseSuccess.value = false
    }
    
    /**
     * Clears category error.
     */
    fun clearCategoryError() {
        _categoryError.value = null
    }
    
    /**
     * Clears save transaction error.
     */
    fun clearSaveTransactionError() {
        _saveTransactionError.value = null
    }
}

