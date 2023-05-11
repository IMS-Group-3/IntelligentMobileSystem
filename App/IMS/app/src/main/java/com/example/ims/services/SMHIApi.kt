package com.example.ims.services


import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SMHIApi {

    private val getPointForecastJKPG = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/14.15618/lat/57.78145/data.json"

    fun fetchWeatherData(callback: (Result<String>) -> Unit) {
        val url = URL(getPointForecastJKPG)
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

                    // Log the entire response
                    Log.d("SMHIApi", "Response: $response")
                    callback(Result.success(response))
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
}