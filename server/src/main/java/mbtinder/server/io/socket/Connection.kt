package mbtinder.server.io.socket

import mbtinder.lib.component.IDContent
import mbtinder.lib.constant.Notification
import mbtinder.lib.constant.ServerResponse
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.CloseableThread
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.Socket
import java.util.*

class Connection(private val socket: Socket): CloseableThread(), IDContent {
    private val dataInputStream = DataInputStream(socket.getInputStream())
    private val dataOutputStream = DataOutputStream(socket.getOutputStream())
    private var token = UUID.randomUUID()

    init {
        loop = {
            try {
                val clientMessage = dataInputStream.readUTF()
                println("message=$clientMessage")

                val parsedMessage = JSONObject(clientMessage)
                if (parsedMessage.getString("name") == Command.CLOSE.name) {
                    close()
                } else {
                    val command = CommandContent(parsedMessage)
                    val serverMessage = CommandProcess.onReceived(command, this)
                    send(serverMessage)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                SocketServer.getInstance().removeConnection(this)
            }
        }
    }

    fun setToken(token: UUID) {
        this.token = token
    }

    private fun send(serverMessage: JSONObject) {
        println("message=$serverMessage")

        dataOutputStream.writeUTF(serverMessage.toString())
        dataOutputStream.flush()
    }

    fun sendNotification(notification: Notification) {
        val serverMessage = JSONObject()
        serverMessage.put("is_notification", true)
        serverMessage.put("notification_id", notification.notificationId.toString())
        serverMessage.put("title", notification.title)
        serverMessage.put("content", notification.content)

        send(serverMessage)
    }

    override fun getUUID() = token

    override fun close() {
        super.close()

        dataInputStream.close()
        dataOutputStream.close()
        socket.close()

        SocketServer.getInstance().removeConnection(this)
    }

    companion object {
        private fun makeServerResponse(uuid: UUID, arguments: JSONObject) = JSONObject().apply {
            put("uuid", uuid.toString())
            put("arguments", arguments)
        }

        fun makePositiveResponse(uuid: UUID) =
            makeServerResponse(uuid, JSONObject().apply { put("result", true) })

        fun makePositiveResponse(uuid: UUID, arguments: JSONObject) =
            makeServerResponse(uuid, arguments.apply { put("result", true) })

        fun makeNegativeResponse(uuid: UUID, response: ServerResponse) =
            makeNegativeResponse(uuid, response.ordinal, JSONObject())

        fun makeNegativeResponse(uuid: UUID, code: Int, arguments: JSONObject) =
            makeServerResponse(uuid, arguments.apply {
                put("result", false)
                put("code", code)
            })
    }
}