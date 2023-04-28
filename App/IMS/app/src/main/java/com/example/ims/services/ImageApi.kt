package com.example.ims.services

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ImageApi {
    private val baseUrl = "http://16.16.68.202"

    fun getImageById(id: Int, callback: (Result<String>) -> Unit) {
        val url = URL("$baseUrl/image/$id")
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
                    callback(Result.success(response))
                } else {
                    callback(Result.failure(Exception("Error: $responseCode")))
                }

                connection.disconnect()
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }.start()
    }

    fun getAllImages(callback: (Result<String>) -> Unit) {
        val url = URL("$baseUrl/image")
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
                    callback(Result.success(response))
                } else {
                    callback(Result.failure(Exception("Error: $responseCode")))
                }

                connection.disconnect()
            } catch (e: Exception) {
                callback(Result.failure(e))
            }
        }.start()
    }

}
