package mbtinder.android.io

import mbtinder.android.component.CommandResult
import mbtinder.android.util.ListeningThread
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.util.*

class SocketClient(private val address: String, private val port: Int): CloseableThread() {
    companion object {
        private var instance: SocketClient? = null

        fun createInstance(address: String, port: Int): SocketClient {
            if (instance == null) {
                instance = SocketClient(address, port)

                return instance!!
            } else {
                throw RuntimeException("SocketClient is already initialized")
            }
        }

        fun getInstance(): SocketClient {
            if (instance != null) {
                return instance!!
            } else {
                throw RuntimeException("SocketClient is not initialized")
            }
        }
    }

    private lateinit var socket: Socket
    private lateinit var dataInputStream: DataInputStream
    private lateinit var dataOutputStream: DataOutputStream
    private lateinit var listeningThread: ListeningThread

    private val commandPool = IDList<CommandContent>()
    private val resultPool = IDList<CommandResult>()

    var onConnected: (() -> Unit)? = null
    var onConnectionFailed: ((e: IOException) -> Unit)? = null
    var onDisconnected: ((e: IOException) -> Unit)? = null

    init {
        prefix = {
            try {
                socket = Socket(address, port)
                dataInputStream = DataInputStream(socket.getInputStream())
                dataOutputStream = DataOutputStream(socket.getOutputStream())

                onConnected?.let { it() }
            } catch (e: IOException) {
                stopThread()
                onConnectionFailed?.let { it(e) }
            }

            listeningThread = ListeningThread(dataInputStream, onDisconnected ?: {})
            listeningThread.start()
        }

        loop = {
            if (commandPool.isEmpty()) {
                sleep()
            } else {
                val command = commandPool.removeAt(0)
                val clientMessage = command.jsonObject.toString()

                try {
                    dataOutputStream.writeUTF(clientMessage)
                    dataOutputStream.flush()
                } catch (e: IOException) {
                    stopThread()
                    close()
                    onDisconnected?.let { it(e) }
                }
            }
        }
    }

    override fun close() {
        super.close()

        listeningThread.close()
        dataInputStream.close()
        dataOutputStream.close()
        socket.close()
    }

    fun addCommand(commandContent: CommandContent) = synchronized(commandPool) {
        commandPool.add(commandContent)
    }

    fun addResult(commandResult: CommandResult) = synchronized(resultPool) {
        resultPool.add(commandResult)
    }

    fun getResult(commandId: UUID): JSONObject {
        while (!resultPool.contains(commandId)) {
            sleep()
        }

        return synchronized(resultPool) {
            resultPool.remove(commandId).arguments
        }
    }

    override fun pauseThread() {
        super.pauseThread()

        listeningThread.pauseThread()
    }

    override fun resumeThread() {
        super.resumeThread()

        listeningThread.resumeThread()
    }
}