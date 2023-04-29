package com.example.ims

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class ImagePopUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_pop_up)

        // Get the ByteArray from the Intent and convert it to a Bitmap
        val byteArray = intent.getByteArrayExtra("bitmap")
        val bitmap = byteArray?.let { BitmapFactory.decodeByteArray(byteArray, 0, it.size) }

        //create AlertDialog with image
        popupWindow(bitmap!!)

        //clickListener to close dialog
        closeDialog()

        //Set title for popup
        setPopupTitle()
    }

    private fun popupWindow(bitmap: Bitmap) {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()

        // Load image from the provided Bitmap
        loadImage(bitmap)

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

   private fun loadImage(bitmap: Bitmap) {
        // Get ImageView from activity_image_pop_up.xml
        val imageView = findViewById<ImageView>(R.id.popup_collision_image)
        // Set the provided Bitmap to the ImageView
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

    private fun setPopupTitle() {
        //change "Cat" to the text we get from backend of the avoided collision
        val text = "Cat"

        val textView = findViewById<TextView>(R.id.popup_window_title)
        textView.text = text
    }
}