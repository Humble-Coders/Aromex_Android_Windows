package com.humblecoders.aromex_android_windows.domain.model

data class FinancialOverview(
    val accountBalance: AccountBalance = AccountBalance(),
    val debtOverview: DebtOverview = DebtOverview()
)

