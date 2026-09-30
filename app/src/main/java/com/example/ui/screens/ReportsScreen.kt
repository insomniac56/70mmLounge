package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GstSlabSummary
import com.example.data.model.ReportTimeRange
import com.example.data.model.SalesSummary
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CurrencyFormatter
import com.example.util.DailySummaryManager
import com.example.util.ExcelExporter

@Composable
fun ReportsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedRange by viewModel.selectedReportRange.collectAsStateWithLifecycle()
    val salesSummary by viewModel.currentSalesSummary.collectAsStateWithLifecycle()

    val ranges = ReportTimeRange.values()
    var downloadedExcelFile by remember { mutableStateOf<java.io.File?>(null) }

    if (downloadedExcelFile != null) {
        val file = downloadedExcelFile!!
        AlertDialog(
            onDismissRequest = { downloadedExcelFile = null },
            title = {
                Text("📊 Excel Report Downloaded", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "File downloaded successfully to your device Downloads folder:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        file.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        ExcelExporter.openDownloadedFile(context, file)
                        downloadedExcelFile = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                ) {
                    Text("Open File 📂")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            ExcelExporter.shareExcelReport(context, salesSummary)
                            downloadedExcelFile = null
                        }
                    ) {
                        Text("Share 📤")
                    }
                    TextButton(onClick = { downloadedExcelFile = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Action Bar: Title & WhatsApp Daily Report & Excel Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sales & Tax Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real-time GST, profit & closing summary",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp to Owner Button (8987477773)
                Button(
                    onClick = {
                        DailySummaryManager.sendSummaryToWhatsApp(context, salesSummary)
                    },
                    modifier = Modifier.testTag("whatsapp_summary_top_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Summary",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }

                // Export to Excel / CSV Button (1-Click Download + Share)
                OutlinedButton(
                    onClick = {
                        val file = ExcelExporter.downloadExcelReport(context, salesSummary)
                        if (file != null) {
                            downloadedExcelFile = file
                        }
                    },
                    modifier = Modifier.testTag("export_excel_button"),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export Excel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Range Tabs: Today (Daily), Week, Month, All
        TabRow(
            selectedTabIndex = ranges.indexOf(selectedRange),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[ranges.indexOf(selectedRange)]),
                    color = Emerald600,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            ranges.forEach { range ->
                val isSelected = (selectedRange == range)
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setReportTimeRange(range) },
                    text = {
                        Text(
                            text = when (range) {
                                ReportTimeRange.TODAY -> "Daily"
                                ReportTimeRange.THIS_WEEK -> "Weekly"
                                ReportTimeRange.THIS_MONTH -> "Monthly"
                                ReportTimeRange.ALL_TIME -> "All Time"
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("report_tab_${range.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Scrollable
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Daily Closing Summary & WhatsApp to Owner (8987477773)
            item {
                DailyClosingWhatsAppCard(summary = salesSummary)
            }

            // KPI Financial Metrics Grid
            item {
                KpiMetricsGrid(summary = salesSummary)
            }

            // GST Tax Analysis Card
            item {
                GstTaxAnalysisCard(slabs = salesSummary.gstSlabs, totalTax = salesSummary.totalGst)
            }

            // Payment Methods Breakdown Card
            item {
                PaymentBreakdownCard(summary = salesSummary)
            }

            // Category Sales Breakdown
            item {
                CategorySalesCard(categories = salesSummary.categoryBreakdown)
            }

            // Top Selling Menu Items
            item {
                TopSellingProductsCard(products = salesSummary.topSellingProducts)
            }
        }
    }
}

@Composable
private fun KpiMetricsGrid(summary: SalesSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Gross Sales & Net Profit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gross Sales Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gross Revenue", style = MaterialTheme.typography.labelSmall, color = Slate400)
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.grossSales),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.orderCount} orders • AOV ${CurrencyFormatter.format(summary.averageOrderValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            // Net Profit Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(0.8.dp, Emerald100, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Emerald50),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val margin = if (summary.grossSales > 0) (summary.netProfit / summary.grossSales) * 100.0 else 0.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = Emerald600, fontWeight = FontWeight.Bold)
                        Text("${String.format("%.1f", margin)}% margin", style = MaterialTheme.typography.labelSmall, color = Emerald600, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.netProfit),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Emerald600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "COGS: ${CurrencyFormatter.format(summary.totalCostOfGoods)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Row 2: Total GST & Discounts Given
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GST Tax Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("GST Tax Collected", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalGst),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CGST: ${CurrencyFormatter.format(summary.totalGst / 2)} | SGST: ${CurrencyFormatter.format(summary.totalGst / 2)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }

            // Discounts Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Discounts Given", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalDiscounts),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val discountPercent = if (summary.grossSales > 0) (summary.totalDiscounts / (summary.grossSales + summary.totalDiscounts)) * 100.0 else 0.0
                    Text(
                        text = "Avg ${String.format("%.1f", discountPercent)}% off gross",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GstTaxAnalysisCard(
    slabs: List<GstSlabSummary>,
    totalTax: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Sky600, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GST Tax Slabs Breakdown",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Total: ${CurrencyFormatter.format(totalTax)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Emerald600
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text("Slab", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(0.8f))
                Text("Taxable", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                Text("CGST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("SGST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("Total Tax", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (slabs.isEmpty()) {
                Text(
                    text = "No tax collected in this period",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                for (slab in slabs) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${slab.rate.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.taxableAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.cgstAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.sgstAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.totalTax),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = Emerald600,
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun PaymentBreakdownCard(summary: SalesSummary) {
    val pb = summary.paymentBreakdown
    val total = (pb.cashTotal + pb.cardTotal + pb.upiTotal).coerceAtLeast(0.01)

    val cashPct = (pb.cashTotal / total).toFloat()
    val cardPct = (pb.cardTotal / total).toFloat()
    val upiPct = (pb.upiTotal / total).toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Payment Modes Distribution",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (cashPct > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(cashPct)
                            .background(Emerald600)
                    )
                }
                if (cardPct > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(cardPct)
                            .background(Sky600)
                    )
                }
                if (upiPct > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(upiPct)
                            .background(Color(0xFF8B5CF6))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Breakdown stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaymentStatPill("Cash", CurrencyFormatter.format(pb.cashTotal), pb.cashCount, Emerald600)
                PaymentStatPill("Card", CurrencyFormatter.format(pb.cardTotal), pb.cardCount, Sky600)
                PaymentStatPill("UPI / QR", CurrencyFormatter.format(pb.upiTotal), pb.upiCount, Color(0xFF8B5CF6))
            }
        }
    }
}

@Composable
private fun PaymentStatPill(mode: String, amount: String, count: Int, dotColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(mode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(amount, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("$count txns", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
    }
}

@Composable
private fun CategorySalesCard(categories: List<com.example.data.model.CategorySaleStat>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Sales by Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (categories.isEmpty()) {
                Text("No category sales recorded yet", color = Slate400, style = MaterialTheme.typography.bodySmall)
            } else {
                for (cat in categories) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cat.category,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${CurrencyFormatter.format(cat.totalRevenue)} (${String.format("%.1f", cat.percentageOfSales)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (cat.percentageOfSales / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Emerald600,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopSellingProductsCard(products: List<com.example.data.model.TopSellingProduct>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.8.dp, Slate200, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top Selling Menu Items",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(Icons.Default.Assessment, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (products.isEmpty()) {
                Text("No items sold in this period", color = Slate400, style = MaterialTheme.typography.bodySmall)
            } else {
                for ((index, product) in products.withIndex()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(22.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = if (index < 3) Emerald600 else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index < 3) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${product.category} • ${product.quantitySold} units sold",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = CurrencyFormatter.format(product.totalRevenue),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                    }
                    if (index < products.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyClosingWhatsAppCard(summary: SalesSummary) {
    val context = LocalContext.current
    var showPreview by remember { mutableStateOf(false) }

    val cashAmount = summary.paymentBreakdown.cashTotal
    val upiAmount = summary.paymentBreakdown.upiTotal
    val cardAmount = summary.paymentBreakdown.cardTotal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.2.dp, Emerald600.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald600
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Closing WhatsApp & SMS Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Auto Day-End Report to Owner: ${DailySummaryManager.OWNER_WHATSAPP_NUMBER}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Emerald600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Payment Breakdown Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Sales
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Emerald50,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Gross Sales", fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Text(CurrencyFormatter.format(summary.grossSales), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                    }
                }
                // Cash
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Slate100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Cash (कैश)", fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Text(CurrencyFormatter.format(cashAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                }
                // UPI
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Sky100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("UPI (यूपीआई)", fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Text(CurrencyFormatter.format(upiAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Sky600)
                    }
                }
                // Card
                Surface(
                    modifier = Modifier.weight(1f),
                    color = Amber100,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Card (कार्ड)", fontSize = 10.sp, color = Slate600, fontWeight = FontWeight.Medium)
                        Text(CurrencyFormatter.format(cardAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Amber500)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        DailySummaryManager.sendSummaryToWhatsApp(context, summary)
                    },
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp (8987477773)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        DailySummaryManager.sendSummaryToSms(context, summary)
                    },
                    modifier = Modifier.weight(0.85f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SMS", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        DailySummaryManager.copySummaryToClipboard(context, summary)
                    },
                    modifier = Modifier.weight(0.85f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            // Preview Message Accordion
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPreview = !showPreview }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showPreview) "Hide Message Preview ▲" else "Preview Daily Closing Message ▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showPreview) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Slate100,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = DailySummaryManager.buildDailySummaryMessage(summary),
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Slate800
                    )
                }
            }
        }
    }
}
