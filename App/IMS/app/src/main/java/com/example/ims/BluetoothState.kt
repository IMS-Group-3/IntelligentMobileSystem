package com.example.ims

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.Composable

@Composable
fun BluetoothState(
    onBluetoothStateChanged:()->Unit
) {

    SystemBroadcastReceiver(systemAction = BluetoothAdapter.ACTION_STATE_CHANGED) { bluetoothState ->
        val action = bluetoothState?.action ?: return@SystemBroadcastReceiver
        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            onBluetoothStateChanged()
        }
    }
}

