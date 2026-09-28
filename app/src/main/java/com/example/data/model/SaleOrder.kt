package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_orders")
data class SaleOrder(
    @PrimaryKey(autoGenerate = true)
    val orderId: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val customerName: String = "Walk-in Customer",
    val customerPhone: String = "",
    val paymentMethod: String = "CASH", // CASH, CARD, UPI
    val subtotal: Double,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double,
    val totalAmount: Double,
    val totalCost: Double = 0.0,
    val netProfit: Double = 0.0,
    val tableNumber: String = "",
    val orderType: String = "DINE_IN",
    val notes: String = ""
)
