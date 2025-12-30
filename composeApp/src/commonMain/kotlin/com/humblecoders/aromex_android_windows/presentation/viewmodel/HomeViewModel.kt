package com.humblecoders.aromex_android_windows.presentation.viewmodel

import com.humblecoders.aromex_android_windows.data.repository.FinancialRepository
import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow

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
        _isLoading.value = true
        financialRepository.addEntity(entity)
            .onEach { result ->
                result.fold(
                    onSuccess = { _isLoading.value = false },
                    onFailure = { e -> 
                        _error.value = e.message
                        _isLoading.value = false
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}
