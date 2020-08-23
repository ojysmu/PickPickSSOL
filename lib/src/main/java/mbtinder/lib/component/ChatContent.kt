package mbtinder.lib.component

import mbtinder.lib.component.database.Row
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class ChatContent: JSONParsable, IDContent, Comparable<ChatContent> {
    lateinit var chatId: UUID
    lateinit var opponentId: UUID
    lateinit var opponentName: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(chatId: UUID, opponentId: UUID, opponentName: String) {
        this.chatId = chatId
        this.opponentId = opponentId
        this.opponentName = opponentName

        updateJSONObject()
    }

    constructor(row: Row) {
        this.chatId = row.getUUID("chat_id")
        this.opponentId = row.getUUID("opponent_id")
        this.opponentName = row.getString("opponent_name")

        updateJSONObject()
    }

    fun getCreateSql() = "CREATE TABLE '$chatId' (" +
            "_id         INTEGER PRIMARY KEY AUTOINCREMENT , " +
            "sender_id   CHAR(36) NOT NULL , " +
            "receiver_id CHAR(36) NOT NULL , " +
            "timestamp   BIGINT NOT NULL , " +
            "body        VARCHAR(200) NOT NULL)"

    fun getInsertSql() = "INSERT INTO chat (" +
            "chat_id, receiver_id, receiver_name) VALUES (" +
            "'${chatId}', '${opponentId}', '${opponentName}')"

    override fun getUUID() = chatId

    override fun compareTo(other: ChatContent) = getUUID().compareTo(other.getUUID())
}