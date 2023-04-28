package com.example.ims.services

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.json.JSONObject


class ImageApi {
    private val baseUrl = "http://16.16.68.202"

    private fun fetchImage(urlString: String, callback: (Result<Bitmap>) -> Unit) {
        val url = URL(urlString)
        Thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    // Decode the image data
                    val imageData = JSONObject(response).getJSONObject("imageData")
                    val encodedImage = imageData.getString("encodedImage")
                    val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
                    val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                    callback(Result.success(decodedBitmap))
                } else {
                    callback(Result.failure(Exception("Error: $responseCode")))
                }

                connection.disconnect()
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }.start()
    }

    fun getImageById(id: Int, callback: (Result<Bitmap>) -> Unit) {
        fetchImage("$baseUrl/image/$id", callback)
    }

    fun getAllImages(callback: (Result<Bitmap>) -> Unit) {
        fetchImage("$baseUrl/image", callback)
    }
}

