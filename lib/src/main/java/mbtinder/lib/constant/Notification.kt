package mbtinder.lib.constant

import mbtinder.lib.component.IDContent
import java.util.*

enum class Notification(val notificationId: UUID): IDContent {
    MESSAGE_RECEIVED(UUID.fromString("c0e967d6-330b-4b31-b29d-21e0c121e954"));

    lateinit var receiverId: UUID
    lateinit var title: String
    lateinit var content: String

    override fun getUUID() = notificationId

    companion object {
        private val notifications = values()

        fun findNotification(name: String) = notifications.find {
            it.name == name
        }
    }
}