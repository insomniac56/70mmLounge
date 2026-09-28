package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kitchen_order_tickets")
data class KitchenOrderTicket(
    @PrimaryKey(autoGenerate = true)
    val kotId: Long = 0,
    val kotNumber: String, // e.g. "KOT-101"
    val tableNumber: String,
    val zone: String = "Main Dining",
    val customerName: String = "Table Guest",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "NEW", // "NEW", "PREPARING", "READY", "SERVED", "CANCELLED"
    val section: String = "KITCHEN", // "KITCHEN", "BAR", "GRILL"
    val itemsSummary: String = "", // e.g. "2x Signature Burger (No onion) ; 1x Truffle Fries"
    val completedItems: String = "", // e.g. "1x Truffle Fries"
    val specialNotes: String = ""
)
