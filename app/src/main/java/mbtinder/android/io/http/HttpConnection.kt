package mbtinder.android.io.http

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

enum class RequestMethod {
    POST, GET
}

class HttpConnection(private val rawUrl: String, private val requestMethod: RequestMethod,
                     private val isHttps: Boolean = rawUrl.startsWith("https://"),
                     private val requestParameters: List<Pair<String, Any>> = emptyList(),
                     private val contentType: String = "application/x-www-form-urlencoded"):
        AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg params: Void?): String {
        val url = if (requestMethod == RequestMethod.GET) {
            URL(getUrl())
        } else {
            URL(rawUrl)
        }

        val connection = if (isHttps) {
            url.openConnection() as HttpsURLConnection
        } else {
            url.openConnection() as HttpURLConnection
        }

        connection.defaultUseCaches = false
        connection.requestMethod = requestMethod.name
        connection.doInput = true

        if (requestMethod == RequestMethod.POST && requestParameters.isNotEmpty()) {
            setProperties(connection)
        } else {
            connection.doOutput = false
        }

        val responseCode = connection.responseCode
        val bufferedReader = BufferedReader(InputStreamReader(
            if (responseCode == 200) {
                connection.inputStream
            } else {
                connection.errorStream
            },
            "UTF-8"
        ))

        val resultBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        do {
            if (line == null) {
                break
            }
            resultBuilder.append(line).append('\n')
            line = bufferedReader.readLine()
        } while (true)

        bufferedReader.close()

        return resultBuilder.substring(0, resultBuilder.length - 1).toString()
    }

    private fun getUrl(): String {
        if (requestParameters.isEmpty()) {
            return rawUrl
        }

        val body = StringBuilder(rawUrl)
        body.append('?')
            .append(requestParameters[0].first)
            .append('=')
            .append(requestParameters[0].second.toString())

        if (requestParameters.size == 1) {
            return body.toString()
        }

        for (i in 1 until requestParameters.size) {
            body.append('&')
                .append(requestParameters[i].first)
                .append('=')
                .append(requestParameters[i].second.toString())
        }

        return body.toString()
    }

    private fun setProperties(connection: URLConnection) = requestParameters.forEach {
        connection.addRequestProperty(it.first, it.second.toString())
    }
}