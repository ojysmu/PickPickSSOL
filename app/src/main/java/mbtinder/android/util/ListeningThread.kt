package mbtinder.android.util

import mbtinder.android.component.CommandResult
import mbtinder.android.component.notifications
import mbtinder.android.io.SocketClient
import mbtinder.lib.util.CloseableThread
import org.json.JSONException
import org.json.JSONObject
import java.io.DataInputStream
import java.io.IOException
import java.util.*

internal class ListeningThread(dataInputStream: DataInputStream,
                               onDisconnected: (IOException) -> Unit): CloseableThread() {
    init {
        loop = {
            try {
                val response = JSONObject(dataInputStream.readUTF())
                if (isNotificationMessage(response)) {
                    val id = UUID.fromString(response.getString("notification_id"))
                    val title = response.getString("title")
                    val content = response.getString("content")

                    notifications[id].perform(title, content)
                } else {
                    Log.v(response.toString())
                    SocketClient.getInstance().addResult(CommandResult(response))
                }
            } catch (e: IOException) {
                stopThread()
                onDisconnected(e)
                SocketClient.getInstance().stopThread()
            }
        }
    }

    private fun isNotificationMessage(response: JSONObject) = try {
        response.getBoolean("is_notification")
    } catch (e: JSONException) {
        false
    }
}