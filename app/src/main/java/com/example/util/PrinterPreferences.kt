package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.PrinterConfig
import com.example.data.model.PrinterConnectionType
import com.example.data.model.ThermalPaperWidth

/**
 * SharedPreferences storage for saving and retrieving thermal printer configuration.
 * Automatically saves discovered USB / Bluetooth printer addresses for 1-click printing.
 */
object PrinterPreferences {

    private const val PREFS_NAME = "pos_printer_preferences"

    private const val KEY_CONNECTION_TYPE = "pref_printer_connection_type"
    private const val KEY_PAPER_WIDTH = "pref_printer_paper_width"
    private const val KEY_IP_ADDRESS = "pref_printer_ip_address"
    private const val KEY_PORT = "pref_printer_port"
    private const val KEY_BT_ADDRESS = "pref_printer_bt_address"
    private const val KEY_BT_NAME = "pref_printer_bt_name"
    private const val KEY_USB_NAME = "pref_printer_usb_name"
    private const val KEY_USB_VID = "pref_printer_usb_vid"
    private const val KEY_USB_PID = "pref_printer_usb_pid"
    private const val KEY_AUTO_CUT = "pref_printer_auto_cut"
    private const val KEY_AUTO_PRINT_BILL = "pref_printer_auto_bill"
    private const val KEY_AUTO_PRINT_KOT = "pref_printer_auto_kot"
    private const val KEY_COPIES = "pref_printer_copies"
    private const val KEY_LAST_SUMMARY = "pref_printer_last_summary"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Loads the saved printer config, or returns a sensible default if not configured.
     */
    fun loadConfig(context: Context): PrinterConfig {
        val prefs = getPrefs(context)
        val connTypeStr = prefs.getString(KEY_CONNECTION_TYPE, PrinterConnectionType.AUTO_DETECT.name)
            ?: PrinterConnectionType.AUTO_DETECT.name
        val connType = try {
            PrinterConnectionType.valueOf(connTypeStr)
        } catch (_: Exception) {
            PrinterConnectionType.AUTO_DETECT
        }

        val widthStr = prefs.getString(KEY_PAPER_WIDTH, ThermalPaperWidth.WIDTH_80MM.name)
            ?: ThermalPaperWidth.WIDTH_80MM.name
        val paperWidth = try {
            ThermalPaperWidth.valueOf(widthStr)
        } catch (_: Exception) {
            ThermalPaperWidth.WIDTH_80MM
        }

        return PrinterConfig(
            connectionType = connType,
            paperWidth = paperWidth,
            ipAddress = prefs.getString(KEY_IP_ADDRESS, "192.168.1.100") ?: "192.168.1.100",
            port = prefs.getInt(KEY_PORT, 9100),
            bluetoothDeviceAddress = prefs.getString(KEY_BT_ADDRESS, "") ?: "",
            bluetoothDeviceName = prefs.getString(KEY_BT_NAME, "POS Thermal") ?: "POS Thermal",
            usbDeviceName = prefs.getString(KEY_USB_NAME, "Rugtek Thermal Printer") ?: "Rugtek Thermal Printer",
            usbVendorId = prefs.getInt(KEY_USB_VID, 0),
            usbProductId = prefs.getInt(KEY_USB_PID, 0),
            autoPrintBillOnCheckout = prefs.getBoolean(KEY_AUTO_PRINT_BILL, true),
            autoPrintKotOnOrder = prefs.getBoolean(KEY_AUTO_PRINT_KOT, true),
            autoCutPaper = prefs.getBoolean(KEY_AUTO_CUT, true),
            printCopies = prefs.getInt(KEY_COPIES, 1),
            lastDiscoveredSummary = prefs.getString(KEY_LAST_SUMMARY, "") ?: ""
        )
    }

    /**
     * Saves full printer configuration to persistent storage.
     */
    fun saveConfig(context: Context, config: PrinterConfig) {
        getPrefs(context).edit()
            .putString(KEY_CONNECTION_TYPE, config.connectionType.name)
            .putString(KEY_PAPER_WIDTH, config.paperWidth.name)
            .putString(KEY_IP_ADDRESS, config.ipAddress)
            .putInt(KEY_PORT, config.port)
            .putString(KEY_BT_ADDRESS, config.bluetoothDeviceAddress)
            .putString(KEY_BT_NAME, config.bluetoothDeviceName)
            .putString(KEY_USB_NAME, config.usbDeviceName)
            .putInt(KEY_USB_VID, config.usbVendorId)
            .putInt(KEY_USB_PID, config.usbProductId)
            .putBoolean(KEY_AUTO_PRINT_BILL, config.autoPrintBillOnCheckout)
            .putBoolean(KEY_AUTO_PRINT_KOT, config.autoPrintKotOnOrder)
            .putBoolean(KEY_AUTO_CUT, config.autoCutPaper)
            .putInt(KEY_COPIES, config.printCopies)
            .putString(KEY_LAST_SUMMARY, config.lastDiscoveredSummary)
            .apply()
    }

    /**
     * Automatically stores discovered USB printer info for 1-click printing without user intervention.
     */
    fun saveDiscoveredUsbPrinter(
        context: Context,
        deviceName: String,
        vendorId: Int,
        productId: Int,
        summary: String
    ) {
        val current = loadConfig(context)
        val updated = current.copy(
            usbDeviceName = deviceName,
            usbVendorId = vendorId,
            usbProductId = productId,
            lastDiscoveredSummary = summary
        )
        saveConfig(context, updated)
    }

    /**
     * Automatically stores discovered Bluetooth printer MAC address and name for 1-click printing.
     */
    fun saveDiscoveredBluetoothPrinter(
        context: Context,
        deviceName: String,
        deviceAddress: String,
        summary: String
    ) {
        val current = loadConfig(context)
        val updated = current.copy(
            bluetoothDeviceName = deviceName,
            bluetoothDeviceAddress = deviceAddress,
            lastDiscoveredSummary = summary
        )
        saveConfig(context, updated)
    }

    /**
     * Updates the summary label of the currently connected hardware.
     */
    fun updateHardwareSummary(context: Context, summary: String) {
        getPrefs(context).edit().putString(KEY_LAST_SUMMARY, summary).apply()
    }
}
