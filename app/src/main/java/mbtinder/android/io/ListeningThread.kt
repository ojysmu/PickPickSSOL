package mbtinder.android.io

import android.content.Context
import mbtinder.android.component.CommandResult
import mbtinder.android.component.notifications
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
                val response = JSONObject(dataInputStream.readUTF())
                Log.v("ListeningThread: $response")
                if (isNotificationMessage(response)) {
                    val id = UUID.fromString(response.getString("notification_id"))
                    val title = response.getString("title")
                    val content = response.getString("content")

                    notifications[id].perform(context, title, content)
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
}