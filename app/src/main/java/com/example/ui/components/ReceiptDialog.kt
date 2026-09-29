package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.util.PrinterManager
import kotlinx.coroutines.launch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.OrderWithItems
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.CurrencyFormatter

@Composable
fun ReceiptDialog(
    orderWithItems: OrderWithItems,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val order = orderWithItems.order
    val items = orderWithItems.items
    var showPrinterSettings by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("receipt_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with status & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Emerald50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = Emerald600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Sale Completed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = order.invoiceNumber,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_receipt_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Thermal receipt paper styling container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Store header
                        Text(
                            text = "70MM LOUNGE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            letterSpacing = 1.5.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Restaurant • Bar • Club Lounge",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Slate700
                        )
                        Text(
                            text = "Bokaro • Tel: 8987477773",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Slate500
                        )
                        Text(
                            text = "Email: 70mmlounge8bokaro@gmail.com",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptDottedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Meta: Date, Customer, Payment, Table
                        ReceiptMetaRow("Invoice No", order.invoiceNumber)
                        ReceiptMetaRow("Date & Time", CurrencyFormatter.formatDateTime(order.timestamp))
                        if (order.tableNumber.isNotBlank()) {
                            ReceiptMetaRow("Table / Zone", order.tableNumber)
                        }
                        ReceiptMetaRow("Customer", order.customerName)
                        if (order.customerPhone.isNotBlank()) {
                            ReceiptMetaRow("Phone", order.customerPhone)
                        }
                        ReceiptMetaRow("Payment Mode", order.paymentMethod)

                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptDottedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Table header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Item",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                modifier = Modifier.weight(1.8f)
                            )
                            Text(
                                text = "Qty",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(0.6f)
                            )
                            Text(
                                text = "Rate",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(0.8f)
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1.0f)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Slate200, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Items list
                        for (item in items) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.8f)) {
                                    Text(
                                        text = item.productName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "GST ${item.gstRate.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = Slate500
                                    )
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = Slate800,
                                    modifier = Modifier.weight(0.6f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(item.unitPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.End,
                                    color = Slate800,
                                    modifier = Modifier.weight(0.8f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(item.totalAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.End,
                                    color = Slate900,
                                    modifier = Modifier.weight(1.0f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        ReceiptDottedDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Calculations summary
                        ReceiptSummaryRow("Items Subtotal", CurrencyFormatter.format(order.subtotal))

                        if (order.discountAmount > 0) {
                            ReceiptSummaryRow(
                                "Discount" + if (order.discountPercent > 0) " (${order.discountPercent.toInt()}%)" else "",
                                "- ${CurrencyFormatter.format(order.discountAmount)}",
                                isDiscount = true
                            )
                        }

                        // GST breakdown (CGST + SGST)
                        val halfTax = order.taxAmount / 2.0
                        ReceiptSummaryRow("Central GST (CGST)", CurrencyFormatter.format(halfTax))
                        ReceiptSummaryRow("State GST (SGST)", CurrencyFormatter.format(halfTax))
                        ReceiptSummaryRow("Total GST Tax", CurrencyFormatter.format(order.taxAmount))

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Slate900, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grand Total
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL AMOUNT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                            Text(
                                text = CurrencyFormatter.format(order.totalAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Emerald600
                            )
                        }

                        if (order.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = order.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "*** THANK YOU FOR SHOPPING! ***",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-detected printer status card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showPrinterSettings = true },
                    color = Emerald50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Emerald600)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = PrinterManager.getDetectedPrinterSummary(context),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Direct 1-Click Thermal Print with Auto-Cut",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Emerald600
                                )
                            }
                        }
                        Text(
                            text = "Settings ⚙️",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Print Bill, Share, Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                PrinterManager.printReceipt(context, orderWithItems)
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("print_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print Receipt",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Bill", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { shareReceiptAsText(context, orderWithItems) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_receipt_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Receipt",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("dismiss_receipt_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showPrinterSettings) {
        PrinterSettingsDialog(onDismiss = { showPrinterSettings = false })
    }
}

@Composable
private fun ReceiptMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Slate500)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Slate800
        )
    }
}

@Composable
private fun ReceiptSummaryRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDiscount) Emerald600 else Slate600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isDiscount) FontWeight.Bold else FontWeight.Medium,
            color = if (isDiscount) Emerald600 else Slate900
        )
    }
}

@Composable
private fun ReceiptDottedDivider() {
    Text(
        text = "--------------------------------------------------------",
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        color = Slate400,
        modifier = Modifier.fillMaxWidth(),
        letterSpacing = 2.sp
    )
}

private fun shareReceiptAsText(context: Context, orderWithItems: OrderWithItems) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    val sb = StringBuilder()
    sb.appendLine("====== 70MM LOUNGE ======")
    sb.appendLine("Restaurant • Bar • Club Lounge")
    sb.appendLine("Bokaro | Tel: 8987477773")
    sb.appendLine("Email: 70mmlounge8bokaro@gmail.com")
    sb.appendLine("Invoice No: ${order.invoiceNumber}")
    sb.appendLine("Date: ${CurrencyFormatter.formatDateTime(order.timestamp)}")
    if (order.tableNumber.isNotBlank()) {
        sb.appendLine("Table: ${order.tableNumber}")
    }
    sb.appendLine("Customer: ${order.customerName}")
    sb.appendLine("Payment: ${order.paymentMethod}")
    sb.appendLine("----------------------------------")
    for (item in items) {
        sb.appendLine("${item.productName}")
        sb.appendLine("  ${item.quantity} x ${CurrencyFormatter.format(item.unitPrice)} (GST ${item.gstRate.toInt()}%) = ${CurrencyFormatter.format(item.totalAmount)}")
    }
    sb.appendLine("----------------------------------")
    sb.appendLine("Subtotal: ${CurrencyFormatter.format(order.subtotal)}")
    if (order.discountAmount > 0) {
        sb.appendLine("Discount: -${CurrencyFormatter.format(order.discountAmount)}")
    }
    sb.appendLine("CGST: ${CurrencyFormatter.format(order.taxAmount / 2.0)}")
    sb.appendLine("SGST: ${CurrencyFormatter.format(order.taxAmount / 2.0)}")
    sb.appendLine("Total GST: ${CurrencyFormatter.format(order.taxAmount)}")
    sb.appendLine("TOTAL: ${CurrencyFormatter.format(order.totalAmount)}")
    if (order.notes.isNotBlank()) {
        sb.appendLine("Notes: ${order.notes}")
    }
    sb.appendLine("==================================")
    sb.appendLine("Thank you for your business!")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Receipt ${order.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
}
