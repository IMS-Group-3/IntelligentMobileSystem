package com.example.ims.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.util.Base64
import org.json.JSONObject

class ImageApi {
    private val baseUrl = "http://16.16.68.202"

    // Return an image bytearray
    private fun fetchImage(urlString: String, callback: (Result<Pair<String, ByteArray>>) -> Unit) {
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

                    // Decodes the image data and callback the byte array
                    val imageData = JSONObject(response)
                    val encodedImage = imageData.getString("encodedImage")
                    val imageClassification = imageData.getString("image_classification")
                    val decodedImage = Base64.decode(encodedImage, Base64.DEFAULT)
                    callback(Result.success(Pair(imageClassification, decodedImage)))
                } else {
                    callback(Result.failure(Exception("Error: $responseCode")))
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(Result.failure(e))
            }
        }.start()
    }

    // This needs to be updated with the endpoint for fetching the image from the positionId
    fun getImageByteArrayById(id: Int, callback: (Result<Pair<String, ByteArray>>) -> Unit) {
        fetchImage("$baseUrl/image/position/$id", callback)
    }
    fun getImageBitmapByID(imageId: Int, onSuccess: (Result<Pair<String, Bitmap>>) -> Unit, onFailure: () -> Unit) {
        getImageByteArrayById(imageId) { result ->
            if (result.isSuccess) {
                val imageResult = result.getOrNull()

                val imageClassification = imageResult?.first
                val imageByteArray = imageResult?.second

                val bitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray!!.size)

                onSuccess(Result.success(Pair(imageClassification, bitmap)) as Result<Pair<String, Bitmap>>)
            } else if (result.isFailure) {
                onFailure()
            }
        }
    }

    fun getAllImagesByteArray(callback: (Result<Pair<String, ByteArray>>) -> Unit) {
        fetchImage("$baseUrl/image", callback)
    }
}

