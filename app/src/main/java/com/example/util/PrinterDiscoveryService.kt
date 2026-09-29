package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.model.PrinterConfig
import com.example.data.model.PrinterConnectionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Discovered thermal printer hardware information.
 */
data class DiscoveredPrinter(
    val name: String,
    val type: PrinterConnectionType,
    val addressOrPort: String,
    val vendorId: Int = 0,
    val productId: Int = 0,
    val isReady: Boolean = true,
    val description: String = ""
)

/**
 * Background Service and Manager that scans for connected USB or paired Bluetooth printers
 * and stores the address in local preferences for instant 'one-click' printing.
 */
class PrinterDiscoveryService : Service() {

    private val serviceJob = Job()
    private val scope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val hardwareReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                when (action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED,
                    UsbManager.ACTION_USB_DEVICE_DETACHED,
                    BluetoothDevice.ACTION_ACL_CONNECTED,
                    BluetoothDevice.ACTION_ACL_DISCONNECTED,
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        context?.let { ctx ->
                            PrinterDiscoveryManager.scanAndStorePrinters(ctx)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerHardwareReceiver()
        startPeriodicScan()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PrinterDiscoveryManager.scanAndStorePrinters(applicationContext)
        return START_STICKY
    }

    private fun registerHardwareReceiver() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        registerReceiver(hardwareReceiver, filter)
    }

    private fun startPeriodicScan() {
        scope.launch {
            while (isActive) {
                PrinterDiscoveryManager.scanAndStorePrinters(applicationContext)
                delay(30_000) // Re-scan every 30 seconds to catch hot-plugged printers
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(hardwareReceiver)
        } catch (_: Exception) {}
        serviceJob.cancel()
    }
}

/**
 * Singleton Manager to trigger scans and observe currently discovered hardware.
 */
object PrinterDiscoveryManager {

    private val _activePrinterState = MutableStateFlow<DiscoveredPrinter?>(null)
    val activePrinterState: StateFlow<DiscoveredPrinter?> = _activePrinterState.asStateFlow()

    private val knownPosVendorIds = setOf(
        0x0416, // Winbond / ZJiang / POS Thermal
        0x0483, // STMicroelectronics / Rugtek / Rongta
        0x0493, // Magnascan
        0x04b8, // Seiko Epson
        0x0525, // Netchip POS
        0x0fe6, // ICS
        0x1504, // Sunlux
        0x154f, // SNBC
        0x1659, // Prologix
        0x1a86, // QinHeng Electronics (CH340/CH341 USB to Serial POS)
        0x1fc9, // NXP POS
        0x20d1, // Gcube
        0x28e9, // GigaDevice POS
        0x6868  // Generic Thermal ESC/POS
    )

    /**
     * Scans for connected USB POS printers and bonded Bluetooth thermal printers,
     * stores the best active address into local preferences, and prepares 1-click printing.
     */
    fun scanAndStorePrinters(context: Context): DiscoveredPrinter? {
        // 1. Try USB POS Printer
        val usbPrinter = scanUsbPrinters(context)
        if (usbPrinter != null) {
            PrinterPreferences.saveDiscoveredUsbPrinter(
                context = context,
                deviceName = usbPrinter.name,
                vendorId = usbPrinter.vendorId,
                productId = usbPrinter.productId,
                summary = "USB: ${usbPrinter.name}"
            )
            // Update PrinterManager in-memory config
            PrinterManager.currentConfig = PrinterPreferences.loadConfig(context)
            _activePrinterState.value = usbPrinter
            return usbPrinter
        }

        // 2. Try Linux POS hardware character device node (/dev/usb/lp0, etc.)
        val nodePrinter = scanDeviceNodes()
        if (nodePrinter != null) {
            PrinterPreferences.updateHardwareSummary(context, "Hardware Port: ${nodePrinter.addressOrPort}")
            PrinterManager.currentConfig = PrinterPreferences.loadConfig(context)
            _activePrinterState.value = nodePrinter
            return nodePrinter
        }

        // 3. Try Paired Bluetooth Thermal Printer
        val btPrinter = scanBluetoothPrinters(context)
        if (btPrinter != null) {
            PrinterPreferences.saveDiscoveredBluetoothPrinter(
                context = context,
                deviceName = btPrinter.name,
                deviceAddress = btPrinter.addressOrPort,
                summary = "Bluetooth: ${btPrinter.name}"
            )
            PrinterManager.currentConfig = PrinterPreferences.loadConfig(context)
            _activePrinterState.value = btPrinter
            return btPrinter
        }

        // 4. Default fallback: System Print
        val fallback = DiscoveredPrinter(
            name = "Android System Print",
            type = PrinterConnectionType.SYSTEM_PRINT,
            addressOrPort = "system",
            isReady = true,
            description = "Print dialog fallback"
        )
        _activePrinterState.value = fallback
        return fallback
    }

    private fun scanUsbPrinters(context: Context): DiscoveredPrinter? {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager ?: return null
        val deviceList = usbManager.deviceList
        if (deviceList.isEmpty()) return null

        for (device in deviceList.values) {
            if (isUsbDeviceThermalPrinter(device)) {
                val devName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    device.productName?.ifBlank { null } ?: device.deviceName
                } else {
                    device.deviceName
                }
                return DiscoveredPrinter(
                    name = devName,
                    type = PrinterConnectionType.USB,
                    addressOrPort = device.deviceName,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    isReady = usbManager.hasPermission(device),
                    description = "USB POS Thermal Printer (VID:${Integer.toHexString(device.vendorId)})"
                )
            }
        }
        return null
    }

    private fun isUsbDeviceThermalPrinter(device: UsbDevice): Boolean {
        // Class 7 is USB PRINTER class
        for (i in 0 until device.interfaceCount) {
            val iface = device.getInterface(i)
            if (iface.interfaceClass == UsbConstants.USB_CLASS_PRINTER) return true
            if (iface.interfaceClass == 0 || iface.interfaceClass == 255) {
                for (j in 0 until iface.endpointCount) {
                    val ep = iface.getEndpoint(j)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK && ep.direction == UsbConstants.USB_DIR_OUT) {
                        return true
                    }
                }
            }
        }
        if (knownPosVendorIds.contains(device.vendorId)) return true

        val name = (device.deviceName + " " + (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) device.productName ?: "" else "")).lowercase()
        return name.contains("print") || name.contains("pos") || name.contains("rugtek") || name.contains("thermal") || name.contains("receipt")
    }

    private fun scanDeviceNodes(): DiscoveredPrinter? {
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
            if (f.exists() && f.canWrite()) {
                return DiscoveredPrinter(
                    name = "POS Port ($path)",
                    type = PrinterConnectionType.USB,
                    addressOrPort = path,
                    isReady = true,
                    description = "Direct hardware kernel device node"
                )
            }
        }
        return null
    }

    private fun scanBluetoothPrinters(context: Context): DiscoveredPrinter? {
        val adapter = BluetoothAdapter.getDefaultAdapter() ?: return null
        if (!adapter.isEnabled) return null

        val bonded = try {
            adapter.bondedDevices ?: emptySet()
        } catch (_: SecurityException) {
            return null
        }

        for (dev in bonded) {
            val devName = (dev.name ?: "").lowercase()
            val isPrinter = devName.contains("print") ||
                devName.contains("pos") ||
                devName.contains("thermal") ||
                devName.contains("receipt") ||
                devName.contains("rp-") ||
                devName.contains("mpt") ||
                devName.contains("rugtek") ||
                devName.contains("58") ||
                devName.contains("80") ||
                dev.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.IMAGING

            if (isPrinter) {
                return DiscoveredPrinter(
                    name = dev.name ?: "Bluetooth Thermal Printer",
                    type = PrinterConnectionType.BLUETOOTH,
                    addressOrPort = dev.address,
                    isReady = true,
                    description = "Paired Bluetooth ESC/POS Thermal Printer (${dev.address})"
                )
            }
        }

        // If any bonded device exists and nothing matched explicitly, return the first one
        if (bonded.isNotEmpty()) {
            val first = bonded.first()
            return DiscoveredPrinter(
                name = first.name ?: "Bluetooth Device",
                type = PrinterConnectionType.BLUETOOTH,
                addressOrPort = first.address,
                isReady = true,
                description = "Paired Bluetooth Device (${first.address})"
            )
        }
        return null
    }

    /**
     * Starts the background discovery service
     */
    fun startService(context: Context) {
        try {
            val intent = Intent(context, PrinterDiscoveryService::class.java)
            context.startService(intent)
        } catch (_: Exception) {}
        // Also perform an immediate synchronous scan
        scanAndStorePrinters(context)
    }
}
