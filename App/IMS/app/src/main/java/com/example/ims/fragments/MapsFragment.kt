package com.example.ims.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.example.ims.views.MapView
import com.example.ims.R
import com.example.ims.activities.MainActivity
import com.example.ims.data.Commands
import com.example.ims.data.LocationMarker
import com.example.ims.services.ImageApi
import com.example.ims.services.PathApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import sendCollisionNotification

// For simulating the array coordinates from a socket

class MapsFragment : Fragment(), MapView.OnCollisionListener {
    private var isStarted = false
    private var isStopped = true
    private lateinit var mapView: MapView
    private lateinit var startButton: Button
    private lateinit var infoButton : ImageButton
    private lateinit var centerButton : FloatingActionButton

    private val pathApi = PathApi()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCollisionListener = this

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startButton)
        infoButton = view.findViewById(R.id.infoButton)
        centerButton = view.findViewById(R.id.centerButton)
        centerButton.hide()

        startButton.setOnClickListener {
            if (!isStarted) {
                isStopped = false
                mapView.centerMap()
                centerButton.show()
                startButton.setText(R.string.stop_button)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B0000"))
                isStarted = true

                PathApi().sendManualCommand(Commands.M_AUTO){
                    Log.i("Start responseCode: ", it.toString())
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val pathData = getPath()
                    for (marker in pathData) {
                        if (isStopped) break // Exit the loop if stopped
                        mapView.addMarker(marker)
                    }
                }
            } else {
                isStopped = true // Stop the loop
                centerButton.hide()
                startButton.setText(R.string.start_button)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#223a1d"))
                isStarted = false

                PathApi().sendManualCommand(Commands.M_OFF){
                    Log.i("Stop responseCode: ", it.toString())
                }
            }
        }

        centerButton.setOnClickListener {
            mapView.centerMap()
        }

        infoButton.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info_button, null)
            val builder = AlertDialog.Builder(requireContext())
                .setView(dialogView)

            val dialog = builder.create()

            //can't close dialog by clicking outside
            dialog.setCanceledOnTouchOutside(false)

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            //close dialog through close_icon
            val closeButton = dialogView.findViewById<Button>(R.id.closeButton_info)
            closeButton?.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    // Handles collision notification
    override fun onCollision(imageId: Int) {

        (activity as? MainActivity)?.let { mainActivity ->
            ImageApi().getImageBitmapByID(
                imageId,
                onSuccess = { result ->
                    if (result.isSuccess) {
                        val imageResult = result.getOrNull()
                        val imageClassification = imageResult?.first
                        val bitmap = imageResult?.second
                        if (bitmap != null) {
                            sendCollisionNotification(mainActivity, bitmap, imageClassification!!)
                        } else {
                            Log.e("Notification", "Bitmap is null")
                        }
                    } else {
                        Log.e("Notification", "Failed to load image for notification")
                    }
                },
                onFailure = {
                    Log.e("Notification", "Failed to load image for notification")
                }
            )
        }
    }

    private suspend fun getPath(): List<LocationMarker> {
        val markers = mutableListOf<LocationMarker>()
        val displayedMarkers = mutableSetOf<LocationMarker>()

        while (true) {
            val pathData = pathApi.getPathById()

            pathData?.let {
                for ((key, valueList) in pathData) {
                    val positionId = key.toInt()
                    val x = valueList[0].toInt()
                    val y = valueList[1].toInt()
                    val collision = valueList[2].toInt()
                    val collisionOccurred = collision != 0

                    val locationMarker = LocationMarker(positionId, x, y, collisionOccurred)

                    //checks if displayedMarkers contains locationMarker, if not it adds it.
                    //makes sure already displayed positions wont be added to markers again
                    if (!displayedMarkers.contains(locationMarker)) {
                        displayedMarkers.add(locationMarker)
                        markers.add(locationMarker)
                    }
                }
            }

            if (markers.isNotEmpty()) {
                return markers
            }
            delay(200)
        }
    }
}