package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import com.example.data.model.KitchenOrderTicket
import com.example.util.KotVoiceAnnouncer
import com.example.util.PrinterManager
import kotlinx.coroutines.launch
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber500
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose100
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
import kotlinx.coroutines.delay

@Composable
fun KitchenScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeKots by viewModel.activeKots.collectAsStateWithLifecycle()
    val allKots by viewModel.allKots.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isKitchenAudioEnabled.collectAsStateWithLifecycle()
    val isKotVoiceEnabled by viewModel.isKotVoiceEnabled.collectAsStateWithLifecycle()

    // Initialize Hindi Voice Announcer and bind to lifecycle
    val voiceAnnouncer = remember(context) { KotVoiceAnnouncer(context) }
    DisposableEffect(voiceAnnouncer) {
        onDispose {
            voiceAnnouncer.shutdown()
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Active, 1: Ready, 2: History
    var sectionFilter by remember { mutableStateOf("All") } // "All", "KITCHEN", "BAR", "GRILL"
    var showConsolidatedQueue by remember { mutableStateOf(true) }

    // Live clock ticker that auto-refreshes elapsed time every 5 seconds
    var currentClockTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            currentClockTime = System.currentTimeMillis()
        }
    }

    // Real-time Event Listener for instant Hindi Voice Table Alerts
    LaunchedEffect(Unit) {
        viewModel.kotAlertFlow.collect { alertEvent ->
            if (isKotVoiceEnabled) {
                voiceAnnouncer.announceKot(
                    areaName = alertEvent.areaName,
                    tableNumber = alertEvent.tableNumber,
                    isAddon = alertEvent.isAddon
                )
            } else if (isAudioEnabled) {
                voiceAnnouncer.playKitchenChime()
            }
        }
    }

    // Audio chime / voice on new incoming tickets fallback (from database sync)
    var previousKotCount by remember { mutableStateOf(activeKots.size) }
    LaunchedEffect(activeKots.size) {
        if (activeKots.size > previousKotCount && previousKotCount > 0) {
            val newest = activeKots.maxByOrNull { it.timestamp }
            if (newest != null) {
                if (isKotVoiceEnabled) {
                    voiceAnnouncer.announceKot(newest.zone, newest.tableNumber, isAddon = false)
                } else if (isAudioEnabled) {
                    voiceAnnouncer.playKitchenChime()
                }
            }
        }
        previousKotCount = activeKots.size
    }

    val displayedKots = when (selectedTab) {
        0 -> activeKots.filter { it.status == "NEW" || it.status == "PREPARING" }
        1 -> activeKots.filter { it.status == "READY" }
        else -> allKots.filter { it.status == "SERVED" }
    }.filter {
        sectionFilter == "All" || it.section.equals(sectionFilter, ignoreCase = true)
    }

    val newOrdersCount = activeKots.count { it.status == "NEW" }

    // Calculate all pending items across all active tickets for the kitchen prep summary
    val consolidatedPendingItems = remember(activeKots) {
        val counts = mutableMapOf<String, Int>()
        activeKots.filter { it.status == "NEW" || it.status == "PREPARING" }.forEach { kot ->
            val completed = kot.completedItems.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
            val items = kot.itemsSummary.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
            for (item in items) {
                if (!completed.contains(item)) {
                    counts[item] = (counts[item] ?: 0) + 1
                }
            }
        }
        counts.toList().sortedByDescending { it.second }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kitchen & Bar KOT Display",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "70mm Lounge • Live Kitchen Tickets",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Hindi Voice Announcement Toggle Chip
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleKotVoice() }
                        .border(
                            1.dp,
                            if (isKotVoiceEnabled) Emerald600 else Slate400,
                            RoundedCornerShape(8.dp)
                        ),
                    color = if (isKotVoiceEnabled) Emerald600.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isKotVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Hindi Voice Alert",
                            tint = if (isKotVoiceEnabled) Emerald600 else Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isKotVoiceEnabled) "Hindi Voice: ON" else "Voice: OFF",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isKotVoiceEnabled) Emerald600 else Slate500,
                            fontSize = 11.sp
                        )
                    }
                }

                // Test Voice Button
                OutlinedButton(
                    onClick = {
                        voiceAnnouncer.announceKot(
                            areaName = "Main Dining",
                            tableNumber = "5",
                            isAddon = false
                        )
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Test Voice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    color = if (newOrdersCount > 0) Amber500 else Slate900
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SoupKitchen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${activeKots.size} In Prep",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Real-Time Incoming Order Live Banner (Highlights immediately when table places order)
        AnimatedVisibility(
            visible = newOrdersCount > 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Amber500, RoundedCornerShape(10.dp)),
                color = Amber100
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "New Order Alert",
                        tint = Amber500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE ORDER ALERT: $newOrdersCount new table order(s) waiting!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Pending items highlighted in tickets below for immediate cooking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Consolidated Kitchen Prep Queue (Aggregated pending items across all tables)
        if (consolidatedPendingItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConsolidatedQueue = !showConsolidatedQueue },
                colors = CardDefaults.cardColors(containerColor = Slate100),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = Emerald600,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pending Dishes Queue (${consolidatedPendingItems.sumOf { it.second }} items)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate800
                            )
                        }
                        Text(
                            text = if (showConsolidatedQueue) "Hide" else "Show Batch View",
                            style = MaterialTheme.typography.labelSmall,
                            color = Sky600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (showConsolidatedQueue) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for ((itemName, count) in consolidatedPendingItems.take(8)) {
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                                    color = Color.White
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${count}x",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Amber500
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = itemName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate800,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Status Tabs (Active, Ready, Served)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Slate100,
            contentColor = Slate900,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Slate900,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            val tabs = listOf("Active Tickets", "Ready to Serve", "Served History")
            tabs.forEachIndexed { index, title ->
                val isSelected = (selectedTab == index)
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Emerald600 else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("kot_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section Filters (All, Kitchen, Bar, Grill)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf("All", "KITCHEN", "BAR", "GRILL")
            for (sec in sections) {
                val isSelected = (sectionFilter == sec)
                FilterChip(
                    selected = isSelected,
                    onClick = { sectionFilter = sec },
                    label = { Text(sec) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald600,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Emerald600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // KOT Tickets List
        if (displayedKots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedTab == 0) "All kitchen tickets completed!" else "No tickets in this section",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate600
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(displayedKots, key = { it.kotId }) { kot ->
                    KitchenTicketCard(
                        kot = kot,
                        currentClockTime = currentClockTime,
                        onToggleItemCompleted = { itemStr ->
                            viewModel.toggleKotItemCompleted(kot, itemStr)
                        },
                        onMarkAllItemsReady = {
                            viewModel.markAllKotItemsReady(kot)
                        },
                        onUpdateStatus = { newStatus ->
                            viewModel.updateKotStatus(kot.kotId, newStatus)
                        },
                        onPrintSlip = {
                            scope.launch {
                                PrinterManager.printKot(context, kot)
                            }
                        },
                        onAdjustItemQuantity = { itemStr, delta ->
                            viewModel.updateKotItemQuantity(kot, itemStr, delta)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun KitchenTicketCard(
    kot: KitchenOrderTicket,
    currentClockTime: Long,
    onToggleItemCompleted: (String) -> Unit,
    onMarkAllItemsReady: () -> Unit,
    onUpdateStatus: (String) -> Unit,
    onPrintSlip: () -> Unit,
    onAdjustItemQuantity: (String, Int) -> Unit = { _, _ -> }
) {
    val statusColor = when (kot.status) {
        "NEW" -> Amber500
        "PREPARING" -> Sky600
        "READY" -> Emerald600
        else -> Slate500
    }

    val statusBg = when (kot.status) {
        "NEW" -> Amber100
        "PREPARING" -> Sky100
        "READY" -> Emerald50
        else -> Slate100
    }

    val elapsedSeconds = ((currentClockTime - kot.timestamp) / 1000).coerceAtLeast(0)
    val elapsedMinutes = elapsedSeconds / 60
    val secondsRem = elapsedSeconds % 60

    val timeElapsedText = when {
        elapsedMinutes == 0L -> "${secondsRem}s ago"
        else -> "${elapsedMinutes}m ${secondsRem}s ago"
    }

    val isOverdue = elapsedMinutes >= 15

    // Item-level completion parsing
    val allItems = remember(kot.itemsSummary) {
        kot.itemsSummary.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
    }
    val completedItemsList = remember(kot.completedItems) {
        kot.completedItems.split(" ; ").map { it.trim() }.filter { it.isNotBlank() }
    }

    val pendingCount = allItems.count { !completedItemsList.contains(it) }
    val progress = if (allItems.isNotEmpty()) {
        (allItems.size - pendingCount).toFloat() / allItems.size.toFloat()
    } else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isOverdue && kot.status != "READY") Rose500
                else if (kot.status == "NEW") Amber500
                else if (kot.status == "READY") Emerald600
                else Slate200,
                RoundedCornerShape(14.dp)
            )
            .testTag("kot_card_${kot.kotNumber}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: KOT Number, Section Tag & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = kot.kotNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                        color = if (kot.section == "BAR") Sky100 else Slate900
                    ) {
                        Text(
                            text = kot.section,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (kot.section == "BAR") Sky600 else Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isOverdue && kot.status != "READY") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                            color = Rose100
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Rush", tint = Rose500, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("RUSH", style = MaterialTheme.typography.labelSmall, color = Rose500, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                    color = statusBg
                ) {
                    Text(
                        text = kot.status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Table, Guest, and Timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TABLE ${kot.tableNumber} • ${kot.zone} (${kot.customerName})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = if (isOverdue) Rose500 else if (elapsedMinutes >= 8) Amber500 else Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeElapsedText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal,
                        color = if (isOverdue) Rose500 else if (elapsedMinutes >= 8) Amber500 else Slate500,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preparation Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (pendingCount == 0) "All items completed!" else "$pendingCount pending item(s) to prepare",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingCount == 0) Emerald600 else Amber500
                    )
                    Text(
                        text = "${allItems.size - pendingCount}/${allItems.size} ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (pendingCount == 0) Emerald600 else Sky600,
                    trackColor = Slate100
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate200)
            Spacer(modifier = Modifier.height(10.dp))

            // HIGHLIGHTED ITEMS LIST (Kitchen Staff Interactive Item-by-Item Tick-Off)
            Text(
                text = "KITCHEN ORDER ITEMS (Tap item when prepared):",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (itemStr in allItems) {
                    val isCompleted = completedItemsList.contains(itemStr)
                    val isPending = !isCompleted

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                if (isPending) Amber500 else Slate200,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleItemCompleted(itemStr) },
                        color = if (isPending) Amber100.copy(alpha = 0.4f) else Slate100.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Status Icon indicator
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) Emerald600 else Amber500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCompleted) Icons.Default.Check else Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = itemStr,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isPending) FontWeight.Bold else FontWeight.Normal,
                                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                        color = if (isCompleted) Slate400 else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isPending) {
                                        Text(
                                            text = "Tap to mark prepared",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Amber500,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Item quantity adjust buttons: - and +
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .padding(horizontal = 2.dp, vertical = 2.dp)
                                ) {
                                    Surface(
                                        onClick = { onAdjustItemQuantity(itemStr, -1) },
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.Transparent,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = Slate700,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Surface(
                                        onClick = { onAdjustItemQuantity(itemStr, 1) },
                                        shape = RoundedCornerShape(4.dp),
                                        color = Emerald600.copy(alpha = 0.2f),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = Emerald600,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                // Prominent PENDING vs READY badge
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
                                    color = if (isPending) Amber500 else Emerald50
                                ) {
                                    Text(
                                        text = if (isPending) "PENDING" else "DONE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = if (isPending) Color.White else Emerald600,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Special Instructions Note (Green color for Chef Notes)
            if (kot.specialNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Emerald600.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    color = Emerald600.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Chef Notes: ${kot.specialNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Workflows: NEW -> PREPARING -> READY -> SERVED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPrintSlip,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print KOT", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Slip", fontSize = 12.sp)
                }

                when (kot.status) {
                    "NEW" -> {
                        Button(
                            onClick = { onUpdateStatus("PREPARING") },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("start_cooking_${kot.kotNumber}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Sky600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Prep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onMarkAllItemsReady,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("mark_ready_${kot.kotNumber}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "PREPARING" -> {
                        Button(
                            onClick = onMarkAllItemsReady,
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("mark_ready_${kot.kotNumber}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All Ready to Serve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "READY" -> {
                        Button(
                            onClick = { onUpdateStatus("SERVED") },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("mark_served_${kot.kotNumber}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send to Table 🍽️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    else -> {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Slate100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("Completed & Served", style = MaterialTheme.typography.bodySmall, color = Slate500)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareKotSlip(context: Context, kot: KitchenOrderTicket) {
    val itemsFormatted = kot.itemsSummary.split(" ; ")
        .filter { it.isNotBlank() }
        .joinToString("\n") { " [  ]  $it" }

    val slipContent = """
        ========================================
                     70MM LOUNGE
               KITCHEN ORDER TICKET (KOT)
        ========================================
        KOT NO    : ${kot.kotNumber}
        SECTION   : ${kot.section}
        TABLE     : ${kot.tableNumber} (${kot.zone})
        GUEST     : ${kot.customerName}
        STATUS    : ${kot.status}
        TIME      : ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", java.util.Locale.getDefault()).format(java.util.Date(kot.timestamp))}
        ========================================
        ITEMS TO PREPARE:
        $itemsFormatted
        ========================================
        ${if (kot.specialNotes.isNotBlank()) "CHEF NOTES: ${kot.specialNotes}\n========================================" else ""}
        70mm Lounge • Phone: 8987477773
        Email: 70mmlounge8bokaro@gmail.com
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "70mm Lounge ${kot.kotNumber} Slip - Table ${kot.tableNumber}")
        putExtra(Intent.EXTRA_TEXT, slipContent)
    }
    context.startActivity(Intent.createChooser(intent, "Print / Share KOT Slip"))
}
