package com.example.ims

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import io.github.controlwear.virtual.joystick.android.JoystickView


class ControlFragment : Fragment() {

    private val viewModel: ControlFragmentViewModel by activityViewModels()

    private var mTextViewAngle: TextView? = null
    private var mTextViewStrength: TextView? = null
    private var mTextViewCoordinate: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_control,
            container,
            false
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mTextViewAngle = view.findViewById(R.id.textView_angle) as TextView
        mTextViewStrength = view.findViewById(R.id.textView_strength) as TextView
        mTextViewCoordinate = view.findViewById(R.id.textView_coordinate)

        val joystick = view.findViewById(R.id.joystickView) as JoystickView
        joystick.setOnMoveListener { angle, strength ->
            mTextViewAngle!!.text = "$angle°"
            mTextViewStrength!!.text = "$strength%"
            mTextViewCoordinate!!.text = String.format(
                "x%03d:y%03d",
                joystick.normalizedX,
                joystick.normalizedY
            )
        }

    }


}
class ControlFragmentViewModel() : ViewModel(){




}