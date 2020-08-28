package mbtinder.server.io.notification

import mbtinder.lib.constant.NotificationForm
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import mbtinder.server.io.socket.SocketServer
import mbtinder.server.util.UserUtil
import java.util.*

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
            val token = waitForConnection()
            val notification = notifications.remove(token)
            println("NotificationServer.loop(): " +
                    "receiver=${notification.receiverId}, " +
                    "title=${notification.title}, " +
                    "content=${notification.content}")
            SocketServer.getInstance().getConnection(notification.receiverId).sendNotification(notification)
        }
    }

    /**
     * 알림 추가. 알림 수신 상대방의 연결이 없다면 무시.
     *
     * @param form: 전송할 알림의 제목과 내용이 포함된 형식
     * @return 추가 성공 여부
     */
    fun addNotification(form: NotificationForm) =
        // 알림을 수신받는 사용자인지 확인
        if (!UserUtil.getUser(form.receiverId)!!.notification) {
            false
        } else {
            // 수신자가 연결되어있는지 확인
            if (SocketServer.getInstance().hasConnection(form.receiverId)) {
                notifications.add(form)
            } else {
                false
            }
        }

    /**
     * 알림 대기
     *
     * @return 연결중인 token의 index
     */
    private fun waitForConnection(): UUID {
        var token: UUID? = null
        block(notifications, 500) {
            notifications.isEmpty()
                    || SocketServer.getInstance().getConnectionCount() == 0
                    || SocketServer.getInstance().containsConnections(notifications.map { it.receiverId }).also { token = it } == null
        }

        return token!!
    }
}