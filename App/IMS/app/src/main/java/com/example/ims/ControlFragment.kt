package com.example.ims

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.example.ims.data.ConnectionState
import io.github.controlwear.virtual.joystick.android.JoystickView


class ControlFragment : Fragment() {

    private val controlViewModel: ControlViewModel by activityViewModels()
    var bleConnectionState: ConnectionState? = null

    private var mTextViewAngle: TextView? = null
    private var mTextViewStrength: TextView? = null
    private var mTextViewCoordinate: TextView? = null
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

            if(bleConnectionState == ConnectionState.CurrentlyInitializing){

                if(controlViewModel.initializingMessage != null){
                    mTextViewCoordinate!!.text = controlViewModel.initializingMessage!!
                }

            }else if(!allPermissionsGranted()){
                mTextViewCoordinate!!.text = "Go to the app setting and allow the missing permissions."
            }else if(controlViewModel.errorMessage != null){

                mTextViewCoordinate!!.text = controlViewModel.errorMessage!!
                if(allPermissionsGranted()){
                    controlViewModel.initializeConnection()
                }
            }else if(bleConnectionState == ConnectionState.Connected){
                controlViewModel
                controlViewModel.angle = angle
                controlViewModel.strength = strength
                controlViewModel.sendMessage()

            }else if(bleConnectionState == ConnectionState.Disconnected){
                controlViewModel.initializeConnection()
                mTextViewCoordinate!!.text = "Initialize again"

            }
            mTextViewAngle!!.text = "$angle°"
            mTextViewStrength!!.text = "$strength%"
            /*mTextViewCoordinate!!.text = String.format(
                "x%03d:y%03d",
                joystick.normalizedX,
                joystick.normalizedY
            )*/
        }

    }
    private fun allPermissionsGranted():Boolean{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val bluetoothScanPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)

            if (bluetoothScanPermission != PackageManager.PERMISSION_GRANTED){ return false }
            if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED){ return false  }        }

        val accessFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        if (accessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        return true
    }

}
