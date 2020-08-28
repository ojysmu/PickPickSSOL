package mbtinder.android.component

import mbtinder.android.io.database.RowContent
import mbtinder.lib.component.MessageContent
import org.sqldroid.SQLDroidResultSet
import java.util.*

class LocalMessageContent(resultSet: SQLDroidResultSet) : MessageContent(
    UUID.randomUUID(),
    UUID.fromString(resultSet.getString("sender_id")),
    UUID.fromString(resultSet.getString("receiver_id")),
    "",
    resultSet.getLong("timestamp"),
    resultSet.getString("body")
), RowContent {
    fun toMessageContent() = this as MessageContent
}