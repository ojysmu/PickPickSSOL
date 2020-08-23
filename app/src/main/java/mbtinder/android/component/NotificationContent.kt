package mbtinder.android.component

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.component.NotificationContent.Companion.makeNotification
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.ui.fragment.chat.ChatFragment
import mbtinder.android.ui.fragment.message_list.MessageListFragment
import mbtinder.android.util.putUUID
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.IDContent
import mbtinder.lib.component.MessageContent
import mbtinder.lib.constant.Notification
import mbtinder.lib.util.getUUID
import mbtinder.lib.util.idListOf
import org.json.JSONObject

val notifications = idListOf(
    NotificationContent(Notification.MATCHED) { context, title, content, extra ->
//        val pendingIntent = NavDeepLinkBuilder(context)
//            .setGraph(R.navigation.navigation_main)
//            .setDestination(R.id.action_to_message_list)
//            .createPendingIntent()

        val builder = makeNotification(
            context = context,
            icon = R.mipmap.ic_launcher,
            title = title,
            content = content,
            priority = NotificationCompat.PRIORITY_HIGH,
//            contentIntent = pendingIntent,
            contentIntent = null,
            autoCancel = true,
            importance = NotificationManager.IMPORTANCE_HIGH
        )
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(0, builder.build())

        extra?.let {
            val chatContent = ChatContent(extra.getJSONObject("chat_content"))
            val messageContent = MessageContent(extra.getJSONObject("message_content"))
            SQLiteConnection.getInstance().executeUpdate(chatContent.getCreateSql())
            SQLiteConnection.getInstance().executeUpdate(chatContent.getInsertSql())
            SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())
        }
    },
    NotificationContent(Notification.MESSAGE_RECEIVED) { context, title, content, extra ->
        val arguments = Bundle()
        arguments.putUUID("chat_id", extra!!.getUUID("chat_id"))

        runOnUiThread {
//            val pendingIntent = NavDeepLinkBuilder(context.applicationContext)
//                .setComponentName(MainActivity::class.java)
//                .setArguments(arguments)
//                .setDestination(R.id.action_to_home)
//                .setGraph(R.navigation.navigation_main)
//                .createPendingIntent()

            val messageContent = MessageContent(extra)
            // 현재 채팅방이 실행중일 경우 알림을 보내지 않음
            if (ChatFragment.addMessage(messageContent)) {
                val builder = makeNotification(
                    context = context,
                    icon = R.mipmap.ic_launcher,
                    title = title,
                    content = content,
                    priority = NotificationCompat.PRIORITY_HIGH,
//                    contentIntent = pendingIntent,
                    contentIntent = null,
                    autoCancel = true,
                    importance = NotificationManager.IMPORTANCE_HIGH
                )
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(0, builder.build())
            }
            // 채팅 목록 마지막 채팅 갱신
            SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())
            MessageListFragment.setLastMessage(messageContent)
        }
    },
    NotificationContent(Notification.BLOCKED) { _, _, _, extra ->
        val chatId = extra!!.getUUID("chat_id")
        MessageListFragment.deleteLastMessage(chatId)
        if (ChatFragment.isAlive(chatId)) {
            ChatFragment.deleteMessage(chatId)
            runOnUiThread { ChatFragment.getFragment().findNavController().popBackStack() }
        }
    }
)

data class NotificationContent(val notification: Notification,
                               val perform: (context: Context, title: String, content: String, extra: JSONObject?) -> Unit): IDContent {
    override fun getUUID() = notification.notificationId

    companion object {
        private const val CHANNEL_ID = "pickpick_channel_id"
        private const val CHANNEL_NAME = "pickpick_channel_name"

        fun makeNotification(
            context: Context,
            @DrawableRes icon: Int,
            title: CharSequence,
            content: CharSequence,
            priority: Int,
            contentIntent: PendingIntent?,
            autoCancel: Boolean,
            importance: Int
        ): NotificationCompat.Builder {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, icon))
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(priority)
                .setContentIntent(contentIntent)
                .setAutoCancel(autoCancel)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(icon)
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
                (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
            } else {
                builder.setSmallIcon(icon)
            }

            return builder
        }
    }
}