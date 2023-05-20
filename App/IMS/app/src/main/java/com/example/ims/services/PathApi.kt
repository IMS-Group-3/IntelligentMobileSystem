import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import com.example.ims.Path
import org.json.JSONArray
import org.json.JSONTokener

class PathApi {
    private val baseUrl = "http://16.16.68.202"
    private val pathesUrl = "http://16.16.68.202"


    private suspend fun fetchPath(urlString: String): Map<String, List<String>> = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        var map = mutableMapOf<String, List<String>>()

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

    suspend fun getPathById(id: Int): Map<String, List<String>> {
        val urlString = "$baseUrl/paths/$id"
        return fetchPath(urlString)
    }
    private fun fetchPaths(urlString: String, callback: (JSONArray) -> Unit) {
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
        fetchPaths("http://16.16.68.202/paths/",){
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
}