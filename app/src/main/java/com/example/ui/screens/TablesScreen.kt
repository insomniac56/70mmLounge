package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RestaurantTable
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose500
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
import com.example.util.CustomerHttpServer
import com.example.util.QrCodeGenerator
import com.example.util.TableQrCodeView
import com.example.util.TableUrlGenerator

@Composable
fun TablesScreen(
    viewModel: PosViewModel,
    onNavigateToCustomerOrder: (RestaurantTable) -> Unit,
    onNavigateToCheckout: (RestaurantTable) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tables by viewModel.allTables.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val selectedTableForQr by viewModel.selectedTableForQr.collectAsStateWithLifecycle()
    var selectedZone by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "AVAILABLE", "RUNNING"
    var showAddTableDialog by remember { mutableStateOf(false) }

    val defaultSections = listOf("Club area", "Outdoor", "Back area")
    val zones = listOf("All") + (defaultSections + tables.map { it.zone }).distinct()
    val filteredTables = tables.filter { table ->
        val matchesZone = (selectedZone == "All") || table.zone.equals(selectedZone, ignoreCase = true)
        val matchesStatus = when (selectedStatusFilter) {
            "AVAILABLE" -> table.isAvailable
            "RUNNING" -> table.isOccupied || table.currentBillAmount > 0
            else -> true
        }
        matchesZone && matchesStatus
    }

    val occupiedCount = tables.count { it.isOccupied || it.currentBillAmount > 0 }
    val availableCount = tables.count { it.isAvailable }
    val totalRunningBill = tables.sumOf { it.currentBillAmount }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header: Title on Left, Download All QRs on Right (One line, robust layout)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "Table System & QR Codes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Club area, Outdoor & Back area",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            for (t in tables) {
                                val url = TableUrlGenerator.createDynamicTableUrl(t.tableNumber, t.zone)
                                QrCodeGenerator.downloadTableQrStandee(context, t, url)
                            }
                            Toast.makeText(context, "All ${tables.size} Table QR Standees downloaded successfully!", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("download_all_qrs_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Download All QRs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Add Table Button
                    Surface(
                        onClick = { showAddTableDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Slate700, RoundedCornerShape(10.dp))
                            .testTag("add_table_top_button"),
                        color = Slate900,
                        contentColor = Emerald600
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Add Table", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // KPI Status Overview Cards with Color-Coded Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Running Bill
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Active Table Bills", style = MaterialTheme.typography.labelSmall, color = Slate500)
                        Text(
                            CurrencyFormatter.format(totalRunningBill),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                // Running Tables (Yellow / Amber)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedStatusFilter = if (selectedStatusFilter == "RUNNING") "ALL" else "RUNNING" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF2B200C) else Color(0xFFFFFBEB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Running", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$occupiedCount tables",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309)
                        )
                    }
                }

                // Available Tables (Green)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedStatusFilter = if (selectedStatusFilter == "AVAILABLE") "ALL" else "AVAILABLE" },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode) Color(0xFF0F261B) else Color(0xFFF0FDF4)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF22C55E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Available", style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$availableCount tables",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color(0xFF86EFAC) else Color(0xFF15803D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Status Legend & Quick Status Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500,
                    fontWeight = FontWeight.Bold
                )

                FilterChip(
                    selected = selectedStatusFilter == "ALL",
                    onClick = { selectedStatusFilter = "ALL" },
                    label = { Text("All (${tables.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Slate900,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                FilterChip(
                    selected = selectedStatusFilter == "AVAILABLE",
                    onClick = { selectedStatusFilter = "AVAILABLE" },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Green: Available ($availableCount)")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF16A34A),
                        selectedLabelColor = Color.White,
                        containerColor = if (isDarkMode) Color(0xFF0F261B) else Color(0xFFF0FDF4),
                        labelColor = if (isDarkMode) Color(0xFF86EFAC) else Color(0xFF15803D)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatusFilter == "AVAILABLE",
                        borderColor = Color(0xFF22C55E)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )

                FilterChip(
                    selected = selectedStatusFilter == "RUNNING",
                    onClick = { selectedStatusFilter = "RUNNING" },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Yellow: Running ($occupiedCount)")
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White,
                        containerColor = if (isDarkMode) Color(0xFF2B200C) else Color(0xFFFFFBEB),
                        labelColor = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatusFilter == "RUNNING",
                        borderColor = Color(0xFFF59E0B)
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Zone Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Area:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500,
                    fontWeight = FontWeight.Bold
                )

                for (z in zones) {
                    val isSelected = (selectedZone == z)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedZone = z },
                        label = {
                            Text(
                                text = z,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald600,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF242F31),
                            labelColor = Color(0xFFE2E8F0)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Emerald500 else Color(0xFF4B5B5E)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("zone_filter_$z")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTables, key = { it.tableId }) { table ->
                    RestaurantTableCard(
                        table = table,
                        isDarkMode = isDarkMode,
                        onViewQr = { viewModel.openTableQr(table) },
                        onOpenCustomerMenu = { onNavigateToCustomerOrder(table) },
                        onDelete = { viewModel.deleteTable(table.tableId) },
                        onSettleBill = { onNavigateToCheckout(table) },
                        onVacate = { viewModel.vacateTable(table.tableId) }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddTableDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_table_fab"),
            containerColor = Slate900,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Table")
        }
    }

    // Table QR Standee Dialog
    selectedTableForQr?.let { table ->
        TableQrStandeeDialog(
            table = table,
            onDismiss = { viewModel.closeTableQr() },
            onSimulateScan = {
                viewModel.closeTableQr()
                onNavigateToCustomerOrder(table)
            }
        )
    }

    // Add Table Dialog
    if (showAddTableDialog) {
        AddTableDialog(
            onDismiss = { showAddTableDialog = false },
            onAdd = { number, zone, capacity ->
                viewModel.addTable(number, zone, capacity)
                showAddTableDialog = false
            }
        )
    }
}

@Composable
private fun RestaurantTableCard(
    table: RestaurantTable,
    isDarkMode: Boolean,
    onViewQr: () -> Unit,
    onOpenCustomerMenu: () -> Unit,
    onDelete: () -> Unit,
    onSettleBill: () -> Unit,
    onVacate: () -> Unit
) {
    val context = LocalContext.current
    val isReq = table.isCheckoutRequested
    val isRunning = table.isOccupied || table.currentBillAmount > 0
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Visual indicators: Green for Available, Yellow for Running, Amber for Checkout Requested
    val cardBg = if (isReq) {
        if (isDarkMode) Color(0xFF332009) else Color(0xFFFEF3C7)
    } else if (isRunning) {
        if (isDarkMode) Color(0xFF2B200C) else Color(0xFFFFFBEB)
    } else {
        if (isDarkMode) Color(0xFF0F261B) else Color(0xFFF0FDF4)
    }

    val cardBorderColor = if (isReq) {
        Color(0xFFD97706)
    } else if (isRunning) {
        Color(0xFFF59E0B) // Bright Running Yellow/Amber border
    } else {
        Color(0xFF22C55E) // Crisp Available Green border
    }

    val statusPillBg = if (isReq) {
        Color(0xFFD97706)
    } else if (isRunning) {
        if (isDarkMode) Color(0xFF78350F) else Color(0xFFFEF3C7)
    } else {
        if (isDarkMode) Color(0xFF14532D) else Color(0xFFDCFCE7)
    }

    val statusPillText = if (isReq) {
        Color.White
    } else if (isRunning) {
        if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309)
    } else {
        if (isDarkMode) Color(0xFF86EFAC) else Color(0xFF15803D)
    }

    val statusLabel = if (isReq) "🔔 COMPLETE ORDER" else if (isRunning) "🟡 RUNNING" else "🟢 AVAILABLE"
    val accentBarColor = if (isReq) Color(0xFFD97706) else if (isRunning) Color(0xFFF59E0B) else Color(0xFF22C55E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isRunning) 2.dp else 1.5.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("table_card_${table.tableNumber}"),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            // Top Illuminated Visual Accent Strip (Green = Available, Yellow = Running)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(accentBarColor)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Top: Table Number with Status Dot & Visual Status Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Bolt else Icons.Default.CheckCircle,
                            contentDescription = if (isRunning) "Running Table" else "Available Table",
                            tint = if (isRunning) Color(0xFFF59E0B) else Color(0xFF22C55E),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = table.tableNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                        color = statusPillBg
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusPillText,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Zone & Capacity
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = table.zone,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${table.capacity} Seats",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = cardBorderColor.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(8.dp))

                // Guest & Bill Status (Visual differentiation)
                if (isRunning) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape,
                            color = statusPillBg
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = statusPillText,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = table.currentGuestName.ifBlank { "Guest Seated" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFF78350F),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Bill:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = CurrencyFormatter.format(table.currentBillAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD97706)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vacant & Ready for Guests",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkMode) Color(0xFF86EFAC) else Color(0xFF15803D)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons: Customer Menu, View QR Standee, Delete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Customer Menu in-app button
                    Button(
                        onClick = onOpenCustomerMenu,
                        modifier = Modifier
                            .weight(1.1f)
                            .height(34.dp)
                            .testTag("customer_menu_button_${table.tableNumber}"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Customer Menu", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Table QR Standee button
                    OutlinedButton(
                        onClick = onViewQr,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("qr_button_${table.tableNumber}"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = "QR Code", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("QR Standee", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    // Delete Table Button
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Rose500.copy(alpha = 0.12f))
                            .testTag("delete_table_${table.tableNumber}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Table",
                            tint = Rose500,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                if (isRunning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isReq) {
                        Button(
                            onClick = onSettleBill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                                .testTag("complete_order_button_${table.tableNumber}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Order 🔔", fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onVacate,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                            ) {
                                Text("Vacate", fontSize = 11.sp, color = if (isDarkMode) Color(0xFFFDE68A) else Color(0xFFB45309))
                            }

                            Button(
                                onClick = onSettleBill,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD97706),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Settle Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Table ${table.tableNumber}?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Kya aap Table ${table.tableNumber} (${table.zone}) ko delete karna chahte hain?" +
                            if (isRunning) "\n\n⚠️ Is table par abhi active order chalu hai!" else "",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Rose500)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TableQrStandeeDialog(
    table: RestaurantTable,
    onDismiss: () -> Unit,
    onSimulateScan: () -> Unit
) {
    val context = LocalContext.current
    var urlMode by remember { mutableStateOf(0) } // 0: Vercel Web Domain, 1: Local WiFi IP, 2: App Deep-Link

    val localIp = remember { CustomerHttpServer.getLocalIpAddress() }
    val localPort = remember { CustomerHttpServer.getPort() }

    val dynamicWebUrl = remember(table.tableNumber, table.zone) {
        TableUrlGenerator.createDynamicTableUrl(table.tableNumber, table.zone)
    }
    val dynamicLocalUrl = remember(table.tableNumber, table.zone, localIp, localPort) {
        "http://$localIp:$localPort/order?table=${table.tableNumber.trim()}&zone=${table.zone.trim()}"
    }
    val deepLinkUrl = remember(table.tableNumber, table.zone) {
        TableUrlGenerator.createTableDeepLink(table.tableNumber, table.zone)
    }

    val activeUrl = when (urlMode) {
        0 -> dynamicWebUrl
        1 -> dynamicLocalUrl
        else -> deepLinkUrl
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Table QR Standee & URL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unique Customer Self-Ordering Link",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // URL Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = listOf("Vercel Web", "Local WiFi", "App Link")
                    modes.forEachIndexed { idx, label ->
                        val isSelected = (urlMode == idx)
                        FilterChip(
                            selected = isSelected,
                            onClick = { urlMode = idx },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Printable Standee Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Slate200, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "70MM LOUNGE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Slate900
                        )
                        Text(
                            text = "Restaurant • Bar • Club Lounge",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate500,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Bokaro • Ph: 8987477773",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Slate400
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Table Number Badge
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                            color = Slate900
                        ) {
                            Text(
                                text = "TABLE ${table.tableNumber} • ${table.zone.uppercase()}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Procedural QR Code Canvas
                        TableQrCodeView(
                            data = activeUrl,
                            sizeDp = 180.dp,
                            foregroundColor = Slate900
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SCAN TO ORDER FOOD & DRINKS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Slate900
                        )
                        Text(
                            text = "Instant service directly to your table",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Unique URL Box with Copy Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Slate200, RoundedCornerShape(10.dp)),
                    color = Slate100
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dynamic Self-Order URL:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate500,
                                fontSize = 10.sp
                            )
                            Text(
                                text = activeUrl,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Table URL", activeUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Table ${table.tableNumber} URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", tint = Slate700, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Download Standee Image, Simulate customer scan (opens self-ordering!), Share QR Standee
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            QrCodeGenerator.downloadTableQrStandee(context, table, activeUrl)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_qr_standee_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download QR Standee Image (PNG)", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onSimulateScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_customer_scan_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open In-App Customer Menu")
                    }

                    Button(
                        onClick = {
                            try {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(activeUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(browserIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_menu_in_browser_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Sky600),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Web Menu in Browser (Chrome)", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "70mm Lounge - Table ${table.tableNumber} QR Code")
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "70MM LOUNGE - TABLE ${table.tableNumber} (${table.zone})\n" +
                                            "Bokaro • Phone: 8987477773 • Email: 70mmlounge8bokaro@gmail.com\n\n" +
                                            "Customer Self-Order Unique Link:\n$activeUrl\n\n" +
                                            "Scan this QR code from the table standee to browse the menu and send orders directly to the Kitchen and Admin Panel!"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Table QR Standee"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Table Standee Details")
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTableDialog(
    onDismiss: () -> Unit,
    onAdd: (number: String, zone: String, capacity: Int) -> Unit
) {
    var tableNumber by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("Club area") }
    var capacityInput by remember { mutableStateOf("4") }

    val zonePresets = listOf("Club area", "Outdoor", "Back area")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add New Table",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = tableNumber,
                    onValueChange = { tableNumber = it },
                    label = { Text("Table Number * (e.g. T-5, VIP-4)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_table_number_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Zone / Area", style = MaterialTheme.typography.labelSmall, color = Slate600)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (z in zonePresets) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { zone = z },
                            color = if (zone == z) Slate900 else Slate100
                        ) {
                            Text(
                                text = z,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (zone == z) Color.White else Slate700,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = capacityInput,
                    onValueChange = { capacityInput = it },
                    label = { Text("Seating Capacity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val cap = capacityInput.toIntOrNull() ?: 4
                            if (tableNumber.isNotBlank()) {
                                onAdd(tableNumber, zone, cap)
                            }
                        },
                        enabled = tableNumber.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                    ) {
                        Text("Add Table")
                    }
                }
            }
        }
    }
}
