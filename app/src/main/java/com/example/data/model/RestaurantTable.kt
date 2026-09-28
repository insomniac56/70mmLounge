package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurant_tables")
data class RestaurantTable(
    @PrimaryKey(autoGenerate = true)
    val tableId: Long = 0,
    val tableNumber: String,
    val zone: String = "Club area", // "Club area", "Outdoor", "Back area"
    val capacity: Int = 4,
    val status: String = "AVAILABLE", // "AVAILABLE", "OCCUPIED", "ORDERING", "BILLED"
    val currentGuestName: String = "",
    val activeOrderId: Long? = null,
    val currentBillAmount: Double = 0.0,
    val qrPayload: String = ""
) {
    val isAvailable: Boolean
        get() = status == "AVAILABLE"

    val isOccupied: Boolean
        get() = status == "OCCUPIED" || status == "ORDERING"
}
