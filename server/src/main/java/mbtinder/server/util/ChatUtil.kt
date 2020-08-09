package mbtinder.server.util

import mbtinder.lib.component.ChatContent
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row
import java.util.*

object ChatUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var chats: List<ChatContent> = updateChats()
    private var lastUpdate: Long = 0

    private fun updateChats(): List<ChatContent> {
        val sql = "SELECT * FROM mbtinder.chat"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        chats = queryResult.content.map { buildChat(it) }.sorted()
        return chats
    }

    private fun ensureUpdate() {
        if (lastUpdate < System.currentTimeMillis() - UPDATE_DURATION) {
            updateChats()
        }
    }

    private fun buildChat(row: Row) = ChatContent(
        row.getUUID("chat_id"), row.getUUID("sender_id"), row.getUUID("receiver_id")
    )

    fun getChatContent(chatId: UUID): ChatContent? {
        ensureUpdate()
        return findBinaryTwice(chats, this::updateChats) { it.getUUID().compareTo(chatId) }
    }
}