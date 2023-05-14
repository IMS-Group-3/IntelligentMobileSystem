package com.example.ims.services

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import org.json.JSONArray
import org.json.JSONTokener

class PathApi {
    private val baseUrl = "http://16.16.68.202"

    private fun fetchPath(urlString: String, callback: (MutableMap<String, MutableList<String>>) -> Unit) {
        val url = URL(urlString)
        var map = mutableMapOf<String, MutableList<String>>()

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

                    val jsonArray = JSONTokener(response).nextValue() as JSONArray
                    for (i in 0 until jsonArray.length()) {

                        val positionId = jsonArray.getJSONObject(i).getString("positionId")
                        val x = jsonArray.getJSONObject(i).getString("x")
                        val y = jsonArray.getJSONObject(i).getString("y")
                        val timestamp = jsonArray.getJSONObject(i).getString("timestamp")
                        val collisionOccurred = jsonArray.getJSONObject(i).getString("collision_occured")

                        map[positionId] = mutableListOf(x, y, timestamp, collisionOccurred)
                    }
                    callback(map)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                val emptyMap = mutableMapOf<String, MutableList<String>>()
                callback(emptyMap)
            }
        }.start()
    }

    fun getPathById(id: Int, callback: (MutableMap<String, MutableList<String>>) -> Unit) {
        fetchPath("$baseUrl/paths/$id", callback)
    }

    fun getAllPositions(callback: (MutableMap<String, MutableList<String>>) -> Unit) {
        fetchPath("$baseUrl/paths", callback)
    }
}