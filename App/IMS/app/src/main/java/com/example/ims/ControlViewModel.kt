package com.example.ims

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ims.data.ConnectionState
import com.example.ims.data.CommunicationManager
import com.example.ims.data.ControlCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ControlViewModel @Inject constructor(
    private val communicationManager: CommunicationManager
) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var angle by mutableStateOf(0)

    var strength by mutableStateOf(0)

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    fun sendMessage(){
        viewModelScope.launch {

            val controlComand = ControlCommand(
                angle,
                strength,
                ConnectionState.Connected
            )
            communicationManager. startSending(controlComand)

            /*collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        angle = result.data.x
                        strength = result.data.y
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
            }*/
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
        communicationManager.startScaning()
    }

    override fun onCleared() {
        super.onCleared()
        communicationManager.closeConnection()
    }


}