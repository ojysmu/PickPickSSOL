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
//            println("NotificationServer.loop(): Waiting...")
            val index = waitForConnection()
//            println("NotificationServer.loop(): Found")
//            val notification = sync(notifications) { it.removeAt(index) }
            val notification = notifications.removeAt(index)
            println("NotificationServer.loop(): " +
                    "receiver=${notification.receiverId}, " +
                    "title=${notification.title}, " +
                    "content=${notification.content}")
            SocketServer.getInstance().getConnection(notification.receiverId).sendNotification(notification)
//            // 연결 수립 여부 확인
//            if (SocketServer.getInstance().isAlive(notification.receiverId)) {
////                println("NotificationServer.loop(): Alive")
//                // 연결되어 있다면 전송
//                SocketServer.getInstance().getConnection(notification.receiverId).sendNotification(notification)
////                println("NotificationServer.loop(): Sent")
//            } else {
//                // 연결되어있지 않다면 맨 뒤로 보냄
////                println("NotificationServer.loop(): Disconnected")
//                notifications.add(notification)
//            }
        }
    }

    fun addNotification(notification: Notification): Boolean {
        print("NotificationServer.addNotification(): " +
                "receiver=${notification.receiverId}, " +
                "title=${notification.title}, " +
                "content=${notification.content}")
        val result = sync(notifications) { notifications.add(notification) }
        println(", added")
        return result
    }

    private fun waitForConnection(): Int {
        var index: Int = -1
        block(notifications, 500) {
            print("Blocked by: ")
            notifications.isEmpty().also { if (it) print(1) }
                    || (SocketServer.getInstance().getConnectionCount() == 0).also { if (it) print(2) }
                    || (SocketServer.getInstance().containsConnections(notifications.map { it.receiverId }).also { index = it } == -1).also { if (it) print(3) }
        }

        return index
    }
}