package com.example.ims

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ims.databinding.ActivityControlBinding


class ControlActivity : AppCompatActivity() {

    private var mTextViewAngle: TextView? = null
    private var mTextViewStrength: TextView? = null
    private var mTextViewCoordinate: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewBinding = ActivityControlBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        mTextViewAngle = viewBinding.textViewAngle
        mTextViewStrength = viewBinding.textViewStrength
        mTextViewCoordinate = viewBinding.textViewCoordinate

        val joystick = viewBinding.joystickView
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