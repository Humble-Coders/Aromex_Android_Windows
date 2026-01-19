package com.humblecoders.aromex_android_windows.domain.model

data class Product(
    val id: String = "",
    val brandId: String,
    val brandName: String,
    val capacityId: String,
    val capacity: String,
    val capacityUnit: String,
    val carrierId: String,
    val carrierName: String,
    val colorId: String,
    val colorName: String,
    val IMEI: String,
    val modelId: String,
    val modelName: String,
    val status: String,
    val storageLocationId: String,
    val storageLocation: String,
    val unitCost: Double,
    val updatedAt: String = ""
)

