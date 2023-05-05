package com.example.ims

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.ims.data.ConnectionState
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import com.example.ims.views.CustomCalendar
import com.example.ims.views.DayViewContainer
import com.example.ims.views.MonthViewContainer
import com.kizitonwose.calendar.core.*
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.WeekCalendarView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val locationViewModelTemp: LocationViewModelTemp by activityViewModels()
    var bleConnectionState: ConnectionState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            bleConnectionState = locationViewModelTemp.connectionState
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bleConnectionState = locationViewModelTemp.connectionState

        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Uninitialized){
            locationViewModelTemp.initializeConnection()
        }

        val x = view.findViewById<TextView>(R.id.textView_x_coordinate)
        val y = view.findViewById<TextView>(R.id.textView_y_coordinate)
        val collisionAvoidance = view.findViewById<TextView>(R.id.textView_collision_avoidance)
        val connectionState = view.findViewById<TextView>(R.id.textView_connection_state)

        // Calendar
        val calendarView = view.findViewById<CalendarView>(R.id.calendarView)
        val monthTitleTextView = view.findViewById<TextView>(R.id.monthTitle)
        CustomCalendar(calendarView, monthTitleTextView, requireContext())

        if(bleConnectionState == ConnectionState.CurrentlyInitializing){
            x.text = ""
            y.text = ""
            collisionAvoidance.text = ""
            connectionState.text = ""

            if(locationViewModelTemp.initializingMessage != null){
                connectionState.text = locationViewModelTemp.initializingMessage!!
            }

        }else if(!allPermissionsGranted()){
                connectionState.text = "Go to the app setting and allow the missing permissions."
        }else if(locationViewModelTemp.errorMessage != null){

            connectionState.text = locationViewModelTemp.errorMessage!!
            if(allPermissionsGranted()){
                locationViewModelTemp.initializeConnection()
            }
        }else if(bleConnectionState == ConnectionState.Connected){

            x.text = "X: ${locationViewModelTemp.x}"
            y.text = "Y: ${locationViewModelTemp.y}"
            collisionAvoidance.text  = "Collisio nAvoidance: ${locationViewModelTemp.collisionAvoidance}"

        }else if(bleConnectionState == ConnectionState.Disconnected){
            locationViewModelTemp.initializeConnection()
            connectionState.text = "Initialize again"

        }else {
            connectionState.text = "No device was found"

        }

    }

    override fun onStart() {
        super.onStart()
        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Disconnected){
            locationViewModelTemp.reconnect()
        }
    }

    override fun onStop() {
        super.onStop()
        if(allPermissionsGranted() && bleConnectionState == ConnectionState.Connected){
            locationViewModelTemp.disconnect()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun allPermissionsGranted():Boolean{

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val bluetoothScanPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN)
            val bluetoothConnectPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)

            if (bluetoothScanPermission != PackageManager.PERMISSION_GRANTED){ return false }
            if (bluetoothConnectPermission != PackageManager.PERMISSION_GRANTED){ return false  }        }

        val accessFineLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarseLocationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (accessFineLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        if (accessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){ return false  }
        return true
    }

}