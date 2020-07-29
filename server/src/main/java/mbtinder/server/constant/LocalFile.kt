package mbtinder.server.constant

import mbtinder.lib.component.ChatContent
import mbtinder.lib.util.loadJSONList
import java.util.*

object LocalFile {
    val fileRoot = "${System.getProperty("user.home")}/raw"
    val userRoot = "$fileRoot/user"

    val userChatList = "chat_list.json"

    fun getUserChatList(userId: UUID) = loadJSONList<ChatContent>("$userRoot/$userId/$userChatList")
    fun getUserImagePath(userId: UUID) = "$userRoot/$userId"
}