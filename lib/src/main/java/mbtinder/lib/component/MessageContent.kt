package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class MessageContent: JSONParsable, Comparable<MessageContent>, IDContent {
    lateinit var chatId: UUID
    lateinit var senderId: UUID
    lateinit var receiverId: UUID
    lateinit var opponentName: String
    var timestamp: Long = 0
    lateinit var body: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(chatId: UUID, senderId: UUID, receiverId: UUID, opponentName: String, timestamp: Long, body: String) {
        this.chatId = chatId
        this.senderId = senderId
        this.receiverId = receiverId
        this.opponentName = opponentName
        this.timestamp = timestamp
        this.body = body

        updateJSONObject()
    }

    fun getOpponentId(userId: UUID) = if (senderId == userId) {
        receiverId
    } else {
        senderId
    }

    override fun compareTo(other: MessageContent) = timestamp.compareTo(other.timestamp)

    fun getLocalInsertMessageSql() = "INSERT INTO $chatId (" +
                "sender_id,    receiver_Id,  timestamp,   body" +
                ") VALUES (" +
                "'$senderId', '$receiverId', $timestamp, '$body')"

    fun getServerInsertMessageSql(): String {
        return "INSERT INTO mbtinder.message (" +
                "chat_id,    sender_id,   receiver_id,  timestamp,   body" +
                ") VALUES (" +
                "'$chatId', '$senderId', '$receiverId', $timestamp, '$body')"
    }

    override fun getUUID() = chatId
}