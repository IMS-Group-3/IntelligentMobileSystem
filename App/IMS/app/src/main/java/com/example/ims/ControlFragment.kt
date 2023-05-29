package com.example.ims

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.ims.data.ConnectionState
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


class ControlFragment : Fragment() {

    private val controlViewModel: ControlViewModel by activityViewModels()
    var bleConnectionState: ConnectionState? = null

    private var mTextViewAngle: TextView? = null
    private var mTextViewStrength: TextView? = null
    private var mTextViewCoordinate: TextView? = null
    private var isBluetoothDialogDisplayed = false
    @Inject
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_control,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTextViewAngle = view.findViewById(R.id.textView_angle) as TextView
        mTextViewStrength = view.findViewById(R.id.textView_strength) as TextView
        mTextViewCoordinate = view.findViewById(R.id.textView_coordinate)
        bleConnectionState = controlViewModel.connectionState
        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Uninitialized){
            controlViewModel.initializeConnection()
        }

        val joystick = view.findViewById(R.id.joystickView) as JoystickView
        joystick.setOnMoveListener { angle, strength ->
            bleConnectionState = controlViewModel.connectionState
            if (angle == 0 && strength == 0){
                CoroutineScope(Dispatchers.Main).launch {
                    for(i in 1..4){
                        send(angle,strength)
                        delay(200)
                    }
                }

            }
            send(angle,strength)


            mTextViewAngle!!.text = "$angle°"
            mTextViewStrength!!.text = "$strength%"

        }

    }
    private fun send(angle:Int,strength:Int){
        if (!allPermissionsGranted()) {
            mTextViewCoordinate!!.text =
                "Go to the app setting and allow the missing permissions."

        } else {
            enableBluetoothIfNot()
            if (bleConnectionState == ConnectionState.Connected) {
                controlViewModel.angle = angle
                controlViewModel.strength = strength
                controlViewModel.sendMessage()

            } else if (bleConnectionState == ConnectionState.Disconnected) {
                controlViewModel.reconnect()
                mTextViewCoordinate!!.text = "Reconnect"

            } else {
                controlViewModel.initializeConnection()
                mTextViewCoordinate!!.text = "Initialize again"
            }
        }
    }
    override fun onStart() {
        super.onStart()
        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Disconnected){
            controlViewModel.reconnect()
            Log.i("state","is connecting")
        }
    }
    override fun onStop() {
        super.onStop()
        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Connected){
            controlViewModel.disconnect()
            Log.i("state","is disconnecting")
        }
    }
    private fun allPermissionsGranted():Boolean{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val bluetoothScanPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)

            if (bluetoothScanPermission != PackageManager.PERMISSION_GRANTED){ return false }
            if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        }

        val accessFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        if (accessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        return true
    }
    private fun enableBluetoothIfNot(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN )
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED)
        ){
            if(!controlViewModel.isBluetoothEnabled() && !isBluetoothDialogDisplayed){
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startBluetoothIntentForResult.launch(enableBluetoothIntent)
                isBluetoothDialogDisplayed = true
            }
        }
    }

    private val startBluetoothIntentForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode != Activity.RESULT_OK){
                controlViewModel.isBluetoothDialogDenied.value = true
            }
            isBluetoothDialogDisplayed = true
        }
}
