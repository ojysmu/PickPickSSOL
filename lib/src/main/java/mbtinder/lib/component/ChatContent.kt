package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class ChatContent: JSONParsable, IDContent, Comparable<ChatContent>, CloneableContent<ChatContent> {
    lateinit var chatId: UUID
    lateinit var participant1: UUID
    lateinit var participant2: UUID

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(chatId: UUID, participant1: UUID, participant2: UUID) {
        this.chatId = chatId
        this.participant1 = participant1
        this.participant2 = participant2

        updateJSONObject()
    }

    override fun getUUID() = chatId

    override fun compareTo(other: ChatContent) = chatId.compareTo(other.chatId)

    override fun clone() = ChatContent(chatId, participant1, participant2)
}