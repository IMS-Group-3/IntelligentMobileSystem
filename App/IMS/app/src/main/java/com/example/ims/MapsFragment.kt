package com.example.ims

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

// For simulating the array coordinates from a socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapsFragment : Fragment() {
    private var isStarted = false
    private var isStopped = true

    // private var isStopVisible = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapGridView = view.findViewById<MapGridView>(R.id.mapGridView)
        val startButton = view.findViewById<Button>(R.id.startButton)
        val infoButton = view.findViewById<ImageButton>(R.id.infoButton)
        val centerButton = view.findViewById<FloatingActionButton>(R.id.centerButton)
        centerButton.hide()

        // Array of markers. Replace with real time coordinates from the mower team.
        val markers = listOf(
            LocationMarker(5000, 5000, true),
            LocationMarker(5100, 5100, false),
            LocationMarker(5200, 5200, false),
            LocationMarker(5100, 5300, false),
            LocationMarker(5000, 5400, false),
            LocationMarker(4900, 5000, false),
            LocationMarker(4800, 5100, false),
            LocationMarker(4700, 5200, true),
            LocationMarker(4600, 5300, false),
            LocationMarker(4500, 5400, false),
            LocationMarker(4400, 10000, false),
            LocationMarker(4400, 10500, false),
            LocationMarker(4400, 10700, true),
            LocationMarker(4400, 11000, true),
            LocationMarker(4400, 13500, true),
            LocationMarker(2000, 13500, false),
            LocationMarker(2500, 12500, false),
            LocationMarker(2500, 12000, false),
            LocationMarker(2500, 11500, false),
            LocationMarker(2500, 11000, false),
            LocationMarker(2500, 10500, false),
            LocationMarker(2500, 10000, false),
            LocationMarker(2500, 9500, true),
            LocationMarker(2500, 9000, false),
            LocationMarker(2500, 8500, false),
            LocationMarker(2500, 8000, false),
            LocationMarker(2500, 7500, false),
        )

        startButton.setOnClickListener {
            isStopped = false

            if (!isStarted) {
                mapGridView.centerMap()
                centerButton.show()
                startButton.setText(R.string.stop_button)
                startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B0000"))
                isStarted = true

                // Simulate adding markers with a 500ms delay
                CoroutineScope(Dispatchers.Main).launch {
                    markers.forEach { marker ->
                        if (!isStopped) {
                            mapGridView.addMarker(marker)
                            delay(300)
                        }
                    }
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
            mapGridView.centerMap()
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
}