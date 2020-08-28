package mbtinder.android.component

import mbtinder.android.io.database.RowContent
import mbtinder.lib.component.ChatContent
import org.sqldroid.SQLDroidResultSet
import java.util.*

class LocalChatContent(resultSet: SQLDroidResultSet): ChatContent(
    UUID.fromString(resultSet.getString("chat_id")),
    UUID.fromString(resultSet.getString("opponent_id")),
    resultSet.getString("opponent_name")
), RowContent {
    fun toChatContent() = this as ChatContent
}