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
import com.example.ims.data.LocationMarker
import com.example.ims.services.ImageApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import sendCollisionNotification

// For simulating the array coordinates from a socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MapsFragment : Fragment(), MapView.OnCollisionListener {
    private var isStarted = false
    private var isStopped = true
    private lateinit var mapView: MapView
    private lateinit var startButton: Button
    private lateinit var infoButton : ImageButton
    private lateinit var centerButton : FloatingActionButton

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

        // Array of markers. Replace with real time coordinates from the mower team.
        val markers = listOf(
            LocationMarker(5000, 5000, true),
            LocationMarker(5500, 5000, false),
            LocationMarker(6000, 5000, false),
            LocationMarker(6500, 5000, false),
            LocationMarker(6500, 5500, false),
            LocationMarker(6500, 6000, false),
            LocationMarker(6000, 6000, false),
            LocationMarker(5500, 6000, false),
            LocationMarker(5000, 6000, false),
            LocationMarker(4500, 6000, true),
            LocationMarker(4500, 6500, false),
            LocationMarker(4500, 7000, false),
            LocationMarker(4500, 7500, false),
            LocationMarker(5000, 7500, false),
            LocationMarker(5500, 7500, false),
            LocationMarker(6000, 7500, false),
            LocationMarker(6500, 7500, true),
            LocationMarker(7000, 7500, false),
            LocationMarker(7500, 7500, false),
            LocationMarker(8000, 7500, false),
            LocationMarker(8500, 7500, false),
            LocationMarker(9000, 7500, false),
            LocationMarker(9500, 7500, true),
            LocationMarker(9500, 8000, false),
            LocationMarker(9500, 8500, false),
            LocationMarker(9000, 8500, false),
            LocationMarker(8500, 8500, false),
            LocationMarker(8000, 8500, false),
            LocationMarker(7500, 8500, false),
            LocationMarker(7000, 8500, false),
            LocationMarker(6500, 8500, false),
            LocationMarker(6000, 8500, false),
            LocationMarker(5500, 8500, false),
            LocationMarker(5000, 8500, false),
            LocationMarker(4500, 8500, false),
            LocationMarker(4000, 8500, false),
            LocationMarker(3500, 8500, false),
            LocationMarker(3500, 8000, false),
            LocationMarker(3500, 7500, false),
            LocationMarker(3500, 7000, false),
            LocationMarker(3500, 6500, false),
            LocationMarker(3500, 6000, false),
            LocationMarker(3500, 5500, false),
            LocationMarker(3500, 5000, false),
            LocationMarker(4000, 5000, false),
            LocationMarker(4500, 5000, false),
            LocationMarker(5000, 5000, false),
        )

        startButton.setOnClickListener {
            isStopped = false

            if (!isStarted) {
                mapView.centerMap()
                centerButton.show()
                startButton.setText(R.string.stop_button)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B0000"))
                isStarted = true

                // Simulate adding markers with a 500ms delay
                CoroutineScope(Dispatchers.Main).launch {
                    markers.forEach { marker ->
                        if (!isStopped) {
                            mapView.addMarker(marker)
                            delay(300)
                        }
                    }
                    centerButton.hide()
                    startButton.setText(R.string.start_button)
                    startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#223a1d"))
                    isStopped = true
                    isStarted = false
                }
            } else {
                centerButton.hide()
                isStopped = true
            }

        }

        centerButton.setOnClickListener {
            mapView.centerMap()
        }

        infoButton?.setOnClickListener {
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
                onSuccess = { bitmap ->
                    sendCollisionNotification(mainActivity, bitmap)
                },
                onFailure = {
                    Log.e("Notification", "Failed to load image for notification")
                }
            )
        }
    }
}