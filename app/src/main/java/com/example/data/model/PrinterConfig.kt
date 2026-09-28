package com.example.data.model

enum class PrinterConnectionType(val displayName: String, val defaultPort: Int = 9100) {
    THERMAL_ESC_POS("Thermal (ESC/POS)"),
    BLUETOOTH("Bluetooth Thermal"),
    WIFI("Wi-Fi Network Printer"),
    USB("USB Direct POS"),
    LAN("LAN / Ethernet Printer")
}

enum class ThermalPaperWidth(val label: String, val lineChars: Int) {
    WIDTH_58MM("58mm (2-inch)", 32),
    WIDTH_80MM("80mm (3-inch)", 48)
}

data class PrinterConfig(
    val connectionType: PrinterConnectionType = PrinterConnectionType.THERMAL_ESC_POS,
    val paperWidth: ThermalPaperWidth = ThermalPaperWidth.WIDTH_80MM,
    val ipAddress: String = "192.168.1.100",
    val port: Int = 9100,
    val bluetoothDeviceAddress: String = "",
    val bluetoothDeviceName: String = "POS-Printer-80",
    val usbDeviceName: String = "POS USB Receipt Printer",
    val autoPrintBillOnCheckout: Boolean = true,
    val autoPrintKotOnOrder: Boolean = true,
    val isConnected: Boolean = true,
    val printCopies: Int = 1
)
