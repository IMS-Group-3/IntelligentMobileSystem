package com.example.ims

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

// For simulating the array coordinates from a socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MapsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialogBuilder = AlertDialog.Builder(requireContext())

        val dialogView = layoutInflater.inflate(R.layout.mower_area_dialogbox, null)
        dialogBuilder.setView(dialogView)

        // Dialogbox for the mower area input
        dialogBuilder.setPositiveButton("Submit") { dialog, which ->
            val editTextWidth = dialogView.findViewById<EditText>(R.id.editTextWidth)
            val editTextHeight = dialogView.findViewById<EditText>(R.id.editTextHeight)

            // Setting the width and height to 0 if the string in the EditText widget is empty
            val width = if (editTextWidth.text.toString().isNotEmpty()) {
                editTextWidth.text.toString().toInt()
            } else {
                0
            }
            val height = if (editTextHeight.text.toString().isNotEmpty()) {
                editTextHeight.text.toString().toInt()
            } else {
                0
            }
            val mapGridView = view.findViewById<MapGridView>(R.id.mapGridView)
            mapGridView.setGridSize(width, height)

            // Array of markers. Replace with real time coordinates from the mower team.
            val markers = listOf(
                GridMarker(50, 50, Color.RED, false),
                GridMarker(51, 51, Color.RED, false),
                GridMarker(52, 52, Color.RED, false),
                GridMarker(51, 53, Color.RED, false),
                GridMarker(50, 54, Color.RED, false),
                GridMarker(49, 50, Color.RED, false),
                GridMarker(48, 51, Color.RED, false),
                GridMarker(47, 52, Color.RED, true),
                GridMarker(46, 53, Color.RED, false),
                GridMarker(45, 54, Color.RED, false),
                GridMarker(44, 100, Color.RED, false),
                GridMarker(44, 105, Color.RED, false),
                GridMarker(44, 107, Color.RED, true),
                GridMarker(44, 110, Color.RED, true),
                GridMarker(44, 135, Color.RED, true),
                GridMarker(20, 135, Color.RED, false),
                GridMarker(25, 125, Color.RED, false),
                GridMarker(25, 120, Color.RED, false),
                GridMarker(25, 115, Color.RED, false),
                GridMarker(25, 110, Color.RED, false),
                GridMarker(25, 105, Color.RED, false),
                GridMarker(25, 100, Color.RED, false),
                GridMarker(25, 95, Color.RED, true),
                GridMarker(25, 90, Color.RED, false),
                GridMarker(25, 85, Color.RED, false),
                GridMarker(25, 80, Color.RED, false),
                GridMarker(25, 75, Color.RED, false),
            )

            // Simulate adding markers with a delay
            CoroutineScope(Dispatchers.Main).launch {
                markers.forEach { marker ->
                    mapGridView.addMarker(marker)
                    delay(500) // Add a 500ms delay between adding markers
                }
            }

            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
