package com.example.ims

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.ims.views.MapView
import com.example.ims.R
import com.example.ims.activities.MainActivity
import com.example.ims.data.LocationMarker
import com.example.ims.services.ImageApi
import com.example.ims.services.PathApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import sendCollisionNotification

// For simulating the array coordinates from a socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class PathMapFragment : Fragment(), MapView.OnCollisionListener {
    private val pathMapViewModel: PathMapViewModel by activityViewModels()

    private var isStarted = false
    private var isStopped = true
    private lateinit var mapView: MapView
    private lateinit var startButton: Button
    private lateinit var infoButton : ImageButton
    private lateinit var centerButton : FloatingActionButton

    private var markers = listOf<LocationMarker>()
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
        if(pathMapViewModel.isHistory.value!!){
            startButton.visibility = View.GONE
        }

        val pathId = pathMapViewModel.pathId.value!!
        //Array of markers
        pathApi.getPathById(pathId) { pathData ->
            // Iterate over the key-value pairs
            //key = positionId, valueList = x, y, timestamp, collisionOccurred
            for ((key, valueList) in pathData) {
                //Puts all values for specific key in a list
                val listValues = mutableListOf<List<String>>()
                listValues.add(valueList)
                //Iterates through each value in the list
                for (value in listValues) {
                    val x = value[0].toInt()
                    val y = value[1].toInt()
                    val collision = value[3].toInt()
                    val collisionOccurred = intToBoolean(collision)

                    markers+=(LocationMarker(x, y, collisionOccurred))
                }
            }
            pathMapViewModel.hasData.postValue(true)
        }

        startButton.setOnClickListener {
            isStopped = false

            if (!isStarted) {
                start()
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
    private fun intToBoolean(value: Int): Boolean {
        return value != 0
    }

    override fun onStart() {
        super.onStart()
        isStopped= false
        pathMapViewModel.run {
            hasData.observe(this@PathMapFragment) {
                if (it != null && it){
                    start()

                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        centerButton.hide()
        isStopped = true
    }

    private fun start(){
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
    }
}

class PathMapViewModel() : ViewModel(){

    var isHistory = MutableLiveData<Boolean>(false)
    val  pathId = MutableLiveData<Int>(0)
    val hasData = MutableLiveData<Boolean>(false)



}