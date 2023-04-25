package com.example.ims.data

data class ControlCommand(
    val angle:Int,
    val strength:Int,
    val connectionState: ConnectionState
)