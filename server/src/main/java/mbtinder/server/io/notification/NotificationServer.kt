package mbtinder.server.io.notification

import mbtinder.lib.constant.Notification
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import mbtinder.server.io.socket.SocketServer

class NotificationServer private constructor(): CloseableThread() {
    companion object {
        private var instance: NotificationServer? = null

        fun getInstance(): NotificationServer {
            if (instance == null) {
                throw RuntimeException("Instance never initialized")
            }

            return instance!!
        }

        fun createInstance(): NotificationServer {
            if (instance == null) {
                instance = NotificationServer()

                return instance!!
            } else {
                throw RuntimeException("Instance already initialized")
            }
        }
    }

    private val notifications = IDList<Notification>()

    init {
        loop = {
            // 전송할 알림이 비었거나, 연결이 없거나, 전송가능한 연결이 없을 때 대기
            waitForConnection()

            val notification = sync(notifications) { it.removeAt(0) }
            // 연결 수립 여부 확인
            if (SocketServer.getInstance().isAlive(notification.receiverId)) {
                // 연결되어 있다면 전송
                val receiverConnection = SocketServer.getInstance().getConnection(notification.receiverId)
                receiverConnection.sendNotification(notification)
            } else {
                // 연결되어있지 않다면 맨 뒤로 보냄
                notifications.add(notification)
            }
        }
    }

    fun addNotification(notification: Notification) {
        notifications.add(notification)
    }

    private fun waitForConnection() {
        block(notifications, intervalInMillis) {
            notifications.isEmpty() ||
                    SocketServer.getInstance().getConnectionCount() == 0 ||
                    SocketServer.getInstance().containsConnections(notifications.map { it.receiverId })
        }
    }
}