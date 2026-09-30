package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PrinterConfig
import com.example.data.model.PrinterConnectionType
import com.example.data.model.ThermalPaperWidth
import com.example.ui.theme.Emerald100
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
import com.example.util.PrinterDiscoveryManager
import com.example.util.PrinterManager
import com.example.util.PrinterPreferences
import kotlinx.coroutines.launch

@Composable
fun PrinterSettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var config by remember { mutableStateOf(PrinterPreferences.loadConfig(context)) }
    var isTesting by remember { mutableStateOf(false) }
    var testResultMsg by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("printer_settings_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate900),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Printer Settings",
                                tint = Emerald600,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Printer Configuration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Thermal, Bluetooth, Wi-Fi, USB & LAN",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Connection Mode Selector
                Text(
                    text = "Select Printer Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrinterConnectionType.values().forEach { type ->
                        val isSelected = config.connectionType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                config = config.copy(connectionType = type)
                            },
                            label = {
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                val icon = when (type) {
                                    PrinterConnectionType.AUTO_DETECT -> Icons.Default.CheckCircle
                                    PrinterConnectionType.USB -> Icons.Default.Usb
                                    PrinterConnectionType.BLUETOOTH -> Icons.Default.Bluetooth
                                    PrinterConnectionType.WIFI -> Icons.Default.Wifi
                                    PrinterConnectionType.LAN -> Icons.Default.Lan
                                    PrinterConnectionType.SYSTEM_PRINT -> Icons.Default.Print
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Slate900,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Emerald600
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Printer Details Card based on selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate100),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        when (config.connectionType) {
                            PrinterConnectionType.AUTO_DETECT -> {
                                Text(
                                    text = "Smart Auto-Detect (Rugtek / USB / Bluetooth)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Recommended for Rugtek POS terminal. The app automatically senses any connected USB thermal printer, internal POS device node (/dev/usb/lp0), or paired Bluetooth printer. 1-Click direct print without extra dialogs!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                var hardwareSummary by remember { mutableStateOf(PrinterManager.getDetectedPrinterSummary(context)) }
                                Surface(
                                    color = Emerald100,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Emerald600, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("Detected Hardware Status:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                                                Text(hardwareSummary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                PrinterDiscoveryManager.scanAndStorePrinters(context)
                                                hardwareSummary = PrinterManager.getDetectedPrinterSummary(context)
                                                config = PrinterPreferences.loadConfig(context)
                                                Toast.makeText(context, "Hardware scan complete: $hardwareSummary", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Scan", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Scan Now", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                            PrinterConnectionType.USB -> {
                                Text(
                                    text = "USB Direct POS Printer (Rugtek / Thermal)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = config.usbDeviceName,
                                    onValueChange = { config = config.copy(usbDeviceName = it) },
                                    label = { Text("USB Printer Device Name / Filter") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                val usbList = remember { PrinterManager.getConnectedUsbPrinters(context) }
                                if (usbList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Connected USB Devices (Tap to select):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald600
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        usbList.forEach { info ->
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(1.dp, Emerald600, RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        config = config.copy(usbDeviceName = info.name)
                                                        if (!info.hasPermission) {
                                                            val usbMgr = context.getSystemService(android.content.Context.USB_SERVICE) as? android.hardware.usb.UsbManager
                                                            usbMgr?.let { PrinterManager.requestUsbPermission(context, it, info.device) }
                                                        }
                                                    },
                                                color = Emerald100
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Usb, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald600)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(info.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                    }
                                                    Text(
                                                        if (info.hasPermission) "Authorized ✓" else "Request Permission",
                                                        fontSize = 11.sp,
                                                        color = if (info.hasPermission) Emerald600 else Slate600
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Connects directly via USB Host API and Linux hardware nodes (/dev/usb/lp0).",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                            PrinterConnectionType.BLUETOOTH -> {
                                Text(
                                    text = "Bluetooth POS Printer",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = config.bluetoothDeviceName,
                                    onValueChange = { config = config.copy(bluetoothDeviceName = it) },
                                    label = { Text("Paired Bluetooth Printer Name / MAC") },
                                    placeholder = { Text("POS-Printer / MPT-II / RPP02N") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                val pairedList = remember { PrinterManager.getPairedBluetoothPrinters() }
                                if (pairedList.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Detected Paired Devices (Tap to select):",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald600
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        pairedList.forEach { (devName, devMac) ->
                                            val isChosen = config.bluetoothDeviceAddress == devMac || config.bluetoothDeviceName == devName
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .border(
                                                        1.dp,
                                                        if (isChosen) Emerald600 else Slate400,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable {
                                                        config = config.copy(
                                                            bluetoothDeviceName = devName,
                                                            bluetoothDeviceAddress = devMac
                                                        )
                                                    },
                                                color = if (isChosen) Emerald100 else Slate200
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(12.dp), tint = Emerald600)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(devName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Pair your Bluetooth thermal printer in Android Bluetooth Settings first, then select or enter its name here.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                            PrinterConnectionType.WIFI, PrinterConnectionType.LAN -> {
                                Text(
                                    text = if (config.connectionType == PrinterConnectionType.WIFI) "Wi-Fi Network Printer IP" else "LAN Ethernet Printer IP",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = config.ipAddress,
                                        onValueChange = { config = config.copy(ipAddress = it) },
                                        label = { Text("IP Address") },
                                        placeholder = { Text("192.168.1.100") },
                                        singleLine = true,
                                        modifier = Modifier.weight(2f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = config.port.toString(),
                                        onValueChange = {
                                            val p = it.toIntOrNull() ?: 9100
                                            config = config.copy(port = p)
                                        },
                                        label = { Text("Port") },
                                        placeholder = { Text("9100") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Standard RAW port is 9100 for Epson, Star, TVS, NGX, Posiflex printers.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500,
                                    fontSize = 11.sp
                                )
                            }
                            PrinterConnectionType.SYSTEM_PRINT -> {
                                Text(
                                    text = "Android System Print Dialog",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate800
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Opens the standard Android print preview window. Useful if you want to Save as PDF or use Mopria/Google Cloud Print services.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate600,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Paper Width Selection
                Text(
                    text = "Paper Roll Width",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThermalPaperWidth.values().forEach { paper ->
                        val isSelected = config.paperWidth == paper
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) Slate900 else Slate200,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { config = config.copy(paperWidth = paper) },
                            color = if (isSelected) Slate900 else MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = paper.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else Slate900
                                )
                                Text(
                                    text = "${paper.lineChars} chars/line",
                                    fontSize = 11.sp,
                                    color = if (isSelected) Slate400 else Slate500
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Automation Toggles
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Print Bill on Checkout", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Automatically send invoice to printer after payment", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = config.autoPrintBillOnCheckout,
                                onCheckedChange = { config = config.copy(autoPrintBillOnCheckout = it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Emerald600, checkedTrackColor = Emerald100)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-Print KOT to Kitchen", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Send tickets instantly to kitchen or bar printer on order submit", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = config.autoPrintKotOnOrder,
                                onCheckedChange = { config = config.copy(autoPrintKotOnOrder = it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Sky600, checkedTrackColor = Sky100)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Slate200)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Thermal Auto-Cut Paper (ऑटो-कट)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Send ESC/POS cut paper command (GS V 1) after printing bill or KOT", fontSize = 11.sp, color = Slate500)
                            }
                            Switch(
                                checked = config.autoCutPaper,
                                onCheckedChange = { config = config.copy(autoCutPaper = it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Emerald600, checkedTrackColor = Emerald100)
                            )
                        }
                    }
                }

                if (testResultMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = testResultMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = Emerald600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Test Print, Save Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isTesting = true
                            scope.launch {
                                PrinterManager.printTestSlip(context, config) { success, msg ->
                                    isTesting = false
                                    testResultMsg = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isTesting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTesting) "Testing..." else "Test Print")
                    }

                    Button(
                        onClick = {
                            PrinterPreferences.saveConfig(context, config)
                            PrinterManager.currentConfig = config
                            Toast.makeText(context, "Printer settings saved!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald600)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Apply")
                    }
                }
            }
        }
    }
}
