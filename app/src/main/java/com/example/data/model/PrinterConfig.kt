package com.example.data.model

enum class PrinterConnectionType(val displayName: String, val defaultPort: Int = 9100) {
    AUTO_DETECT("Auto-Detect (USB / Rugtek / Bluetooth)"),
    USB("USB Direct POS (Rugtek / Thermal)"),
    BLUETOOTH("Bluetooth Thermal"),
    WIFI("Wi-Fi Network Printer"),
    LAN("LAN / Ethernet Printer"),
    SYSTEM_PRINT("Android System Print Dialog")
}

enum class ThermalPaperWidth(val label: String, val lineChars: Int) {
    WIDTH_58MM("58mm (2-inch)", 32),
    WIDTH_80MM("80mm (3-inch)", 48)
}

data class PrinterConfig(
    val connectionType: PrinterConnectionType = PrinterConnectionType.AUTO_DETECT,
    val paperWidth: ThermalPaperWidth = ThermalPaperWidth.WIDTH_80MM,
    val ipAddress: String = "192.168.1.100",
    val port: Int = 9100,
    val bluetoothDeviceAddress: String = "",
    val bluetoothDeviceName: String = "POS-Printer-80",
    val usbDeviceName: String = "Rugtek POS Thermal Printer",
    val autoPrintBillOnCheckout: Boolean = true,
    val autoPrintKotOnOrder: Boolean = true,
    val isConnected: Boolean = true,
    val autoCutPaper: Boolean = true,
    val printCopies: Int = 1,
    val usbVendorId: Int = 0,
    val usbProductId: Int = 0,
    val lastDiscoveredSummary: String = ""
)
