package mbtinder.android.io.socket

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.core.app.NotificationCompat.*
import androidx.navigation.NavDeepLinkBuilder
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.NotificationContent.Companion.makeNotification
import mbtinder.android.ui.fragment.chat.ChatFragment
import mbtinder.android.util.putUUID
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.IDContent
import mbtinder.lib.constant.Notification
import mbtinder.lib.util.idListOf
import org.json.JSONObject

private fun Context.getNotificationManager(): NotificationManager {
    return getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}

/**
 * 알림 수신 시 이벤트
 */
val notifications = idListOf(
    NotificationContent(Notification.MATCHED) { context, title, content, extra ->
        NotificationProcess.onMatchReceived(extra)
        if (StaticComponent.user.notification) {
            val pendingIntent = NavDeepLinkBuilder(context)
                .setGraph(R.navigation.navigation_main)
                .setDestination(R.id.nav_message_list) // 메시지 목록으로 이동
                .createPendingIntent()
            val notification = makeNotification(
                context = context,
                title = title,
                content = content,
                contentIntent = pendingIntent
            )
            context.getNotificationManager().notify(0, notification)
        }
    },
    NotificationContent(Notification.MESSAGE_RECEIVED) { context, title, content, extra ->
        val messageContent = NotificationProcess.onMessageReceived(extra!!)

        val arguments = Bundle()
        arguments.putUUID("chat_id", messageContent.chatId)
        arguments.putUUID("opponent_id", messageContent.senderId)
        arguments.putString("opponent_name", messageContent.opponentName)

        runOnUiThread {
            val pendingIntent = NavDeepLinkBuilder(context.applicationContext)
                .setArguments(arguments)
                .setDestination(R.id.nav_chat) // 채팅방으로 이동
                .setGraph(R.navigation.navigation_main)
                .setArguments(arguments)
                .createPendingIntent()
            if (StaticComponent.user.notification) {
                if (ChatFragment.addMessage(messageContent)) {
                    val notification = makeNotification(
                        context = context,
                        title = title,
                        content = content,
                        contentIntent = pendingIntent
                    )
                    context.getNotificationManager().notify(0, notification)
                }
            } else {
                ChatFragment.addMessage(messageContent)
            }
        }
    },
    NotificationContent(Notification.BLOCKED) { _, _, _, extra -> NotificationProcess.onBlockReceived(extra!!) },
    NotificationContent(Notification.CHAT_REMOVED) { _, _, _, extra -> NotificationProcess.onChatRemoveReceived(extra!!) }
//    NotificationContent(Notification.SOCKET_CLOSED) { _, _, _, _ -> NotificationProcess.onSocketClosed() }
)

data class NotificationContent(
    val notification: Notification,
    val perform: (context: Context, title: String, content: String, extra: JSONObject?) -> Unit
): IDContent {
    override fun getUUID() = notification.notificationId

    companion object {
        private const val CHANNEL_ID = "PickPick"
        private const val CHANNEL_NAME = "PickPick"

        fun makeNotification(
            context: Context,
            title: CharSequence,
            content: CharSequence,
            contentIntent: PendingIntent
        ): android.app.Notification {
            val builder = Builder(context, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)

            builder.setSmallIcon(R.mipmap.ic_launcher)
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            context.getNotificationManager().createNotificationChannel(channel)

            val notification = builder.build()
            notification.flags = FLAG_ACTIVITY_SINGLE_TOP or FLAG_ONGOING_EVENT or FLAG_AUTO_CANCEL

            return notification
        }
    }
}