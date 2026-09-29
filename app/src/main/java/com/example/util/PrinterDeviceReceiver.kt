package com.example.util

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager

/**
 * BroadcastReceiver listening for hardware events:
 * - USB device attached / detached
 * - Bluetooth paired / connected / disconnected
 * Automatically triggers hardware scan and updates local preferences for 1-click printing.
 */
class PrinterDeviceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED,
            UsbManager.ACTION_USB_DEVICE_DETACHED,
            BluetoothDevice.ACTION_ACL_CONNECTED,
            BluetoothDevice.ACTION_ACL_DISCONNECTED,
            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                PrinterDiscoveryManager.scanAndStorePrinters(context)
            }
        }
    }
}
