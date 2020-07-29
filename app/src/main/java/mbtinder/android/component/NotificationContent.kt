package mbtinder.android.component

import mbtinder.lib.component.IDContent
import mbtinder.lib.constant.Notification
import mbtinder.lib.util.IDList
import java.util.*

val notifications = IDList<NotificationContent>()

data class NotificationContent(val notification: Notification,
                               val perform: (title: String, content: String) -> Unit): IDContent {
    override fun getUUID(): UUID {
        return notification.notificationId
    }
}