package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.FinancialRepository
import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.BalanceType
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
import com.humblecoders.aromex_android_windows.domain.model.Entity
import com.humblecoders.aromex_android_windows.domain.model.EntityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Result of balance formatting containing the formatted text and cursor position
 */
data class FormattedBalanceResult(
    val text: String,
    val cursorPosition: Int
)

class HomeViewModel(
    private val financialRepository: FinancialRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _accountBalance = MutableStateFlow<AccountBalance>(AccountBalance())
    val accountBalance: StateFlow<AccountBalance> = _accountBalance.asStateFlow()
    
    private val _debtOverview = MutableStateFlow<DebtOverview>(DebtOverview())
    val debtOverview: StateFlow<DebtOverview> = _debtOverview.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Add Entity Sheet/Dialog State
    private val _showAddEntitySheet = MutableStateFlow(false)
    val showAddEntitySheet: StateFlow<Boolean> = _showAddEntitySheet.asStateFlow()
    
    private val _isSavingEntity = MutableStateFlow(false)
    val isSavingEntity: StateFlow<Boolean> = _isSavingEntity.asStateFlow()
    
    private val _showEntitySuccess = MutableStateFlow(false)
    val showEntitySuccess: StateFlow<Boolean> = _showEntitySuccess.asStateFlow()
    
    private val _lastSavedEntityType = MutableStateFlow<EntityType?>(null)
    val lastSavedEntityType: StateFlow<EntityType?> = _lastSavedEntityType.asStateFlow()
    
    // Edit Balance Sheet/Dialog State
    private val _showEditBalanceSheet = MutableStateFlow(false)
    val showEditBalanceSheet: StateFlow<Boolean> = _showEditBalanceSheet.asStateFlow()
    
    private val _editingBalanceType = MutableStateFlow<String?>(null)
    val editingBalanceType: StateFlow<String?> = _editingBalanceType.asStateFlow()
    
    private val _editingCurrentAmount = MutableStateFlow(0.0)
    val editingCurrentAmount: StateFlow<Double> = _editingCurrentAmount.asStateFlow()
    
    init {
        loadAccountBalance()
        loadDebtOverview()
    }
    
    private fun loadAccountBalance() {
        _isLoading.value = true
        financialRepository.getAccountBalance()
            .onEach { balance ->
                _accountBalance.value = balance
                _isLoading.value = false
            }
            .catch { e ->
                _error.value = e.message
                _isLoading.value = false
            }
            .launchIn(viewModelScope)
    }

    private fun loadDebtOverview() {
        financialRepository.getDebtOverview()
            .onEach { overview ->
                _debtOverview.value = overview
            }
            .catch { e ->
                _error.value = e.message
            }
            .launchIn(viewModelScope)
    }

    fun updateAccountBalance(accountBalance: AccountBalance) {
        financialRepository.updateAccountBalance(accountBalance)
            .onEach { result ->
                result.fold(
                    onSuccess = { loadAccountBalance() },
                    onFailure = { e -> _error.value = e.message }
                )
            }
            .launchIn(viewModelScope)
    }
    
    fun updateSingleBalance(balanceType: String, amount: Double) {
        financialRepository.updateSingleBalance(balanceType, amount)
            .onEach { result ->
                result.fold(
                    onSuccess = { loadAccountBalance() },
                    onFailure = { e -> _error.value = e.message }
                )
            }
            .launchIn(viewModelScope)
    }

    fun addEntity(entity: Entity) {
        _isSavingEntity.value = true
        _lastSavedEntityType.value = entity.type
        financialRepository.addEntity(entity)
            .onEach { result ->
                result.fold(
                    onSuccess = { 
                        _isSavingEntity.value = false
                        _showEntitySuccess.value = true
                        _showAddEntitySheet.value = false
                    },
                    onFailure = { e -> 
                        _error.value = e.message
                        _isSavingEntity.value = false
                    }
                )
            }
            .launchIn(viewModelScope)
    }
    
    // Add Entity Sheet/Dialog Methods
    fun showAddEntitySheet() {
        _showAddEntitySheet.value = true
    }
    
    fun dismissAddEntitySheet() {
        _showAddEntitySheet.value = false
    }
    
    fun dismissEntitySuccess() {
        _showEntitySuccess.value = false
    }
    
    // Edit Balance Sheet/Dialog Methods
    fun showEditBalanceSheet(balanceType: String, currentAmount: Double) {
        _editingBalanceType.value = balanceType
        _editingCurrentAmount.value = currentAmount
        _showEditBalanceSheet.value = true
    }
    
    fun dismissEditBalanceSheet() {
        _showEditBalanceSheet.value = false
        _editingBalanceType.value = null
        _editingCurrentAmount.value = 0.0
    }
    
    // Balance Formatting Logic - Shared between Android and Windows
    
    /**
     * Formats balance input based on balance type with cursor position calculation
     * This handles the onValueChange logic for balance input fields
     * 
     * @param newText The new text input
     * @param newCursorPosition The new cursor position from the input
     * @param oldText The previous text
     * @param oldCursorPosition The previous cursor position
     * @param balanceType The current balance type (TO_RECEIVE or TO_GIVE)
     * @return FormattedBalanceResult containing formatted text and adjusted cursor position
     */
    fun formatBalanceInput(
        newText: String,
        newCursorPosition: Int,
        oldText: String,
        oldCursorPosition: Int,
        balanceType: BalanceType
    ): FormattedBalanceResult {
        // Filter to allow only digits, decimal point, and negative sign
        var filtered = newText.filter { it.isDigit() || it == '.' || it == '-' }
        
        // Ensure only one decimal point
        val decimalCount = filtered.count { it == '.' }
        if (decimalCount > 1) {
            val firstDecimalIndex = filtered.indexOf('.')
            filtered = filtered.substring(0, firstDecimalIndex + 1) + 
                      filtered.substring(firstDecimalIndex + 1).replace(".", "")
        }
        
        // Apply balance type logic and calculate cursor position
        return when (balanceType) {
            BalanceType.TO_RECEIVE -> {
                // Remove negative sign if present, keep only positive
                val result = filtered.replace("-", "")
                // Calculate cursor offset: if we removed a negative sign before cursor, adjust
                val removedBeforeCursor = if (oldText.startsWith("-") && oldCursorPosition > 0) 1 else 0
                val newCursor = (newCursorPosition - removedBeforeCursor).coerceIn(0, result.length)
                FormattedBalanceResult(result, newCursor)
            }
            BalanceType.TO_GIVE -> {
                // Extract numeric part (digits and decimal point)
                val numericPart = filtered.replace("-", "")
                val isZero = numericPart.toDoubleOrNull() == 0.0
                
                if (numericPart.isEmpty() || numericPart == ".") {
                    FormattedBalanceResult(numericPart, newCursorPosition.coerceIn(0, numericPart.length))
                } else {
                    // For TO_GIVE, we need negative sign unless it is zero
                    val finalText = if (filtered.startsWith("-") && 
                        filtered.substring(1) == numericPart &&
                        numericPart.all { it.isDigit() || it == '.' }) {
                        // Value is already in correct format - use it directly to preserve cursor
                        if (isZero) numericPart else filtered
                    } else {
                        // Need to ensure negative sign is at the start
                        if (isZero) numericPart else "-$numericPart"
                    }
                    
                    // Calculate cursor position
                    val newCursor = if (filtered.startsWith("-") && filtered == finalText) {
                        // Value was already correct, preserve cursor position from newValue
                        newCursorPosition.coerceIn(0, finalText.length)
                    } else {
                        // We reconstructed the string, need to adjust cursor
                        // If old text didn't have negative and new does, add 1 to cursor
                        val cursorAdjustment = if (!oldText.startsWith("-") && finalText.startsWith("-")) {
                            1
                        } else if (oldText.startsWith("-") && !finalText.startsWith("-")) {
                            -1
                        } else {
                            0
                        }
                        (newCursorPosition + cursorAdjustment).coerceIn(0, finalText.length)
                    }
                    
                    FormattedBalanceResult(finalText, newCursor)
                }
            }
        }
    }
    
    /**
     * Formats balance when balance type is changed (e.g., via button click)
     * 
     * @param currentText The current balance text
     * @param currentCursorPosition The current cursor position
     * @param newBalanceType The new balance type being selected
     * @return FormattedBalanceResult containing formatted text and adjusted cursor position
     */
    fun formatBalanceOnTypeChange(
        currentText: String,
        currentCursorPosition: Int,
        newBalanceType: BalanceType
    ): FormattedBalanceResult {
        val numericValue = currentText.replace("-", "").replace(".", "")
        if (numericValue.isNotEmpty() && numericValue.all { it.isDigit() }) {
            val newText = when (newBalanceType) {
                BalanceType.TO_RECEIVE -> {
                    // Remove negative sign if present
                    currentText.replace("-", "")
                }
                BalanceType.TO_GIVE -> {
                    // Add negative sign if not present and value exists, unless zero
                    val valueWithoutSign = currentText.replace("-", "")
                    val isZero = valueWithoutSign.toDoubleOrNull() == 0.0
                    
                    if (isZero) {
                        valueWithoutSign
                    } else if (valueWithoutSign.isNotEmpty() && !currentText.startsWith("-")) {
                        "-$valueWithoutSign"
                    } else {
                        currentText
                    }
                }
            }
            // Set cursor to end when TO_GIVE is selected and negative sign is added
            val cursorPos = if (newBalanceType == BalanceType.TO_GIVE && 
                newText.startsWith("-") && !currentText.startsWith("-")) {
                newText.length
            } else {
                currentCursorPosition.coerceIn(0, newText.length)
            }
            return FormattedBalanceResult(newText, cursorPos)
        }
        // If no valid numeric value, return unchanged
        return FormattedBalanceResult(currentText, currentCursorPosition)
    }
    
    /**
     * Formats balance when balance type changes via LaunchedEffect or similar reactive change
     * This is a simpler version that doesn't require cursor adjustment logic
     * 
     * @param currentText The current balance text
     * @param currentCursorPosition The current cursor position
     * @param balanceType The current balance type
     * @return FormattedBalanceResult containing formatted text and adjusted cursor position
     */
    fun formatBalanceOnTypeChangeSimple(
        currentText: String,
        currentCursorPosition: Int,
        balanceType: BalanceType
    ): FormattedBalanceResult {
        val numericValue = currentText.replace("-", "").replace(".", "")
        if (numericValue.isNotEmpty() && numericValue.all { it.isDigit() }) {
            val newText = when (balanceType) {
                BalanceType.TO_RECEIVE -> {
                    // Ensure positive
                    if (currentText.startsWith("-")) {
                        currentText.replace("-", "")
                    } else {
                        currentText
                    }
                }
                BalanceType.TO_GIVE -> {
                    // Ensure negative unless zero
                    val valueWithoutSign = currentText.replace("-", "")
                    val isZero = valueWithoutSign.toDoubleOrNull() == 0.0
                    
                    if (isZero) {
                        valueWithoutSign
                    } else if (!currentText.startsWith("-") && currentText.isNotEmpty()) {
                        "-$currentText"
                    } else {
                        currentText
                    }
                }
            }
            // Set cursor to end when TO_GIVE is selected and negative sign is added
            val cursorPos = if (balanceType == BalanceType.TO_GIVE && 
                newText.startsWith("-") && !currentText.startsWith("-")) {
                newText.length
            } else {
                currentCursorPosition.coerceIn(0, newText.length)
            }
            return FormattedBalanceResult(newText, cursorPos)
        }
        // If no valid numeric value, return unchanged
        return FormattedBalanceResult(currentText, currentCursorPosition)
    }
}
