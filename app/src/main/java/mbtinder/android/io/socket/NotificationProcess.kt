package mbtinder.android.io.socket

import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.ui.fragment.message_list.MessageListFragment
import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.MessageContent
import org.json.JSONObject

object NotificationProcess {
    fun onMatchReceived(extra: JSONObject?) {
        extra?.let {
            val chatContent = ChatContent(extra.getJSONObject("chat_content"))
            val messageContent = MessageContent(extra.getJSONObject("message_content"))
            SQLiteConnection.getInstance().executeUpdate(chatContent.getCreateSql())
            SQLiteConnection.getInstance().executeUpdate(chatContent.getInsertSql())
            SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())
        }
    }

    fun onMessageReceived(extra: JSONObject): MessageContent {
        val messageContent = MessageContent(extra)

        SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())
        MessageListFragment.setLastMessage(messageContent)

        return messageContent
    }
}