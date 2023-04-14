package com.example.ims

import android.graphics.Color
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

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
            val markerColor = Color.RED // Set the color of the marker
            mapGridView.addMarker(width, height, markerColor)

            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
}
