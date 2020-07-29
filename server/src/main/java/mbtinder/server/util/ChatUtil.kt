package mbtinder.server.util

import mbtinder.lib.component.ChatContent
import mbtinder.lib.util.IDList
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row
import java.util.*

object ChatUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private lateinit var chats: IDList<ChatContent>
    private var lastUpdate: Long = 0

    private fun updateChats() {
        val sql = "SELECT * FROM mbtinder.chat"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        chats = queryResult.content.mapTo(IDList()) { buildChat(it) }
    }

    private fun ensureUpdate() {
        if (lastUpdate < System.currentTimeMillis() - UPDATE_DURATION) {
            updateChats()
        }
    }

    private fun buildChat(row: Row) = ChatContent(
        row.getUUID("chat_id"), row.getUUID("sender_id"), row.getUUID("receiver_id")
    )

    fun getChatContent(chatId: UUID): ChatContent? = synchronized(chats) {
        ensureUpdate()

        val chatContent = chats.find { it.chatId == chatId }
        return if (chatContent != null) {
            chatContent
        } else {
            updateChats()
            chats.find { it.chatId == chatId }
        }
    }
}