package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Bitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Sky600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.CurrencyFormatter
import com.example.util.QrCodeGenerator

@Composable
fun CloudSyncDialog(
    todaySales: Double,
    activeRunningTablesCount: Int,
    lowStockCount: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val monitorUrl = "https://ntfy.sh/lounge70mm_bokaro_orders"
    val qrBitmap: Bitmap? = remember(monitorUrl) {
        QrCodeGenerator.generateQrBitmap(monitorUrl, 300)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Sky600.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = Sky600)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Cloud Real-Time Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Emerald600)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Multi-Terminal Active • Live",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Emerald600,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Travel anywhere in the world and view your lounge's live sales, running tables, and inventory directly from your mobile phone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Stats Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Today Sales
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Emerald600.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Emerald600.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = Emerald600, modifier = Modifier.size(14.dp))
                                Text("Live Sales", style = MaterialTheme.typography.labelSmall, color = Emerald600, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = CurrencyFormatter.format(todaySales),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                        }
                    }

                    // Running Tables
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Sky600.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Sky600.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TableBar, contentDescription = null, tint = Sky600, modifier = Modifier.size(14.dp))
                                Text("Running", style = MaterialTheme.typography.labelSmall, color = Sky600, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$activeRunningTablesCount Tables",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                        }
                    }

                    // Low Stock
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = if (lowStockCount > 0) Color(0xFFEF4444).copy(alpha = 0.08f) else Color(0xFFF3F4F6)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (lowStockCount > 0) Color(0xFFEF4444).copy(alpha = 0.3f) else Slate200)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = if (lowStockCount > 0) Color(0xFFEF4444) else Slate500, modifier = Modifier.size(14.dp))
                                Text("Low Stock", style = MaterialTheme.typography.labelSmall, color = if (lowStockCount > 0) Color(0xFFEF4444) else Slate500, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$lowStockCount Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mobile Remote Live Monitor Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 Scan with Mobile to Open Live Feed",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Live Remote Monitor QR",
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Topic: lounge70mm_bokaro_orders",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Slate500
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        putExtra(Intent.EXTRA_TEXT, "70MM Lounge Live Remote Feed: $monitorUrl")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Live Lounge Link"))
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Link", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(monitorUrl))
                                        context.startActivity(browserIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Sky600),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Feed", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Encrypted Real-Time Relay • Zero Port-Forwarding Required",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate500,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
