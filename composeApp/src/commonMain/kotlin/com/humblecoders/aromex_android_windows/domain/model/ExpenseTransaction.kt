package com.humblecoders.aromex_android_windows.domain.model

data class PaymentSplit(
    val bank: Double = 0.0,
    val cash: Double = 0.0,
    val creditCard: Double = 0.0
)

data class ExpenseTransaction(
    val id: String = "",
    val categoryId: String,
    val categoryName: String,
    val createdAt: String = "",
    val notes: String = "",
    val paymentSplit: PaymentSplit,
    val totalAmount: Double
)

