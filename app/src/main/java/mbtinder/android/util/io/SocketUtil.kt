package mbtinder.android.util.io

import mbtinder.android.io.SocketClient
import mbtinder.lib.io.component.CommandContent
import org.json.JSONObject
import java.util.*

object SocketUtil {
    fun getServerResult(command: String, arguments: JSONObject): JSONObject {
        val commandId = UUID.randomUUID()
        val client = SocketClient.getInstance()
        client.addCommand(CommandContent(commandId, command, arguments))
        return client.getResult(commandId)
    }
}