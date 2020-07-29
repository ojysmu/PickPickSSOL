package mbtinder.lib.constant

import java.util.*

enum class Notification(val notificationId: UUID) {
    MESSAGE_RECEIVED(UUID.fromString("c0e967d6-330b-4b31-b29d-21e0c121e954"));

    companion object {
        private val notifications = values()

        fun findNotification(name: String) = notifications.find {
            it.name == name
        }
    }
}