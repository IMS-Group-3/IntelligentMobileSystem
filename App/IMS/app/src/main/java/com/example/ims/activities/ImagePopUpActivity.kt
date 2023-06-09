package com.example.ims.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.ims.R


class ImagePopUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_pop_up)

        // Gets the ByteArray from the Intent and converts it back to Bitmap
        val byteArray = intent.getByteArrayExtra("bitmap")
        val imageClassification = intent.getStringExtra("imageClassification")
        val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

        //create AlertDialog with image
        popupWindow(bitmap!!, imageClassification!!)

        //clickListener to close dialog
        closeDialog()
    }

    private fun popupWindow(bitmap: Bitmap, imageClassification: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()

        // Load image from the Bitmap
        loadImage(bitmap)
        val text = findViewById<TextView>(R.id.popup_window_title)
        text.text = imageClassification
        dialog.show()

        //Removes the dimming effect on the popup when displayed
        dialog.window?.setDimAmount(0f)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

   private fun loadImage(bitmap: Bitmap) {
        // Get ImageView from activity_image_pop_up.xml
        val imageView = findViewById<ImageView>(R.id.popup_collision_image)
        imageView.setImageBitmap(bitmap)
    }

    private fun closeDialog() {
        //can't close dialog by clicking outside
        setFinishOnTouchOutside(false)

        //close dialog through close_icon
        val closeButton = findViewById<ImageButton>(R.id.popup_close_icon)
        closeButton?.setOnClickListener {
            finish()
        }
    }
}