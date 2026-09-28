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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Action Bar: Title & Excel Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sales & Tax Reports",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Real-time GST, profit & performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            // Export to Excel / CSV Button
            Button(
                onClick = {
                    ExcelExporter.shareExcelReport(context, salesSummary)
                },
                modifier = Modifier.testTag("export_excel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Excel",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Excel Report", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Range Tabs: Today (Daily), Week, Month, All
        TabRow(
            selectedTabIndex = ranges.indexOf(selectedRange),
            containerColor = Slate100,
            contentColor = Slate900,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[ranges.indexOf(selectedRange)]),
                    color = Slate900,
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
                            color = if (isSelected) Slate900 else Slate600
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
                        Text("Gross Revenue", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.grossSales),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${summary.orderCount} orders • AOV ${CurrencyFormatter.format(summary.averageOrderValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
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
                    Text("GST Tax Collected", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalGst),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CGST: ${CurrencyFormatter.format(summary.totalGst / 2)} | SGST: ${CurrencyFormatter.format(summary.totalGst / 2)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
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
                    Text("Discounts Given", style = MaterialTheme.typography.labelSmall, color = Slate500)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyFormatter.format(summary.totalDiscounts),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val discountPercent = if (summary.grossSales > 0) (summary.totalDiscounts / (summary.grossSales + summary.totalDiscounts)) * 100.0 else 0.0
                    Text(
                        text = "Avg ${String.format("%.1f", discountPercent)}% off gross",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
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
                        color = Slate900
                    )
                }

                Text(
                    text = "Total: ${CurrencyFormatter.format(totalTax)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Sky600
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate100)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text("Slab", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700, modifier = Modifier.weight(0.8f))
                Text("Taxable", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
                Text("CGST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("SGST", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
                Text("Total Tax", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Slate700, textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
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
                            color = Slate900,
                            modifier = Modifier.weight(0.8f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.taxableAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = Slate800,
                            modifier = Modifier.weight(1.2f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.cgstAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = Slate600,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = CurrencyFormatter.format(slab.sgstAmount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            color = Slate600,
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
                    HorizontalDivider(color = Slate100, thickness = 0.5.dp)
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
                color = Slate900
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Slate200)
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
                            .background(Slate900)
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
                PaymentStatPill("UPI / QR", CurrencyFormatter.format(pb.upiTotal), pb.upiCount, Slate900)
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
            Text(mode, style = MaterialTheme.typography.labelSmall, color = Slate600)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(amount, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Slate900)
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
                color = Slate900
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
                                color = Slate800
                            )
                            Text(
                                text = "${CurrencyFormatter.format(cat.totalRevenue)} (${String.format("%.1f", cat.percentageOfSales)}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = { (cat.percentageOfSales / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Slate900,
                            trackColor = Slate100
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
                    color = Slate900
                )
                Icon(Icons.Default.Assessment, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
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
                            color = if (index < 3) Slate900 else Slate100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index < 3) Color.White else Slate700
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.productName,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Text(
                                text = "${product.category} • ${product.quantitySold} units sold",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500,
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
                        HorizontalDivider(color = Slate100, thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}
