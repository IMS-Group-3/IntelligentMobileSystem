package com.example.ims.data

data class LocationResult(
    val x:Int,
    val y:Int,
    val collisionAvoidance:Boolean,
    val connectionState: ConnectionState
)
