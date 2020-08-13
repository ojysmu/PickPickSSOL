package mbtinder.server.io.notification

import mbtinder.lib.constant.NotificationForm
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import mbtinder.server.io.socket.SocketServer
import mbtinder.server.util.UserUtil

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

    private val notifications = IDList<NotificationForm>()

    init {
        loop = {
            val index = waitForConnection()
            val notification = notifications.removeAt(index)
            println("NotificationServer.loop(): " +
                    "receiver=${notification.receiverId}, " +
                    "title=${notification.title}, " +
                    "content=${notification.content}")
            SocketServer.getInstance().getConnection(notification.receiverId).sendNotification(notification)
        }
    }

    /**
     * 알림 추가
     *
     * @param form: 전송할 알림의 제목과 내용이 포함된 형식
     * @return 추가 성공 여부
     */
    fun addNotification(form: NotificationForm) =
        if (!UserUtil.getUser(form.receiverId)!!.notification) {
            false
        } else {
            sync(notifications) { notifications.add(form) }
        }

    /**
     * 알림 대기
     *
     * @return 연결중인 token의 index
     */
    private fun waitForConnection(): Int {
        var index: Int = -1
        block(notifications, 500) {
            notifications.isEmpty()
                    || SocketServer.getInstance().getConnectionCount() == 0
                    || SocketServer.getInstance().containsConnections(notifications.map { it.receiverId }).also { index = it } == -1
        }

        return index
    }
}