package com.example.ims.data

import com.example.ims.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface CommunicationManager {

    val data: MutableSharedFlow<Resource<LocationResult>>

    fun reconnect()

    fun disconnect()

    fun startReceiving()

    fun startSending()

    fun closeConnection()
}