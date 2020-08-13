package mbtinder.android.io.socket

import android.content.Context
import mbtinder.android.component.CommandResult
import mbtinder.android.util.Log
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import org.json.JSONObject
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.util.*

class SocketClient private constructor(private val address: String, private val port: Int, private val context: Context)
    : CloseableThread() {
    companion object {
        const val CONNECTION_RETRY_MAX = 3

        private var instance: SocketClient? = null

        fun createInstance(address: String, port: Int, context: Context): SocketClient {
            if (instance == null) {
                instance =
                    SocketClient(address, port, context)

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

        fun releaseInstance() {
            instance = null
        }

        fun isAlive() = instance != null
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

                listeningThread =
                    ListeningThread(
                        context,
                        dataInputStream,
                        onDisconnected ?: {})
                listeningThread.start()
            } catch (e: IOException) {
                stopThread()
                onConnectionFailed?.let { it(e) }
            }
        }

        loop = {
            if (commandPool.isEmpty()) {
                sleep()
            } else {
//                val command = sync(commandPool, commandPool::removeAt, 0)
                val command = sync(commandPool) { it.removeAt(0) }
                val clientMessage = command.jsonObject.toString()
                Log.v("clientMessage=$clientMessage")

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
        instance = null
    }

    fun addCommand(commandContent: CommandContent) = sync(commandPool) { it.add(commandContent) }

    fun addResult(commandResult: CommandResult) = sync(resultPool) { it.add(commandResult) }

    fun getResult(commandId: UUID): JSONObject {
        block(resultPool, intervalInMillis) { !it.contains(commandId) }

        return sync(resultPool) { it.remove(commandId) }.arguments
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