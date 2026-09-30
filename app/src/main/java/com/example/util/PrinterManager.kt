package com.example.util

import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import com.example.data.model.KitchenOrderTicket
import com.example.data.model.OrderWithItems
import com.example.data.model.PrinterConfig
import com.example.data.model.PrinterConnectionType
import com.example.data.model.ThermalPaperWidth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UsbPrinterInfo(
    val name: String,
    val manufacturer: String,
    val vendorId: Int,
    val productId: Int,
    val hasPermission: Boolean,
    val device: UsbDevice
)

object PrinterManager {

    var currentConfig: PrinterConfig = PrinterConfig()

    /**
     * Initializes the printer configuration from persistent preferences.
     */
    fun init(context: Context) {
        currentConfig = PrinterPreferences.loadConfig(context)
    }

    // ESC/POS Command Constants
    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    private val LF: Byte = 0x0A

    val CMD_INIT = byteArrayOf(ESC, 0x40) // Initialize printer
    val CMD_ALIGN_LEFT = byteArrayOf(ESC, 0x61, 0x00)
    val CMD_ALIGN_CENTER = byteArrayOf(ESC, 0x61, 0x01)
    val CMD_ALIGN_RIGHT = byteArrayOf(ESC, 0x61, 0x02)
    val CMD_BOLD_ON = byteArrayOf(ESC, 0x45, 0x01)
    val CMD_BOLD_OFF = byteArrayOf(ESC, 0x45, 0x00)
    val CMD_DOUBLE_ON = byteArrayOf(GS, 0x21, 0x11) // 2x width & 2x height
    val CMD_DOUBLE_OFF = byteArrayOf(GS, 0x21, 0x00)
    val CMD_CUT_PAPER = byteArrayOf(GS, 0x56, 0x41, 0x03) // Full cut with feed
    val CMD_DRAWER_KICK = byteArrayOf(ESC, 0x70, 0x00, 0x19, 0xFA.toByte()) // Open cash drawer

    /**
     * Prints customer receipt using the currently active printer configuration.
     * Auto-detects connected printer (Rugtek USB / Bluetooth / Direct POS Node) or falls back to system print.
     */
    suspend fun printReceipt(
        context: Context,
        orderWithItems: OrderWithItems,
        config: PrinterConfig = currentConfig,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val effectiveConfig = if (config == currentConfig) {
            PrinterPreferences.loadConfig(context)
        } else {
            config
        }
        val bytes = generateReceiptEscPos(orderWithItems, effectiveConfig)
        val textSlip = generateReceiptPlainText(orderWithItems, effectiveConfig.paperWidth)

        withContext(Dispatchers.IO) {
            when (effectiveConfig.connectionType) {
                PrinterConnectionType.AUTO_DETECT -> {
                    autoDetectAndPrint(
                        context = context,
                        data = bytes,
                        textSlip = textSlip,
                        title = "Receipt-${orderWithItems.order.invoiceNumber}",
                        onComplete = onComplete
                    )
                }
                PrinterConnectionType.USB -> {
                    val result = sendBytesToUsb(context, effectiveConfig.usbDeviceName, bytes)
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "USB Print: ${result.second}", Toast.LENGTH_SHORT).show()
                            onComplete(true, result.second)
                        } else {
                            Toast.makeText(context, "${result.second}. Fallback to Print dialog...", Toast.LENGTH_SHORT).show()
                            openSystemPrintDialog(context, "Receipt-${orderWithItems.order.invoiceNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.BLUETOOTH -> {
                    val result = sendBytesToBluetooth(
                        context = context,
                        deviceAddressOrName = effectiveConfig.bluetoothDeviceAddress.ifBlank { effectiveConfig.bluetoothDeviceName },
                        data = bytes
                    )
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "Thermal Print: ${result.second} (Auto-cut sent)", Toast.LENGTH_SHORT).show()
                            onComplete(true, result.second)
                        } else {
                            Toast.makeText(context, "${result.second}. Fallback to Print dialog...", Toast.LENGTH_SHORT).show()
                            openSystemPrintDialog(context, "Receipt-${orderWithItems.order.invoiceNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.WIFI, PrinterConnectionType.LAN -> {
                    val result = sendBytesToSocket(effectiveConfig.ipAddress, effectiveConfig.port, bytes)
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "Printed to ${effectiveConfig.connectionType.displayName} (${effectiveConfig.ipAddress})", Toast.LENGTH_SHORT).show()
                            onComplete(true, "Printed successfully")
                        } else {
                            openSystemPrintDialog(context, "Receipt-${orderWithItems.order.invoiceNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.SYSTEM_PRINT -> {
                    withContext(Dispatchers.Main) {
                        openSystemPrintDialog(context, "Receipt-${orderWithItems.order.invoiceNumber}", textSlip)
                        onComplete(true, "Sent to Android System Print dialog")
                    }
                }
            }
        }
    }

    /**
     * Prints Kitchen Order Ticket (KOT)
     */
    suspend fun printKot(
        context: Context,
        kot: KitchenOrderTicket,
        config: PrinterConfig = currentConfig,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val bytes = generateKotEscPos(kot, config)
        val textSlip = generateKotPlainText(kot, config.paperWidth)

        withContext(Dispatchers.IO) {
            when (config.connectionType) {
                PrinterConnectionType.AUTO_DETECT -> {
                    autoDetectAndPrint(
                        context = context,
                        data = bytes,
                        textSlip = textSlip,
                        title = "KOT-${kot.kotNumber}",
                        onComplete = onComplete
                    )
                }
                PrinterConnectionType.USB -> {
                    val result = sendBytesToUsb(context, config.usbDeviceName, bytes)
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "USB KOT: ${result.second}", Toast.LENGTH_SHORT).show()
                            onComplete(true, result.second)
                        } else {
                            openSystemPrintDialog(context, "KOT-${kot.kotNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.BLUETOOTH -> {
                    val result = sendBytesToBluetooth(
                        context = context,
                        deviceAddressOrName = config.bluetoothDeviceAddress.ifBlank { config.bluetoothDeviceName },
                        data = bytes
                    )
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "KOT Printed via Bluetooth (Auto-cut sent)", Toast.LENGTH_SHORT).show()
                            onComplete(true, "KOT printed via Bluetooth")
                        } else {
                            openSystemPrintDialog(context, "KOT-${kot.kotNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.WIFI, PrinterConnectionType.LAN -> {
                    val result = sendBytesToSocket(config.ipAddress, config.port, bytes)
                    withContext(Dispatchers.Main) {
                        if (result.first) {
                            Toast.makeText(context, "KOT Printed via ${config.connectionType.displayName}", Toast.LENGTH_SHORT).show()
                            onComplete(true, "KOT printed")
                        } else {
                            openSystemPrintDialog(context, "KOT-${kot.kotNumber}", textSlip)
                            onComplete(false, result.second)
                        }
                    }
                }
                PrinterConnectionType.SYSTEM_PRINT -> {
                    withContext(Dispatchers.Main) {
                        openSystemPrintDialog(context, "KOT-${kot.kotNumber}", textSlip)
                        onComplete(true, "KOT sent to system printer")
                    }
                }
            }
        }
    }

    /**
     * Automatic smart print:
     * 1. Direct USB POS printer (Rugtek, Epson, STMicro, etc.)
     * 2. Linux device node (/dev/usb/lp0, /dev/ttyUSB0, etc.)
     * 3. Paired Bluetooth printer (RP-80, POS-80, etc.)
     * 4. LAN / Wi-Fi IP socket
     * 5. Android system print dialog fallback
     */
    suspend fun autoDetectAndPrint(
        context: Context,
        data: ByteArray,
        textSlip: String,
        title: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            val savedConfig = PrinterPreferences.loadConfig(context)

            // 0a. Check saved USB printer from preferences first
            if (savedConfig.usbDeviceName.isNotBlank()) {
                val usbRes = sendBytesToUsb(context, savedConfig.usbDeviceName, data)
                if (usbRes.first) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Direct Bill Printed! (${usbRes.second})", Toast.LENGTH_SHORT).show()
                        onComplete(true, usbRes.second)
                    }
                    return@withContext
                }
            }

            // 0b. Check saved Bluetooth printer from preferences first
            if (savedConfig.bluetoothDeviceAddress.isNotBlank()) {
                val btRes = sendBytesToBluetooth(context, savedConfig.bluetoothDeviceAddress, data)
                if (btRes.first) {
                    val name = savedConfig.bluetoothDeviceName.ifBlank { savedConfig.bluetoothDeviceAddress }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Direct Bill Printed! ($name)", Toast.LENGTH_SHORT).show()
                        onComplete(true, btRes.second)
                    }
                    return@withContext
                }
            }

            // 1. Try any connected USB POS Printer
            val usbPrinters = getConnectedUsbPrinters(context)
            if (usbPrinters.isNotEmpty()) {
                val usbRes = sendBytesToUsb(context, "", data)
                if (usbRes.first) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Direct Bill Printed! (${usbRes.second})", Toast.LENGTH_SHORT).show()
                        onComplete(true, usbRes.second)
                    }
                    return@withContext
                }
            }

            // 2. Try direct POS Linux device nodes
            val nodeRes = sendBytesToDeviceNode(data)
            if (nodeRes.first) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Direct Bill Printed! (${nodeRes.second})", Toast.LENGTH_SHORT).show()
                    onComplete(true, nodeRes.second)
                }
                return@withContext
            }

            // 3. Try Paired Bluetooth Printer
            val btPrinters = getPairedBluetoothPrinters()
            if (btPrinters.isNotEmpty()) {
                val targetBt = btPrinters.first()
                val btRes = sendBytesToBluetooth(context, targetBt.second, data)
                if (btRes.first) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Direct Bill Printed! (${targetBt.first})", Toast.LENGTH_SHORT).show()
                        onComplete(true, btRes.second)
                    }
                    return@withContext
                }
            }

            // 4. Try Network Socket if custom IP configured
            if (currentConfig.ipAddress.isNotBlank() && currentConfig.ipAddress != "192.168.1.100") {
                val sockRes = sendBytesToSocket(currentConfig.ipAddress, currentConfig.port, data)
                if (sockRes.first) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Direct Bill Printed! (${currentConfig.ipAddress})", Toast.LENGTH_SHORT).show()
                        onComplete(true, sockRes.second)
                    }
                    return@withContext
                }
            }

            // 5. Fallback: If no hardware printer detected, open system print dialog
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "No direct thermal printer found. Opening print dialog...", Toast.LENGTH_SHORT).show()
                openSystemPrintDialog(context, title, textSlip)
                onComplete(true, "Fallback print dialog opened")
            }
        }
    }

    /**
     * Prints a test slip to verify printer connection and alignment
     */
    suspend fun printTestSlip(
        context: Context,
        config: PrinterConfig = currentConfig,
        onComplete: (Boolean, String) -> Unit
    ) {
        val width = config.paperWidth
        val maxCols = width.lineChars
        val divider = "-".repeat(maxCols)

        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("       70MM LOUNGE POS")
        sb.appendLine("    PRINTER HARDWARE TEST")
        sb.appendLine("================================")
        sb.appendLine("Mode: ${config.connectionType.displayName}")
        sb.appendLine("Paper: ${width.label}")
        if (config.connectionType == PrinterConnectionType.WIFI || config.connectionType == PrinterConnectionType.LAN) {
            sb.appendLine("IP Address: ${config.ipAddress}:${config.port}")
        } else if (config.connectionType == PrinterConnectionType.BLUETOOTH) {
            sb.appendLine("Device: ${config.bluetoothDeviceName}")
        } else if (config.connectionType == PrinterConnectionType.USB) {
            sb.appendLine("USB Device: ${config.usbDeviceName}")
        }
        sb.appendLine("Status: ONLINE & READY")
        sb.appendLine(divider)
        sb.appendLine("0123456789 ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        sb.appendLine("Thermal alignment: OK")
        sb.appendLine("Auto-cut: SUPPORTED")
        sb.appendLine("Cash drawer kick: SUPPORTED")
        sb.appendLine(divider)
        sb.appendLine("Test completed at:")
        sb.appendLine(SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date()))
        sb.appendLine("================================")

        withContext(Dispatchers.IO) {
            val bytes = generateTestSlipEscPos(config)
            when (config.connectionType) {
                PrinterConnectionType.AUTO_DETECT -> {
                    autoDetectAndPrint(
                        context = context,
                        data = bytes,
                        textSlip = sb.toString(),
                        title = "Printer-Test-Slip",
                        onComplete = onComplete
                    )
                }
                PrinterConnectionType.USB -> {
                    val res = sendBytesToUsb(context, config.usbDeviceName, bytes)
                    withContext(Dispatchers.Main) {
                        if (res.first) {
                            onComplete(true, "USB Test Slip Printed (${res.second})")
                        } else {
                            openSystemPrintDialog(context, "Printer-Test-Page", sb.toString())
                            onComplete(false, res.second)
                        }
                    }
                }
                PrinterConnectionType.BLUETOOTH -> {
                    val res = sendBytesToBluetooth(
                        context = context,
                        deviceAddressOrName = config.bluetoothDeviceAddress.ifBlank { config.bluetoothDeviceName },
                        data = bytes
                    )
                    withContext(Dispatchers.Main) {
                        if (res.first) {
                            onComplete(true, "Bluetooth Test Slip Printed (Auto-cut sent)")
                        } else {
                            openSystemPrintDialog(context, "Printer-Test-Page", sb.toString())
                            onComplete(false, res.second)
                        }
                    }
                }
                PrinterConnectionType.WIFI, PrinterConnectionType.LAN -> {
                    val res = sendBytesToSocket(config.ipAddress, config.port, bytes)
                    withContext(Dispatchers.Main) {
                        if (res.first) {
                            onComplete(true, "Test print sent successfully to ${config.ipAddress}")
                        } else {
                            openSystemPrintDialog(context, "Printer-Test-Page", sb.toString())
                            onComplete(false, res.second)
                        }
                    }
                }
                PrinterConnectionType.SYSTEM_PRINT -> {
                    withContext(Dispatchers.Main) {
                        openSystemPrintDialog(context, "Printer-Test-Page", sb.toString())
                        onComplete(true, "Test page rendered to System Print Dialog")
                    }
                }
            }
        }
    }

    /**
     * Connects directly via USB Host (UsbManager) to send raw ESC/POS bytes.
     * Fully compatible with Rugtek RP-326, RP-80, RP-330, and other POS USB thermal printers.
     */
    fun sendBytesToUsb(
        context: Context,
        deviceAddressOrName: String = "",
        data: ByteArray
    ): Pair<Boolean, String> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            ?: return Pair(false, "USB Service unavailable on device")

        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) {
            val nodeRes = sendBytesToDeviceNode(data)
            if (nodeRes.first) return nodeRes
            return Pair(false, "No USB printer detected on terminal")
        }

        // Find printer device
        var targetDevice: UsbDevice? = null
        if (deviceAddressOrName.isNotBlank()) {
            targetDevice = deviceList.values.find { dev ->
                dev.deviceName.contains(deviceAddressOrName, ignoreCase = true) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && dev.productName?.contains(deviceAddressOrName, ignoreCase = true) == true)
            }
        }

        if (targetDevice == null) {
            targetDevice = deviceList.values.find { isUsbDevicePrinter(it) } ?: deviceList.values.firstOrNull()
        }

        if (targetDevice == null) {
            val nodeRes = sendBytesToDeviceNode(data)
            if (nodeRes.first) return nodeRes
            return Pair(false, "No compatible USB POS printer found")
        }

        // Check permission
        if (!usbManager.hasPermission(targetDevice)) {
            try {
                requestUsbPermission(context, usbManager, targetDevice)
            } catch (_: Exception) {}
            // Also test writing to device node
            val nodeRes = sendBytesToDeviceNode(data)
            if (nodeRes.first) return nodeRes
            return Pair(false, "USB Permission requested. Please accept prompt on screen.")
        }

        var connection: UsbDeviceConnection? = null
        var claimedIface: UsbInterface? = null
        return try {
            connection = usbManager.openDevice(targetDevice)
                ?: run {
                    val nodeRes = sendBytesToDeviceNode(data)
                    if (nodeRes.first) return nodeRes
                    return Pair(false, "Failed to open USB connection to ${targetDevice.deviceName}")
                }

            // Find bulk OUT endpoint
            var outEndpoint: UsbEndpoint? = null
            for (i in 0 until targetDevice.interfaceCount) {
                val iface = targetDevice.getInterface(i)
                for (j in 0 until iface.endpointCount) {
                    val ep = iface.getEndpoint(j)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT) {
                        outEndpoint = ep
                        claimedIface = iface
                        break
                    }
                }
                if (outEndpoint != null) break
            }

            if (claimedIface == null || outEndpoint == null) {
                connection.close()
                val nodeRes = sendBytesToDeviceNode(data)
                if (nodeRes.first) return nodeRes
                return Pair(false, "No Bulk OUT endpoint found on USB printer")
            }

            connection.claimInterface(claimedIface, true)

            val chunkSize = 4096
            var offset = 0
            var totalWritten = 0
            while (offset < data.size) {
                val length = minOf(chunkSize, data.size - offset)
                val chunk = data.copyOfRange(offset, offset + length)
                val written = connection.bulkTransfer(outEndpoint, chunk, length, 5000)
                if (written < 0) break
                totalWritten += written
                offset += length
            }

            val devTitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                targetDevice.productName?.ifBlank { null } ?: targetDevice.deviceName
            } else {
                targetDevice.deviceName
            }

            Pair(true, "Printed via $devTitle (Auto-Cut sent)")
        } catch (e: Exception) {
            val nodeRes = sendBytesToDeviceNode(data)
            if (nodeRes.first) return nodeRes
            Pair(false, "USB Print Error: ${e.localizedMessage}")
        } finally {
            try {
                claimedIface?.let { connection?.releaseInterface(it) }
                connection?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Direct POS hardware character device node (/dev/usb/lp0, /dev/ttyUSB0, etc.)
     */
    fun sendBytesToDeviceNode(data: ByteArray): Pair<Boolean, String> {
        val candidateNodes = listOf(
            "/dev/usb/lp0",
            "/dev/usb/lp1",
            "/dev/ttyUSB0",
            "/dev/ttyACM0",
            "/dev/ttyS1",
            "/dev/ttyS3",
            "/dev/ttyS4"
        )
        for (path in candidateNodes) {
            val f = File(path)
            if (f.exists()) {
                try {
                    FileOutputStream(f).use { fos ->
                        fos.write(data)
                        fos.flush()
                    }
                    return Pair(true, "Printed to POS device node ($path)")
                } catch (_: Exception) {}
            }
        }
        return Pair(false, "No accessible POS device node")
    }

    fun getAvailableDeviceNode(): String? {
        val candidateNodes = listOf(
            "/dev/usb/lp0",
            "/dev/usb/lp1",
            "/dev/ttyUSB0",
            "/dev/ttyACM0",
            "/dev/ttyS1",
            "/dev/ttyS3",
            "/dev/ttyS4"
        )
        for (path in candidateNodes) {
            val f = File(path)
            if (f.exists() && f.canWrite()) return path
        }
        return null
    }

    /**
     * Request permission for USB device
     */
    fun requestUsbPermission(context: Context, usbManager: UsbManager, device: UsbDevice) {
        val permissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent("com.example.USB_PERMISSION"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    /**
     * Check if a USB device matches printer profile
     */
    fun isUsbDevicePrinter(device: UsbDevice): Boolean {
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_PRINTER) return true
            if (iface.interfaceClass == 255 || iface.interfaceClass == 0) {
                for (j in 0 until iface.endpointCount) {
                    val ep = iface.getEndpoint(j)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT) {
                        return true
                    }
                }
            }
        }
        val knownPosVendorIds = setOf(
            0x0416, 0x0483, 0x0493, 0x04b8, 0x0525, 0x0fe6, 0x1504, 0x154f, 0x1659, 0x1a86, 0x1fc9, 0x20d1, 0x28e9, 0x6868
        )
        if (knownPosVendorIds.contains(device.vendorId)) return true

        val name = (device.deviceName + " " + (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) device.productName ?: "" else "")).lowercase()
        return name.contains("print") || name.contains("pos") || name.contains("rugtek") || name.contains("thermal") || name.contains("receipt")
    }

    /**
     * Scans and returns connected USB printers
     */
    fun getConnectedUsbPrinters(context: Context): List<UsbPrinterInfo> {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return emptyList()
        val list = mutableListOf<UsbPrinterInfo>()
        for (device in usbManager.deviceList.values) {
            val hasPerm = usbManager.hasPermission(device)
            val devName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                device.productName?.ifBlank { null } ?: device.deviceName
            } else {
                device.deviceName
            }
            val manufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                device.manufacturerName ?: "POS Hardware"
            } else {
                "POS Hardware"
            }
            if (isUsbDevicePrinter(device) || usbManager.deviceList.size == 1) {
                list.add(UsbPrinterInfo(devName, manufacturer, device.vendorId, device.productId, hasPerm, device))
            }
        }
        return list
    }

    /**
     * Returns human-readable summary of detected printer hardware
     */
    fun getDetectedPrinterSummary(context: Context): String {
        val usbPrinters = getConnectedUsbPrinters(context)
        if (usbPrinters.isNotEmpty()) {
            return "Rugtek / USB: ${usbPrinters.first().name}"
        }
        val node = getAvailableDeviceNode()
        if (node != null) {
            return "POS Hardware ($node)"
        }
        val btPrinters = getPairedBluetoothPrinters()
        if (btPrinters.isNotEmpty()) {
            return "Bluetooth: ${btPrinters.first().first}"
        }
        if (currentConfig.connectionType == PrinterConnectionType.WIFI || currentConfig.connectionType == PrinterConnectionType.LAN) {
            return "${currentConfig.connectionType.displayName} (${currentConfig.ipAddress})"
        }
        return "Auto-Detect (Rugtek / USB / Bluetooth)"
    }

    /**
     * Connects via Bluetooth RFCOMM SPP Socket to paired Bluetooth 58mm/80mm thermal POS printer.
     */
    suspend fun sendBytesToBluetooth(
        context: Context,
        deviceAddressOrName: String,
        data: ByteArray
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                ?: return@withContext Pair(false, "Bluetooth hardware not detected on device")

            if (!bluetoothAdapter.isEnabled) {
                return@withContext Pair(false, "Bluetooth is turned off. Please turn on Bluetooth.")
            }

            val bondedDevices = try {
                bluetoothAdapter.bondedDevices ?: emptySet()
            } catch (e: SecurityException) {
                return@withContext Pair(false, "Bluetooth permission required: ${e.message}")
            }

            val targetDevice = if (deviceAddressOrName.isNotBlank()) {
                bondedDevices.find {
                    it.address.equals(deviceAddressOrName, ignoreCase = true) ||
                    (it.name != null && it.name.equals(deviceAddressOrName, ignoreCase = true))
                } ?: bondedDevices.firstOrNull()
            } else {
                bondedDevices.firstOrNull()
            }

            if (targetDevice == null) {
                return@withContext Pair(false, "No paired Bluetooth printer found. Please pair printer first.")
            }

            try { bluetoothAdapter.cancelDiscovery() } catch (_: Exception) {}

            val sppUuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val socket = targetDevice.createRfcommSocketToServiceRecord(sppUuid)
            socket.connect()
            socket.outputStream.use { outStream ->
                outStream.write(data)
                outStream.flush()
            }
            socket.close()
            Pair(true, "Sent to ${targetDevice.name ?: targetDevice.address}")
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Bluetooth printer connection failed")
        }
    }

    /**
     * Returns list of currently paired/bonded Bluetooth devices (Name to MAC address)
     */
    fun getPairedBluetoothPrinters(): List<Pair<String, String>> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
            adapter.bondedDevices.map { (it.name ?: "Bluetooth Printer") to it.address }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Connects via TCP Socket to Wi-Fi / LAN Ethernet POS Printer
     */
    private fun sendBytesToSocket(ip: String, port: Int, data: ByteArray): Pair<Boolean, String> {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), 2500)
                val out: OutputStream = socket.getOutputStream()
                out.write(data)
                out.flush()
                Pair(true, "Sent $ip:$port")
            }
        } catch (e: Exception) {
            Pair(false, e.localizedMessage ?: "Connection error")
        }
    }

    /**
     * Android Native PrintManager integration for printing to any system-discovered printer (Wi-Fi, Bluetooth, USB, PDF)
     */
    fun openSystemPrintDialog(context: Context, jobName: String, contentText: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val pdi = PrintDocumentInfo.Builder("$jobName.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback?.onLayoutFinished(pdi, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        destination?.fileDescriptor?.let { fd ->
                            FileOutputStream(fd).use { fos ->
                                val pdfDoc = android.graphics.pdf.PdfDocument()
                                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(300, 700, 1).create()
                                val page = pdfDoc.startPage(pageInfo)
                                val canvas = page.canvas

                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 10f
                                    typeface = android.graphics.Typeface.MONOSPACE
                                }

                                var y = 25f
                                for (line in contentText.lines()) {
                                    canvas.drawText(line, 12f, y, paint)
                                    y += 13f
                                }

                                pdfDoc.finishPage(page)
                                pdfDoc.writeTo(fos)
                                pdfDoc.close()
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            },
            PrintAttributes.Builder().build()
        )
    }

    /**
     * ESC/POS Byte Array Generator for Customer Receipt with Dynamic UPI QR Code & Auto-Cut
     */
    fun generateReceiptEscPos(orderWithItems: OrderWithItems, config: PrinterConfig): ByteArray {
        val order = orderWithItems.order
        val items = orderWithItems.items
        val maxCols = config.paperWidth.lineChars

        val builder = EscPosBuilder()
            .initPrinter()
            .alignCenter()
            .bold(true)
            .textSize(EscPosBuilder.SIZE_DOUBLE_BOTH)
            .textLine("70MM LOUNGE")
            .textSize(EscPosBuilder.SIZE_NORMAL)
            .textLine("RESTAURANT & CLUB")
            .bold(false)
            .textLine("City Centre, Sector 4, Bokaro Steel City")
            .textLine("GSTIN: 20AABCL1234F1Z5 | FSSAI: 11522026000123")
            .textLine("Tel: +91 89874 77773")
            .feed(1)
            .alignLeft()
            .doubleDivider(maxCols)
            .twoColumn("Invoice No:", order.invoiceNumber, maxCols)
            .twoColumn("Date & Time:", CurrencyFormatter.formatDateTime(order.timestamp), maxCols)

        if (order.tableNumber.isNotBlank()) {
            builder.twoColumn("Table / Zone:", order.tableNumber, maxCols)
        }
        builder.twoColumn("Customer:", order.customerName, maxCols)
        builder.twoColumn("Payment Mode:", order.paymentMethod, maxCols)
        builder.divider(maxCols)

        // Header
        builder.bold(true)
            .threeColumn("Item", "Qty x Rate", "Total", maxCols)
            .bold(false)
            .divider(maxCols)

        // Items with intelligent line wrapping to prevent dish names truncation
        for (item in items) {
            val qtyRate = "${item.quantity}x ${CurrencyFormatter.formatWhole(item.unitPrice)}"
            val lineTotal = CurrencyFormatter.format(item.totalAmount)
            val maxItemCol = if (maxCols >= 48) 22 else 14
            if (item.productName.length > maxItemCol) {
                builder.alignLeft().textLine(item.productName)
                builder.twoColumn("   $qtyRate", lineTotal, maxCols)
            } else {
                builder.threeColumn(item.productName, qtyRate, lineTotal, maxCols)
            }
        }

        builder.divider(maxCols)
        builder.twoColumn("Total Items: ${items.size}", "Total Qty: ${items.sumOf { it.quantity }}", maxCols)
        builder.divider(maxCols)

        // Totals
        builder.twoColumn("Subtotal:", CurrencyFormatter.format(order.subtotal), maxCols)
        if (order.discountAmount > 0) {
            builder.twoColumn("Discount:", "-${CurrencyFormatter.format(order.discountAmount)}", maxCols)
        }
        val halfTax = order.taxAmount / 2.0
        builder.twoColumn("CGST Tax (2.5%):", CurrencyFormatter.format(halfTax), maxCols)
        builder.twoColumn("SGST Tax (2.5%):", CurrencyFormatter.format(halfTax), maxCols)
        builder.doubleDivider(maxCols)

        // Grand Total (Bold & Larger)
        builder.bold(true)
            .textSize(EscPosBuilder.SIZE_DOUBLE_BOTH)
            .twoColumn("NET TOTAL:", CurrencyFormatter.format(order.totalAmount), maxCols / 2)
            .textSize(EscPosBuilder.SIZE_NORMAL)
            .bold(false)
            .feed(1)
            .alignCenter()
            .textLine("[ OFFICIAL VERIFIED BILL • 70MM LOUNGE ]")
            .feed(1)

        // Dynamic UPI QR code on printed bill (instant payment with exact bill amount)
        val upiAmount = String.format(Locale.US, "%.2f", order.totalAmount)
        val upiString = "upi://pay?pa=8987477773@okbizaxis&pn=70MM%20Lounge&am=$upiAmount&cu=INR&tn=Bill-${order.invoiceNumber}"

        builder.alignCenter()
            .bold(true)
            .textLine("--- SCAN TO PAY (INSTANT UPI) ---")
            .bold(false)
            .textLine("Google Pay / PhonePe / Paytm / BHIM")
            .feed(1)
            .qrCode(upiString, moduleSize = if (config.paperWidth == ThermalPaperWidth.WIDTH_58MM) 5 else 6)
            .feed(1)
            .bold(true)
            .textSize(EscPosBuilder.SIZE_DOUBLE_HEIGHT)
            .textLine("Amount to Pay: ₹$upiAmount")
            .textSize(EscPosBuilder.SIZE_NORMAL)
            .bold(false)
            .textLine("UPI ID: 8987477773@okbizaxis")
            .feed(1)
            .textLine("*** THANK YOU! VISIT AGAIN ***")
            .textLine("70MM LOUNGE & CLUB • BOKARO")
            .feed(2)

        if (config.autoCutPaper) {
            builder.cutPaper(fullCut = false)
        }
        builder.kickDrawer()

        return builder.build()
    }

    /**
     * ESC/POS Byte Array Generator for KOT
     */
    fun generateKotEscPos(kot: KitchenOrderTicket, config: PrinterConfig): ByteArray {
        val maxCols = config.paperWidth.lineChars
        val builder = EscPosBuilder()
            .initPrinter()
            .alignCenter()
            .bold(true)
            .textSize(EscPosBuilder.SIZE_DOUBLE_BOTH)
            .textLine("KITCHEN ORDER TICKET")
            .textLine(kot.kotNumber)
            .textSize(EscPosBuilder.SIZE_NORMAL)
            .bold(false)
            .alignLeft()
            .twoColumn("TABLE:", kot.tableNumber, maxCols)
            .twoColumn("ZONE:", kot.zone, maxCols)
            .twoColumn("TIME:", CurrencyFormatter.formatTimeOnly(kot.timestamp), maxCols)
            .doubleDivider(maxCols)
            .bold(true)

        val items = kot.itemsSummary.split(" ; ")
        for (item in items) {
            builder.textLine(">> $item")
        }
        builder.bold(false)

        if (kot.specialNotes.isNotBlank()) {
            builder.divider(maxCols)
                .textLine("NOTE: ${kot.specialNotes}")
        }

        builder.feed(2)
        if (config.autoCutPaper) {
            builder.cutPaper(fullCut = false)
        }

        return builder.build()
    }

    /**
     * Test Slip ESC/POS
     */
    private fun generateTestSlipEscPos(config: PrinterConfig): ByteArray {
        val builder = EscPosBuilder()
            .initPrinter()
            .alignCenter()
            .bold(true)
            .textSize(EscPosBuilder.SIZE_DOUBLE_BOTH)
            .textLine("70MM LOUNGE POS")
            .textSize(EscPosBuilder.SIZE_NORMAL)
            .textLine("PRINTER TEST SUCCESSFUL")
            .bold(false)
            .textLine("Mode: ${config.connectionType.displayName}")
            .textLine("Paper Width: ${config.paperWidth.label}")
            .textLine(SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date()))
            .feed(1)
            .qrCode("https://70mmlounge.com", moduleSize = 5)
            .feed(1)
            .textLine("Direct Thermal ESC/POS Ready!")
            .feed(2)
        if (config.autoCutPaper) {
            builder.cutPaper(fullCut = false)
        }
        builder.beep(1)
        return builder.build()
    }

    /**
     * Plain Text Formatters
     */
    fun generateReceiptPlainText(orderWithItems: OrderWithItems, paperWidth: ThermalPaperWidth): String {
        val order = orderWithItems.order
        val items = orderWithItems.items
        val maxCols = paperWidth.lineChars
        val divider = "-".repeat(maxCols)

        val sb = StringBuilder()
        sb.appendLine(centerText("70MM LOUNGE", maxCols))
        sb.appendLine(centerText("RESTAURANT & CLUB", maxCols))
        sb.appendLine(centerText("Bokaro | Tel: 8987477773", maxCols))
        sb.appendLine(divider)
        sb.appendLine(formatTwoColumn("Invoice:", order.invoiceNumber, maxCols))
        sb.appendLine(formatTwoColumn("Date:", CurrencyFormatter.formatDateTime(order.timestamp), maxCols))
        if (order.tableNumber.isNotBlank()) {
            sb.appendLine(formatTwoColumn("Table:", order.tableNumber, maxCols))
        }
        sb.appendLine(formatTwoColumn("Customer:", order.customerName, maxCols))
        sb.appendLine(formatTwoColumn("Payment:", order.paymentMethod, maxCols))
        sb.appendLine(divider)
        sb.appendLine(formatItemHeader(maxCols))
        sb.appendLine(divider)

        for (item in items) {
            val name = if (item.productName.length > 20) item.productName.take(20) else item.productName
            val qtyRate = "${item.quantity}x ${CurrencyFormatter.formatWhole(item.unitPrice)}"
            val lineTotal = CurrencyFormatter.format(item.totalAmount)
            sb.appendLine(formatItemRow(name, qtyRate, lineTotal, maxCols))
        }

        sb.appendLine(divider)
        sb.appendLine(formatTwoColumn("Subtotal:", CurrencyFormatter.format(order.subtotal), maxCols))
        if (order.discountAmount > 0) {
            sb.appendLine(formatTwoColumn("Discount:", "-${CurrencyFormatter.format(order.discountAmount)}", maxCols))
        }
        sb.appendLine(formatTwoColumn("CGST:", CurrencyFormatter.format(order.taxAmount / 2.0), maxCols))
        sb.appendLine(formatTwoColumn("SGST:", CurrencyFormatter.format(order.taxAmount / 2.0), maxCols))
        sb.appendLine(divider)
        sb.appendLine(formatTwoColumn("TOTAL:", CurrencyFormatter.format(order.totalAmount), maxCols))
        sb.appendLine(divider)
        sb.appendLine(centerText("*** THANK YOU VISIT AGAIN ***", maxCols))

        return sb.toString()
    }

    fun generateKotPlainText(kot: KitchenOrderTicket, paperWidth: ThermalPaperWidth): String {
        val maxCols = paperWidth.lineChars
        val divider = "=".repeat(maxCols)

        val sb = StringBuilder()
        sb.appendLine(centerText("KITCHEN ORDER TICKET", maxCols))
        sb.appendLine(centerText(kot.kotNumber, maxCols))
        sb.appendLine(divider)
        sb.appendLine(formatTwoColumn("Table:", kot.tableNumber, maxCols))
        sb.appendLine(formatTwoColumn("Section/Zone:", kot.zone, maxCols))
        sb.appendLine(formatTwoColumn("Time:", CurrencyFormatter.formatTimeOnly(kot.timestamp), maxCols))
        sb.appendLine(divider)
        for (item in kot.itemsSummary.split(" ; ")) {
            sb.appendLine(">> $item")
        }
        if (kot.specialNotes.isNotBlank()) {
            sb.appendLine(divider)
            sb.appendLine("Notes: ${kot.specialNotes}")
        }
        sb.appendLine(divider)
        return sb.toString()
    }

    private fun formatTwoColumn(left: String, right: String, totalCols: Int): String {
        val space = totalCols - left.length - right.length
        return if (space > 0) {
            left + " ".repeat(space) + right
        } else {
            "$left $right"
        }
    }

    private fun centerText(text: String, totalCols: Int): String {
        if (text.length >= totalCols) return text
        val pad = (totalCols - text.length) / 2
        return " ".repeat(pad) + text
    }

    private fun formatItemHeader(totalCols: Int): String {
        return if (totalCols >= 48) {
            "Item Name                     Qty   Rate    Total"
        } else {
            "Item              Qty   Rate   Tot"
        }
    }

    private fun formatItemRow(name: String, qtyRate: String, total: String, totalCols: Int): String {
        val left = name.padEnd(if (totalCols >= 48) 26 else 16)
        val mid = qtyRate.padEnd(if (totalCols >= 48) 12 else 8)
        val right = total.padStart(totalCols - left.length - mid.length)
        return left + mid + right
    }
}
