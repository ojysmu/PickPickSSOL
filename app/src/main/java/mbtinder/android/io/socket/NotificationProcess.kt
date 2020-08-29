package mbtinder.android.io.socket

import androidx.navigation.fragment.findNavController
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.ui.fragment.chat.ChatFragment
import mbtinder.android.ui.fragment.message_list.MessageListFragment
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.getUUID
import org.json.JSONObject

object NotificationProcess {
    /**
     * 매칭되었을 때 이벤트 처리
     *
     * @param extra: 매칭된 상대방일 경우 채팅방 정보, 첫 메시지 정보를 인수로 받음
     */
    fun onMatchReceived(extra: JSONObject?) {
        extra?.let {
            val chatContent = ChatContent(extra.getJSONObject("chat_content"))
            val messageContent = MessageContent(extra.getJSONObject("message_content"))
            SQLiteConnection.getInstance().executeUpdate(chatContent.getCreateSql())
            SQLiteConnection.getInstance().executeUpdate(chatContent.getInsertSql())
            SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())

            runOnUiThread { MessageListFragment.setLastMessage(messageContent) }
        }
    }

    /**
     * 메시지가 수신되었을 때 이벤트 처리
     *
     * @param extra: 수신자 기준의 메시지 내용
     */
    fun onMessageReceived(extra: JSONObject): MessageContent {
        val messageContent = MessageContent(extra)

        SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())
        runOnUiThread { MessageListFragment.setLastMessage(messageContent) }

        return messageContent
    }

    /**
     * 다른 사용자로부터 차단되었을 때 이벤트 처리
     *
     * @param extra: 차단한 사용자와의 채팅방 ID
     */
    fun onBlockReceived(extra: JSONObject) {
        val chatId = extra.getUUID("chat_id")
        MessageListFragment.deleteLastMessage(chatId)?.let {
            SQLiteConnection.getInstance().executeUpdate("DELETE FROM chat where chat_id='$chatId'")
            SQLiteConnection.getInstance().executeUpdate("DROP TABLE '$chatId'")
        }
        if (ChatFragment.isAlive(chatId)) {
            runOnUiThread { ChatFragment.getFragment().findNavController().popBackStack() }
        }
    }

    /**
     * 다른 사용자가 탈퇴하였을 때 이벤트 처리
     *
     * @param extra: 탈퇴한 사용자와의 채팅방 ID
     */
    fun onChatRemoveReceived(extra: JSONObject) {
        val chatId = extra.getUUID("chat_id")
        MessageListFragment.deleteLastMessage(chatId)?.let {
            val userId = it.getOpponentId(StaticComponent.user.userId)
            SQLiteConnection.getInstance().executeUpdate("DELETE FROM block where opponent_id='$userId'")
            SQLiteConnection.getInstance().executeUpdate("DELETE FROM pick where opponent_id='$userId'")
            SQLiteConnection.getInstance().executeUpdate("DELETE FROM picked where opponent_id='$userId'")
            SQLiteConnection.getInstance().executeUpdate("DELETE FROM chat where chat_id='$chatId'")
            SQLiteConnection.getInstance().executeUpdate("DROP TABLE '$chatId'")
        }
        if (ChatFragment.isAlive(chatId)) {
            runOnUiThread { ChatFragment.getFragment().findNavController().popBackStack() }
        }
    }
}