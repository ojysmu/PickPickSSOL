package mbtinder.lib.constant

import mbtinder.lib.component.IDContent
import org.json.JSONObject
import java.util.*

enum class Notification(val notificationId: UUID): IDContent {
    MATCHED(UUID.fromString("c0e967d6-330b-4b31-b29d-21e0c121e953")),
    MESSAGE_RECEIVED(UUID.fromString("c0e967d6-330b-4b31-b29d-21e0c121e954")),
    BLOCKED(UUID.fromString("c0e967d6-330b-4b31-b29d-21e0c121e954"));

    override fun getUUID() = notificationId

    companion object {
        private val notifications = values()

        fun findNotification(name: String) = notifications.find {
            it.name == name
        }
    }
}

data class NotificationForm(
    val notification: Notification,
    val receiverId: UUID,
    val title: String,
    val content: String,
    val extra: JSONObject? = null
): IDContent {
    override fun getUUID() = notification.getUUID()
}