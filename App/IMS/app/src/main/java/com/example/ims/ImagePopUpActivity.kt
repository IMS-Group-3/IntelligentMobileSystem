package com.example.ims

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

        //create AlertDialog with image
        popupWindow()

        //clickListener to close dialog
        closeDialog()

        //Set title for popup
        setPopupTitle()
    }

    private fun popupWindow() {
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()

        //load image from loadImage() function
        loadImage()

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun loadImage() {
        //get ImageView from activity_image_pop_up.xml
        val imageView = findViewById<ImageView>(R.id.popup_collision_image)
        //sets image from drawable to imageview
        //replace when we can access image from backend
        imageView.setImageResource(R.drawable.image_chill_cat)
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