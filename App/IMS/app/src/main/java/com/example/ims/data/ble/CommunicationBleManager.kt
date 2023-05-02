package com.example.ims.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.util.Log
import com.example.ims.data.ConnectionState
import com.example.ims.data.LocationResult
import com.example.ims.data.CommunicationManager
import com.example.ims.data.ControlCommand
import com.example.ims.util.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@Suppress("DEPRECATION")
@SuppressLint("MissingPermission")

class CommunicationBleManager @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
    ) : CommunicationManager {
//00001801-0000-1000-8000-00805f9b34fb

        private val DEVICE_NAME = "AyhamBle"
        private val LOCATIN_SERVICE_UIID = "0000aa20-0000-1000-8000-00805f9b34fb"
        private val LOCATION_CHARACTERISTICS_UUID = "0000aa21-0000-1000-8000-00805f9b34fb"
        private val DRIVE_SERVICE_UUID = "0000aa20-0000-1000-8000-00805f9b34fb"
        private val DRIVE_CHARACTERISTICS_UUID = "0000aa21-0000-1000-8000-00805f9b34fb"

        override val data: MutableSharedFlow<Resource<LocationResult>> = MutableSharedFlow()

        private val bleScanner by lazy {
            bluetoothAdapter.bluetoothLeScanner
        }

        private val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        private var gatt: BluetoothGatt? = null

        private var isScanning = false

        private val coroutineScope = CoroutineScope(Dispatchers.Default)

        private val scanCallback = object : ScanCallback(){

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device.name != null){
                    Log.i("scan result ",result.device.name.toString() )

                }
                if(result.device.name == DEVICE_NAME){
                    Log.i("scan result ",result.device.name.toString() )
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Connecting to device..."))
                    }
                    if(isScanning){
                        result.device.connectGatt(context,false, gattCallback,BluetoothDevice.TRANSPORT_LE)
                        isScanning = false
                        bleScanner.stopScan(this)
                    }
                }
            }
        }

        private var currentConnectionAttempt = 1
        private var MAXIMUM_CONNECTION_ATTEMPTS = 5

        private val gattCallback = object : BluetoothGattCallback(){
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if(status == BluetoothGatt.GATT_SUCCESS){
                    if(newState == BluetoothProfile.STATE_CONNECTED){
                        coroutineScope.launch {
                            data.emit(Resource.Loading(message = "Discovering Services..."))
                        }
                        gatt.discoverServices()
                        this@CommunicationBleManager.gatt = gatt
                    } else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                        coroutineScope.launch {
                            data.emit(Resource.Success(data = LocationResult(0,0,false,ConnectionState.Disconnected)))
                        }
                        gatt.close()
                    }
                }else{
                    gatt.close()
                    currentConnectionAttempt+=1
                    coroutineScope.launch {
                        data.emit(
                            Resource.Loading(
                                message = "Attempting to connect $currentConnectionAttempt/$MAXIMUM_CONNECTION_ATTEMPTS"
                            )
                        )
                    }
                    if(currentConnectionAttempt<=MAXIMUM_CONNECTION_ATTEMPTS){
                        startScaning()
                    }else{
                        coroutineScope.launch {
                            data.emit(Resource.Error(errorMessage = "Could not connect to ble device"))
                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                with(gatt){
                    printGattTable()
                    coroutineScope.launch {
                        data.emit(Resource.Loading(message = "Adjusting MTU space..."))
                    }
                    gatt.requestMtu(517)
                }
            }

            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                val characteristic = findCharacteristics(LOCATIN_SERVICE_UIID, LOCATION_CHARACTERISTICS_UUID)
                Log.e("Mtu","Mtttttttt() ")

                if(characteristic == null){
                    Log.e("Mtu","errrrrrrrrr ")

                    coroutineScope.launch {
                        data.emit(Resource.Error(errorMessage = "Could not find location publisher"))
                    }
                    return
                }
                enableNotification(characteristic)
            }

            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
               // value: ByteArray
            ) {
                Log.e("char","gooood() ")

                with(characteristic){
                    when(uuid){
                        UUID.fromString(LOCATION_CHARACTERISTICS_UUID) -> {
                        //XX XX XX XX XX XX
                        val x = value[1].toInt() //+ value[2].toInt()
                        val y = value[2].toInt() //+ value[5].toInt()
                        val collisionAvoidance = value[3].toInt() != 0
                        val locationResult = LocationResult(
                            x,
                            y,
                            collisionAvoidance,
                            ConnectionState.Connected
                        )
                        coroutineScope.launch {
                            data.emit(
                                Resource.Success(data = locationResult)
                            )
                        }
                            Log.e("Dataaaaaaaaaaaaaaaaa","locationResult.toString() ")
                        }
                        else -> Unit
                    }
                }
            }


        }



        private fun enableNotification(characteristic: BluetoothGattCharacteristic){
            val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
            val payload = when {
                characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> return
            }

            characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
                if(gatt?.setCharacteristicNotification(characteristic, true) == false){
                    Log.d("BLEReceiveManager","set characteristics notification failed")
                    return
                }
                writeDescription(cccdDescriptor, payload)
            }
        }

        private fun writeDescription(descriptor: BluetoothGattDescriptor, payload: ByteArray){
            gatt?.let { gatt ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    gatt.writeDescriptor(descriptor,payload)
                } else {
                    descriptor.value = payload
                    gatt.writeDescriptor(descriptor)
                }

            } ?: error("Not connected to a BLE device!")
        }

        private fun findCharacteristics(serviceUUID: String, characteristicsUUID:String): BluetoothGattCharacteristic?{
            return gatt?.services?.find { service ->
                service.uuid.toString() == serviceUUID
            }?.characteristics?.find { characteristics ->
                characteristics.uuid.toString() == characteristicsUUID
            }
        }

        override fun startScaning() {
            coroutineScope.launch {
                data.emit(Resource.Loading(message = "Scanning Ble devices..."))
            }
            isScanning = true
            bleScanner.startScan(null,scanSettings,scanCallback)
        }
    override fun startSending(data: ControlCommand) {
        val characteristic = findCharacteristics(DRIVE_SERVICE_UUID, DRIVE_CHARACTERISTICS_UUID)
        val driveCommand = byteArrayOf(data.angle.toByte(), data.strength.toByte())
        if (characteristic != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt?.writeCharacteristic(
                    characteristic,
                    driveCommand,
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
            } else {
                characteristic.value = driveCommand
                gatt?.writeCharacteristic(characteristic)
            }
        }
        gatt?.disconnect()
    }

        override fun reconnect() {
            gatt?.connect()
        }

        override fun disconnect() {
            gatt?.disconnect()
        }
        override fun closeConnection() {
            bleScanner.stopScan(scanCallback)
            val characteristic = findCharacteristics(LOCATIN_SERVICE_UIID, LOCATION_CHARACTERISTICS_UUID)
            if(characteristic != null){
                disconnectCharacteristic(characteristic)
            }
            gatt?.close()
        }

        private fun disconnectCharacteristic(characteristic: BluetoothGattCharacteristic){
            val cccdUuid = UUID.fromString(CCCD_DESCRIPTOR_UUID)
            characteristic.getDescriptor(cccdUuid)?.let { cccdDescriptor ->
                if(gatt?.setCharacteristicNotification(characteristic,false) == false){
                    Log.d("LocationReceiveManager","set charateristics notification failed")
                    return
                }
                writeDescription(cccdDescriptor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            }
        }
}