package com.example.ims.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import com.example.ims.Path
import com.example.ims.data.Commands
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class PathApi {
    private val baseUrl = "http://16.16.68.202"

    private val maxChecks = 5

    private suspend fun fetchPath(urlString: String): Map<String, List<String>> = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val map = mutableMapOf<String, List<String>>()

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        try {
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArray = JSONTokener(response).nextValue() as JSONArray

                for (i in 0 until jsonArray.length()) {
                    val positionId = jsonArray.getJSONObject(i).getString("positionId")
                    val x = jsonArray.getJSONObject(i).getString("x")
                    val y = jsonArray.getJSONObject(i).getString("y")
                    val collisionOccurred = jsonArray.getJSONObject(i).getString("collision_occured")

                    map[positionId] = listOf(x, y, collisionOccurred)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        map
    }

    suspend fun getPathById(): Map<String, List<String>>? {
        val checkIntervalMillis = 200L
        val id = 1//getPathId()

        val urlString = "$baseUrl/paths/$id"
        val markers = mutableMapOf<String, List<String>>()

        val pathData = fetchPath(urlString)

        for ((positionId, valueList) in pathData) {
            //checks to see if positionId already exists in markers, only adds new positionId
            if (!markers.containsKey(positionId)) {
                markers[positionId] = valueList
            }
        }

        //if new data, reset checks and return markers
        if (markers.isNotEmpty()) {
            println("Markers: $markers")
            return markers
        }
        //check for new data every 0.2 sec
        delay(checkIntervalMillis)

        //if markers is empty(no new data), return null
        return null
    }

    //get the latest pathId
    private suspend fun getPathId(): Int = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/paths")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        var latestPathId = 0
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val currentTime = System.currentTimeMillis()
        val fiveSecondsAgo = currentTime - 5_000 // 5 seconds in milliseconds

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArray = JSONTokener(response).nextValue() as JSONArray

                for (i in 0 until jsonArray.length()) {
                    val pathId = jsonArray.getJSONObject(i).getInt("pathId")
                    val startTime = jsonArray.getJSONObject(i).getString("start_time")

                    val startTimeMillis = dateFormat.parse(startTime)?.time ?: continue

                    // Extract the date and time components from the current time
                    val currentDate = dateFormat.format(Date(currentTime))
                    val currentDateObj = dateFormat.parse(currentDate)

                    // Extract the date and time components from the start time
                    val startDate = dateFormat.format(Date(startTimeMillis))
                    val startDateObj = dateFormat.parse(startDate)

                    if (startDateObj != null && currentDateObj != null) {
                        //check if currentDateObj is < startDateObj
                        if (startDateObj.after(currentDateObj)) {
                            latestPathId = pathId
                            break
                        }
                        //check if both dates are equal
                        else if (startDateObj == currentDateObj) {
                            //check for startTimeMillis 5 sec earlier or within 5 sec of currentTime
                            //OR check if currentTime is <= startTimeMillis
                            if (startTimeMillis in fiveSecondsAgo..currentTime || currentTime <= startTimeMillis) {
                                latestPathId = pathId
                                break
                            }
                        }
                        //check if startDateObj is < CurrentDateObj
                        else {
                            //add a full day as milliseconds to currentTime
                            val adjustedCurrentTime = currentTime + 24 * 60 * 60 * 1000

                            //subtract startTimeMillis with currentTime
                            val timeDifference = adjustedCurrentTime - startTimeMillis

                            //check if timeDifference is <= 5 sec
                            if (timeDifference <= 5_000) {
                                latestPathId = pathId
                                break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        latestPathId
    }
    private fun fetchPaths(urlString: String, callback: (JSONArray) -> Unit) {
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

                    val jsonArray = JSONTokener(response).nextValue() as JSONArray

                    callback(jsonArray)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(JSONArray())
            }
        }.start()
    }
    fun getAllPaths( callback: (ArrayList<Path>) -> Unit) {
        val arrayList: ArrayList<Path> = ArrayList()
        fetchPaths("$baseUrl/paths/",){
            for (i in 0 until it.length()) {
                val path = it.getJSONObject(i)
                val pathId = path.getString("pathId")
                val startTime = path.getString("start_time")
                val endTime = path.getString("end_time")
                val newPath = Path(pathId,endTime,startTime)
                arrayList.add(newPath)
            }
            callback(arrayList)
        }
    }
    fun sendManualCommand(command: Commands, callback: (Int) -> Unit) {
        val url = URL("$baseUrl/command")
        var commandStr = ""
        when (command) {
            Commands.M_MANUEL -> {commandStr = "manual"}
            Commands.M_AUTO -> {commandStr = "autonomous"}
            Commands.M_OFF -> {commandStr = "turn_off"}
        }

        Thread {
            try {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

                val jsonObject = JSONObject()
                jsonObject.accumulate("command", commandStr)
                setPostRequestContent(connection,jsonObject)
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    reader.close()

                    callback(HttpURLConnection.HTTP_OK)
                }

                connection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                callback(e.hashCode())
            }
        }.start()
    }
    @Throws(IOException::class)
    private fun setPostRequestContent(conn: HttpURLConnection, jsonObject: JSONObject) {

        val os = conn.outputStream
        val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
        writer.write(jsonObject.toString())
        //Log.i(MainActivity::class.java.toString(), jsonObject.toString())
        writer.flush()
        writer.close()
        os.close()
    }
}