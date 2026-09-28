package com.example.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val decimalFormat = DecimalFormat("₹#,##0.00")
    private val percentFormat = DecimalFormat("#,##0.0'%'")
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeOnlyFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun format(amount: Double): String {
        return decimalFormat.format(amount)
    }

    fun formatWhole(amount: Double): String {
        return "₹${amount.toLong()}"
    }

    fun formatPercent(percent: Double): String {
        return percentFormat.format(percent)
    }

    fun formatDateTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }

    fun formatTimeOnly(timestamp: Long): String {
        return timeOnlyFormat.format(Date(timestamp))
    }
}
