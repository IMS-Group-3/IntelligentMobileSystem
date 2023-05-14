package com.example.ims

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ims.data.ConnectionState
import com.example.ims.data.CommunicationManager
import com.example.ims.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModelTemp  @Inject constructor (
    private val communicationManager: CommunicationManager
) : ViewModel(){

    var initializingMessage by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var x by mutableStateOf(0)
        private set

    var y by mutableStateOf(0)
        private set

    var collisionAvoidance by mutableStateOf(false)
        private set

    var connectionState by mutableStateOf<ConnectionState>(ConnectionState.Uninitialized)

    private fun subscribeToChanges(){
        viewModelScope.launch {
            communicationManager.data.collect{ result ->
                when(result){
                    is Resource.Success -> {
                        connectionState = result.data.connectionState
                        x = result.data.x
                        y = result.data.y
                        collisionAvoidance = result.data.collisionAvoidance
                    }

                    is Resource.Loading -> {
                        initializingMessage = result.message
                        connectionState = ConnectionState.CurrentlyInitializing
                    }

                    is Resource.Error -> {
                        errorMessage = result.errorMessage
                        connectionState = ConnectionState.Failed
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


}