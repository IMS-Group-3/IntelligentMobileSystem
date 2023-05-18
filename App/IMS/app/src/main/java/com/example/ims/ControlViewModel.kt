package com.example.ims

import android.bluetooth.BluetoothAdapter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ims.data.ConnectionState
import com.example.ims.data.CommunicationManager
import com.example.ims.data.ControlCommand
import com.example.ims.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ControlViewModel @Inject constructor(
    private val communicationManager: CommunicationManager,
    private var bluetoothAdapter: BluetoothAdapter

) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var angle by mutableStateOf(0)

    var strength by mutableStateOf(0)

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    val  isBluetoothDialogDenied = MutableLiveData<Boolean?>(false)

    fun sendMessage(){
        if (connectionState == ConnectionState.Connected) {
            val controlComand = ControlCommand(
                angle,
                strength,
                ConnectionState.Connected
            )
            communicationManager.startSending(controlComand)
        }else if (connectionState == ConnectionState.Disconnected ){
            reconnect()
        }else{
            initializeConnection()
        }
    }
    private fun subscribeToChanges(){
        viewModelScope.launch {
            communicationManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Uninitialized
                    }
                }
            }
        }
    }
    fun disconnect(){
        communicationManager.disconnect()
    }

    fun reconnect(){
        communicationManager.reconnect()
    }

    fun initializeConnection(){
        errorMessage = null
        subscribeToChanges()
        communicationManager.startScaning()
    }

    override fun onCleared() {
        super.onCleared()
        communicationManager.closeConnection()
    }
    fun isBluetoothEnabled():Boolean{
        return bluetoothAdapter.isEnabled
    }
}