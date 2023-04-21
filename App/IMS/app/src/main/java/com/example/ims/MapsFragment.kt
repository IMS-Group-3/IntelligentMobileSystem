package com.example.ims

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
        val centerButton = view.findViewById<FloatingActionButton>(R.id.centerButton)
        centerButton.hide()

        // Array of markers. Replace with real time coordinates from the mower team.
        val markers = listOf(
            GridMarker(5000, 5000, Color.BLUE, true),
            GridMarker(5100, 5100, Color.BLUE, false),
            GridMarker(5200, 5200, Color.BLUE, false),
            GridMarker(5100, 5300, Color.BLUE, false),
            GridMarker(5000, 5400, Color.BLUE, false),
            GridMarker(4900, 5000, Color.BLUE, false),
            GridMarker(4800, 5100, Color.BLUE, false),
            GridMarker(4700, 5200, Color.BLUE, true),
            GridMarker(4600, 5300, Color.BLUE, false),
            GridMarker(4500, 5400, Color.BLUE, false),
            GridMarker(4400, 10000, Color.BLUE, false),
            GridMarker(4400, 10500, Color.BLUE, false),
            GridMarker(4400, 10700, Color.BLUE, true),
            GridMarker(4400, 11000, Color.BLUE, true),
            GridMarker(4400, 13500, Color.BLUE, true),
            GridMarker(2000, 13500, Color.BLUE, false),
            GridMarker(2500, 12500, Color.BLUE, false),
            GridMarker(2500, 12000, Color.BLUE, false),
            GridMarker(2500, 11500, Color.BLUE, false),
            GridMarker(2500, 11000, Color.BLUE, false),
            GridMarker(2500, 10500, Color.BLUE, false),
            GridMarker(2500, 10000, Color.BLUE, false),
            GridMarker(2500, 9500, Color.BLUE, true),
            GridMarker(2500, 9000, Color.BLUE, false),
            GridMarker(2500, 8500, Color.BLUE, false),
            GridMarker(2500, 8000, Color.BLUE, false),
            GridMarker(2500, 7500, Color.BLUE, false),
        )

        startButton.setOnClickListener {
            isStopped = false

            if (!isStarted) {
                mapGridView.centerMap()
                centerButton.show()
                startButton.setText(R.string.stop_button)
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

    }
}
