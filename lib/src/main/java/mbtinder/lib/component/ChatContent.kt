package mbtinder.lib.component

import mbtinder.lib.component.database.Row
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class ChatContent: JSONParsable, IDContent, Comparable<ChatContent> {
    lateinit var chatId: UUID
    lateinit var opponentName: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(chatId: UUID, opponentName: String) {
        this.chatId = chatId
        this.opponentName = opponentName

        updateJSONObject()
    }

    constructor(row: Row) {
        this.chatId = row.getUUID("chat_id")
        this.opponentName = row.getString("opponent_name")

        updateJSONObject()
    }

    override fun getUUID() = chatId

    override fun compareTo(other: ChatContent) = getUUID().compareTo(other.getUUID())
}