package mbtinder.android.io.socket

import android.content.Context
import mbtinder.android.component.CommandResult
import mbtinder.android.util.Log
import mbtinder.lib.util.CloseableThread
import org.json.JSONException
import org.json.JSONObject
import java.io.DataInputStream
import java.io.IOException
import java.util.*

internal class ListeningThread(private val context: Context,
                               dataInputStream: DataInputStream,
                               onDisconnected: (IOException) -> Unit): CloseableThread() {
    init {
        loop = {
            try {
                Log.v("ListeningThread: Waiting...")
                val rawResponse = dataInputStream.readUTF()
                val response = JSONObject(rawResponse)
                Log.v("ListeningThread: $response")
                if (isNotificationMessage(response)) {
                    val id = UUID.fromString(response.getString("notification_id"))
                    val title = response.getString("title")
                    val content = response.getString("content")
                    val extra = hasNotificationExtra(response)

                    try {
                        notifications[id].perform(context, title, content, extra)
                    } catch (e: Exception) {
                        stopThread()
                    }
                } else {
                    SocketClient.getInstance().addResult(CommandResult(response))
                }
            } catch (e: IOException) {
                stopThread()
                onDisconnected(e)
            }
        }
    }

    private fun isNotificationMessage(response: JSONObject) = try {
        response.getBoolean("is_notification")
    } catch (e: JSONException) {
        false
    }

    private fun hasNotificationExtra(response: JSONObject) = try {
        response.getJSONObject("extra")
    } catch (e: JSONException) {
        null
    }
}