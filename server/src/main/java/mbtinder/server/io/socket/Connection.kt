package mbtinder.server.io.socket

import mbtinder.lib.component.IDContent
import mbtinder.lib.constant.ServerResponse
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.CloseableThread
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.util.*

class Connection(private val socket: Socket): CloseableThread(), IDContent {
//    private val bufferedReader = BufferedReader(InputStreamReader(socket.inputStream))
//    private val bufferedWriter = BufferedWriter(OutputStreamWriter(socket.outputStream))
    private val dataInputStream = DataInputStream(socket.getInputStream())
    private val dataOutputStream = DataOutputStream(socket.getOutputStream())
    private var token = UUID.randomUUID()

    init {
        prefix = {
            val serverMessage = JSONObject().apply { put("value", 200) }
            send(serverMessage)
        }

        loop = {
//            val clientMessage = bufferedReader.readLine()
            val clientMessage = dataInputStream.readUTF()
            println("message=$clientMessage")

            val parsedMessage = JSONObject(clientMessage)
            if (parsedMessage.getString("name") == Command.CLOSE.name) {
                close()
            } else {
                val command = CommandContent(parsedMessage)
                val serverMessage = CommandProcess.onReceived(command)
                send(serverMessage)
            }
        }
    }

    private fun send(serverMessage: JSONObject) {
        println("message=$serverMessage")

//        bufferedWriter.write(serverMessage.toString() + '\n')
//        bufferedWriter.flush()
        dataOutputStream.writeUTF(serverMessage.toString())
        dataOutputStream.flush()
    }

    override fun getUUID(): UUID = token

    override fun close() {
        super.close()

//        bufferedReader.close()
//        bufferedWriter.close()
        dataInputStream.close()
        dataOutputStream.close()
        socket.close()

        SocketServer.getInstance().removeConnection(this)
    }

    companion object {
        fun makeServerResponse(uuid: UUID, arguments: JSONObject) = JSONObject().apply {
            put("uuid", uuid.toString())
            put("arguments", arguments)
        }

        fun makePositiveResponse(uuid: UUID) =
            makeServerResponse(
                uuid,
                JSONObject().apply {
                    put("result", true)
                })

        fun makePositiveResponse(uuid: UUID, arguments: JSONObject) =
            makeServerResponse(
                uuid,
                arguments.apply {
                    put("result", true)
                })

        fun makeNegativeResponse(uuid: UUID, code: Int) =
            makeNegativeResponse(
                uuid,
                code,
                JSONObject()
            )

        fun makeNegativeResponse(uuid: UUID, response: ServerResponse) =
            makeNegativeResponse(uuid, response.ordinal, JSONObject())

        fun makeNegativeResponse(uuid: UUID, code: Int, arguments: JSONObject) = makeServerResponse(
            uuid,
            arguments
        ).apply {
            put("result", false)
            put("code", code)
        }
    }
}