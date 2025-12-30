package com.humblecoders.aromex_android_windows.data.repository

import com.humblecoders.aromex_android_windows.domain.model.AccountBalance
import com.humblecoders.aromex_android_windows.domain.model.DebtOverview
import com.humblecoders.aromex_android_windows.domain.model.Entity
import kotlinx.coroutines.flow.Flow

interface FinancialRepository {
    fun getAccountBalance(): Flow<AccountBalance>
    fun getDebtOverview(): Flow<DebtOverview>
    fun updateAccountBalance(accountBalance: AccountBalance): Flow<Result<Unit>>
    fun updateSingleBalance(balanceType: String, amount: Double): Flow<Result<Unit>>
    fun addEntity(entity: Entity): Flow<Result<Unit>>
}

// interface -> WHAT (commonMain) -> function name, parameters, return type
// {} -> implementation -> HOW -> platform specific (androidMain/ JvmMain)
