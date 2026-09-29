package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.SalesSummary
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DailySummaryManager {

    const val OWNER_WHATSAPP_NUMBER = "8987477773"

    /**
     * Formats a professional and structured end-of-day daily sales closing message for WhatsApp / SMS.
     */
    fun buildDailySummaryMessage(summary: SalesSummary): String {
        val now = Date()
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now)

        val totalOrders = summary.orderCount
        val grossSales = summary.grossSales
        val netSales = (summary.grossSales - summary.totalGst).coerceAtLeast(0.0)
        val totalGst = summary.totalGst
        val netProfit = summary.netProfit

        // Payment Breakdown
        val cashAmount = summary.paymentBreakdown.cashTotal
        val upiAmount = summary.paymentBreakdown.upiTotal
        val cardAmount = summary.paymentBreakdown.cardTotal
        val otherAmount = (grossSales - cashAmount - upiAmount - cardAmount).coerceAtLeast(0.0)

        val cashPercent = if (grossSales > 0) ((cashAmount / grossSales) * 100).toInt() else 0
        val upiPercent = if (grossSales > 0) ((upiAmount / grossSales) * 100).toInt() else 0
        val cardPercent = if (grossSales > 0) ((cardAmount / grossSales) * 100).toInt() else 0

        val sb = StringBuilder()
        sb.appendLine("*📊 70MM LOUNGE & RESTAURANT, BOKARO*")
        sb.appendLine("*🗓️ DAILY END-OF-DAY CLOSING REPORT*")
        sb.appendLine("════════════════════════════")
        sb.appendLine("📅 *Date:* $dateStr | $timeStr")
        sb.appendLine("🏷️ *Branch:* Bokaro Steel City")
        sb.appendLine("📋 *Status:* Day Shift Settled & Closed")
        sb.appendLine("────────────────────────────")
        sb.appendLine("💰 *TOTAL SALES & REVENUE:*")
        sb.appendLine("• *Total Bills / Orders:* $totalOrders")
        sb.appendLine("• *Gross Sales:* ${CurrencyFormatter.format(grossSales)}")
        sb.appendLine("• *Total GST (Tax):* ${CurrencyFormatter.format(totalGst)}")
        sb.appendLine("• *Net Sales:* ${CurrencyFormatter.format(netSales)}")
        sb.appendLine("• *Estimated Net Profit:* ${CurrencyFormatter.format(netProfit)}")
        sb.appendLine("────────────────────────────")
        sb.appendLine("💳 *PAYMENT MODES BREAKDOWN:*")
        sb.appendLine("💵 *Cash (कैश):* ${CurrencyFormatter.format(cashAmount)} ($cashPercent%)")
        sb.appendLine("📱 *UPI / QR (यूपीआई):* ${CurrencyFormatter.format(upiAmount)} ($upiPercent%)")
        sb.appendLine("💳 *Card (कार्ड):* ${CurrencyFormatter.format(cardAmount)} ($cardPercent%)")
        if (otherAmount > 0) {
            sb.appendLine("🏦 *Other / Credit:* ${CurrencyFormatter.format(otherAmount)}")
        }
        sb.appendLine("────────────────────────────")
        
        if (summary.topSellingProducts.isNotEmpty()) {
            sb.appendLine("🍽️ *TOP SELLING ITEMS TODAY:*")
            summary.topSellingProducts.take(4).forEachIndexed { index, product ->
                sb.appendLine("${index + 1}. ${product.productName} (x${product.quantitySold})")
            }
            sb.appendLine("────────────────────────────")
        }

        sb.appendLine("✅ *Day Closing Verified by Manager.*")
        sb.appendLine("📞 *Owner / Manager:* $OWNER_WHATSAPP_NUMBER")
        sb.appendLine("⚡ *Powered by 70MM Lounge POS*")
        return sb.toString()
    }

    /**
     * Opens WhatsApp chat directly with +91 8987477773 with pre-filled daily report message.
     */
    fun sendSummaryToWhatsApp(context: Context, summary: SalesSummary) {
        val message = buildDailySummaryMessage(summary)
        val fullPhone = "91$OWNER_WHATSAPP_NUMBER"

        try {
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$fullPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to standard share sheet
            shareSummaryGeneral(context, message)
        }
    }

    /**
     * Opens SMS app targeted directly to 8987477773 with pre-filled message.
     */
    fun sendSummaryToSms(context: Context, summary: SalesSummary) {
        val message = buildDailySummaryMessage(summary)
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$OWNER_WHATSAPP_NUMBER")
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            shareSummaryGeneral(context, message)
        }
    }

    /**
     * Copies summary to Android clipboard with a confirmation Toast.
     */
    fun copySummaryToClipboard(context: Context, summary: SalesSummary) {
        val message = buildDailySummaryMessage(summary)
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("70mm Daily Summary", message)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Daily summary copied to clipboard! (Ready to paste)", Toast.LENGTH_SHORT).show()
    }

    private fun shareSummaryGeneral(context: Context, message: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, "70MM Lounge Daily Sales Summary")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Send Daily Summary"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open messaging app: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
