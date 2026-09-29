package com.example.data.model

/**
 * Event broadcast when a new KOT or add-on item order is submitted to the kitchen.
 * Used by the Kitchen Display System (KDS) to ring bell chime and speak out table alerts in Hindi.
 */
data class KotAlertEvent(
    val areaName: String,
    val tableNumber: String,
    val isAddon: Boolean, // true if items were added to an already active table
    val timestamp: Long = System.currentTimeMillis()
)
