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
import androidx.core.text.isDigitsOnly
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

        startButton.setOnClickListener {
            isStopped = false

            if (!isStarted) {
                start(pathId)
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
    private fun intToBoolean(value: Int): Boolean {
        return value != 0
    }

    override fun onStart() {
        super.onStart()
        isStopped= false
        start(pathMapViewModel.pathId.value!!)

        /*pathMapViewModel.run {
            hasData.observe(this@PathMapFragment) {
                if (it != null ){
                    start(pathMapViewModel.pathId.value!!)

                }
            }
        }*/
    }

    override fun onStop() {
        super.onStop()
        centerButton.hide()
        isStopped = true
    }

    private fun start(pathId:Int){
        if (!isStarted) {
            isStopped = false
            mapView.centerMap()
            centerButton.show()
            startButton.setText(R.string.stop_button)
            startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#8B0000"))
            isStarted = true

            CoroutineScope(Dispatchers.Main).launch {
                val pathData = getPath(pathId)
                for (marker in pathData) {
                    if (isStopped) break // Exit the loop if stopped
                    mapView.addMarker(marker)
                    delay(100)
                }
            }
        } else {
            isStopped = true // Stop the loop
            centerButton.hide()
            startButton.setText(R.string.start_button)
            startButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#223a1d"))
            isStarted = false
        }
    }
    private suspend fun getPath(pathId: Int): List<LocationMarker> {

        val markers = mutableListOf<LocationMarker>()

        val pathData = pathApi.getLocationsByPathId(pathId)

        pathData?.let {
            for ((key, valueList) in pathData) {
                val positionId = key.toInt()
                val x = valueList[0].toInt()
                val y = valueList[1].toInt()
                var collision = 0
                if (valueList[2].isDigitsOnly()){
                    collision = valueList[2].toInt()
                }
                val collisionOccurred = collision != 0

                val locationMarker = LocationMarker(positionId, x, y, collisionOccurred)
                markers.add(locationMarker)
            }
        }
        return markers
    }
}

class PathMapViewModel() : ViewModel(){

    var isHistory = MutableLiveData<Boolean>(false)
    val  pathId = MutableLiveData<Int>(0)
    //val hasData = MutableLiveData<Boolean>(false)



}