package com.auctech.siprint.initials

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FetchCountryDataTask(private val listener: OnDataFetchedListener) : AsyncTask<Void, Void, String>() {

    interface OnDataFetchedListener {
        fun onDataFetched(jsonData: String?)
        fun onError(errorMessage: String)
    }

    override fun doInBackground(vararg params: Void?): String? {
        var result: String? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://gist.githubusercontent.com/anubhavshrimal/75f6183458db8c453306f93521e93d37/raw/f77e7598a8503f1f70528ae1cbf9f66755698a16/CountryCodes.json")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
                result = stringBuilder.toString()
                reader.close()
                inputStream.close()
            } else {
                listener.onError("HTTP Error Code: $responseCode")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            listener.onError("Exception: ${e.message}")
        } finally {
            connection?.disconnect()
        }
        print(result)
        return result
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        listener.onDataFetched(result)
    }
}