package mbtinder.server.util

import mbtinder.lib.component.MessageContent
import mbtinder.server.io.database.component.Row
import java.util.*

object MessageUtil {
    fun buildMessage(row: Row, chatId: UUID) =
        MessageContent(
            chatId = chatId,
            senderId = row.getUUID("sender_id"),
            receiverId = row.getUUID("receiver_id"),
            timestamp = row.getLong("timestamp"),
            body = row.getString("body")
        )
}