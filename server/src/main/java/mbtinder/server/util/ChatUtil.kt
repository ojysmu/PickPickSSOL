package mbtinder.server.util

import mbtinder.lib.component.ChatContent
import mbtinder.lib.util.ImmutableList
import mbtinder.lib.util.toImmutableList
import mbtinder.server.io.database.MySQLServer
import mbtinder.lib.component.database.Row
import java.util.*

object ChatUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var chats: ImmutableList<ChatContent> = updateChats()
    private var lastUpdate: Long = 0

    private fun updateChats(): ImmutableList<ChatContent> {
        val sql = "SELECT * FROM mbtinder.chat"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        chats = queryResult.content.map { buildChat(it) }.sorted().toImmutableList()

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
        return findBinaryTwice(chats) { it.getUUID().compareTo(chatId) }
    }
}