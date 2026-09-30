package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.SalesSummary
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    fun exportSalesReportToExcel(context: Context, summary: SalesSummary): File? {
        return try {
            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val periodCode = summary.timeRange.name.lowercase(Locale.getDefault())
            val file = File(reportsDir, "sales_report_${periodCode}_$timeStamp.csv")

            FileWriter(file).use { writer ->
                // UTF-8 BOM so Excel opens with proper accents and formatting immediately
                writer.write("\uFEFF")

                // 1. Report Header Section
                writer.appendLine("70MM LOUNGE - RESTAURANT & CLUB SALES & TAX REPORT")
                writer.appendLine("Contact,Phone: 8987477773 | Email: 70mmlounge8bokaro@gmail.com | Bokaro")
                writer.appendLine("Report Period,${escapeCsv(summary.timeRange.label)}")
                writer.appendLine("Generated On,${escapeCsv(SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.getDefault()).format(Date()))}")
                writer.appendLine("Total Transactions,${summary.orderCount}")
                writer.appendLine()

                // 2. Executive Financial Summary
                writer.appendLine("--- EXECUTIVE FINANCIAL SUMMARY ---")
                writer.appendLine("Metric,Amount (₹)")
                writer.appendLine("Gross Revenue,${String.format(Locale.US, "%.2f", summary.grossSales)}")
                writer.appendLine("Total Discounts Given,${String.format(Locale.US, "%.2f", summary.totalDiscounts)}")
                writer.appendLine("Total GST / Tax Collected,${String.format(Locale.US, "%.2f", summary.totalGst)}")
                writer.appendLine("Cost of Goods Sold (COGS),${String.format(Locale.US, "%.2f", summary.totalCostOfGoods)}")
                writer.appendLine("Estimated Net Profit,${String.format(Locale.US, "%.2f", summary.netProfit)}")
                writer.appendLine("Average Order Value (AOV),${String.format(Locale.US, "%.2f", summary.averageOrderValue)}")
                writer.appendLine()

                // 3. Payment Method Breakdown
                writer.appendLine("--- PAYMENT METHODS SUMMARY ---")
                writer.appendLine("Payment Mode,Count,Total Amount (₹)")
                writer.appendLine("Cash,${summary.paymentBreakdown.cashCount},${String.format(Locale.US, "%.2f", summary.paymentBreakdown.cashTotal)}")
                writer.appendLine("Credit/Debit Card,${summary.paymentBreakdown.cardCount},${String.format(Locale.US, "%.2f", summary.paymentBreakdown.cardTotal)}")
                writer.appendLine("UPI / Digital QR,${summary.paymentBreakdown.upiCount},${String.format(Locale.US, "%.2f", summary.paymentBreakdown.upiTotal)}")
                writer.appendLine()

                // 4. GST Tax Slab Breakdown
                writer.appendLine("--- GST TAX ANALYSIS ---")
                writer.appendLine("GST Rate (%),Taxable Value (₹),CGST (₹),SGST (₹),Total GST (₹)")
                for (slab in summary.gstSlabs) {
                    writer.appendLine(
                        "${slab.rate}%," +
                                "${String.format(Locale.US, "%.2f", slab.taxableAmount)}," +
                                "${String.format(Locale.US, "%.2f", slab.cgstAmount)}," +
                                "${String.format(Locale.US, "%.2f", slab.sgstAmount)}," +
                                "${String.format(Locale.US, "%.2f", slab.totalTax)}"
                    )
                }
                writer.appendLine()

                // 5. Category Performance
                writer.appendLine("--- CATEGORY SALES PERFORMANCE ---")
                writer.appendLine("Category,Items Sold,Revenue (₹),Revenue Share (%)")
                for (cat in summary.categoryBreakdown) {
                    writer.appendLine(
                        "${escapeCsv(cat.category)}," +
                                "${cat.itemCount}," +
                                "${String.format(Locale.US, "%.2f", cat.totalRevenue)}," +
                                "${String.format(Locale.US, "%.1f", cat.percentageOfSales)}%"
                    )
                }
                writer.appendLine()

                // 6. Detailed Transaction Register
                writer.appendLine("--- DETAILED SALES TRANSACTION REGISTER ---")
                writer.appendLine(
                    "Invoice No,Date,Time,Customer Name,Phone,Payment Mode,Items Purchased,Subtotal (₹),Discount (₹),GST Tax (₹),Grand Total (₹),COGS (₹),Net Profit (₹),Notes"
                )

                for (orderWithItems in summary.orders) {
                    val order = orderWithItems.order
                    val dateStr = CurrencyFormatter.formatDateOnly(order.timestamp)
                    val timeStr = CurrencyFormatter.formatTimeOnly(order.timestamp)
                    val itemsSummary = orderWithItems.items.joinToString(" ; ") {
                        "${it.productName} (x${it.quantity} @ ₹${it.unitPrice})"
                    }

                    writer.appendLine(
                        "${escapeCsv(order.invoiceNumber)}," +
                                "${escapeCsv(dateStr)}," +
                                "${escapeCsv(timeStr)}," +
                                "${escapeCsv(order.customerName)}," +
                                "${escapeCsv(order.customerPhone)}," +
                                "${order.paymentMethod}," +
                                "${escapeCsv(itemsSummary)}," +
                                "${String.format(Locale.US, "%.2f", order.subtotal)}," +
                                "${String.format(Locale.US, "%.2f", order.discountAmount)}," +
                                "${String.format(Locale.US, "%.2f", order.taxAmount)}," +
                                "${String.format(Locale.US, "%.2f", order.totalAmount)}," +
                                "${String.format(Locale.US, "%.2f", order.totalCost)}," +
                                "${String.format(Locale.US, "%.2f", order.netProfit)}," +
                                "${escapeCsv(order.notes)}"
                    )
                }
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadExcelReport(context: Context, summary: SalesSummary): File? {
        val file = exportSalesReportToExcel(context, summary)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to generate Excel report file", Toast.LENGTH_SHORT).show()
            return null
        }

        try {
            val fileName = file.name
            var savedFile: File? = null

            // Copy to public Downloads directory for 1-click easy access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/70mmLounge")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        file.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    Toast.makeText(context, "✅ Downloaded: Downloads/70mmLounge/$fileName", Toast.LENGTH_LONG).show()
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                file.copyTo(targetFile, overwrite = true)
                savedFile = targetFile
                Toast.makeText(context, "✅ Downloaded to Downloads/$fileName", Toast.LENGTH_LONG).show()
            }

            return savedFile ?: file
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Saved to cache: ${file.name}", Toast.LENGTH_LONG).show()
            return file
        }
    }

    fun openDownloadedFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(viewIntent, "Open Excel / CSV Report"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareExcelReport(context: Context, summary: SalesSummary) {
        val file = exportSalesReportToExcel(context, summary)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to generate Excel report file", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "70mm Lounge ${summary.timeRange.label} Sales & Tax Report")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "70mm Lounge, Bokaro (Phone: 8987477773)\n" +
                            "Attached is the ${summary.timeRange.label} Sales & GST Tax Report.\n" +
                            "Gross Revenue: ${CurrencyFormatter.format(summary.grossSales)}\n" +
                            "GST Collected: ${CurrencyFormatter.format(summary.totalGst)}\n" +
                            "Net Profit: ${CurrencyFormatter.format(summary.netProfit)}"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Open or Share Excel/CSV Report")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing report: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
