package com.humblecoders.aromex_android_windows.domain.model

enum class EntityType {
    CUSTOMER, SUPPLIER, MIDDLEMAN
}

enum class BalanceType {
    TO_RECEIVE, TO_GIVE
}

data class Entity(
    val id: String = "",
    val type: EntityType,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val notes: String,
    val initialBalance: Double,
    val balanceType: BalanceType,
    val createdAt: Long = System.currentTimeMillis()
)
