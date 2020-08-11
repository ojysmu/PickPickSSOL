package mbtinder.android.ui.fragment.chat

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ThreadUtil
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.toIDList
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val chatId by lazy { UUID.fromString(requireArguments().getString("chat_id")) }
    private lateinit var adapter: MessageAdapter

    override fun initializeView() {
        chat_send.isEnabled = false

        chat_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        chat_recycler_view.itemAnimator = DefaultItemAnimator()

        ThreadUtil.runOnBackground {
            val result = updateMessages()
            if (result) {
                ThreadUtil.runOnUiThread {
                    adapter = MessageAdapter(messages[chatId])

                    chat_send.isEnabled = true
                    chat_recycler_view.adapter = adapter
                    chat_recycler_view.visibility = View.VISIBLE
                    chat_progress_bar.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun updateMessages(): Boolean {
        if (messages.contains(chatId)) {
            val messages = messages[chatId]
            val refreshResult = CommandProcess.refreshMessage(
                StaticComponent.user.userId,
                chatId,
                messages.last().timestamp
            )

            if (refreshResult.isSucceed) {
                messages.addAll(refreshResult.result!!)
            }

            return refreshResult.isSucceed
        } else {
            val getResult = CommandProcess.getMessages(chatId)
            if (getResult.isSucceed) {
                messages.add(getResult.result!!.toIDList())
            }

            return getResult.isSucceed
        }
    }

    companion object {
        // Chat ID 단위로 구별되는 cache
        private val messages = IDList<IDList<MessageContent>>()

        fun addMessage(messageContent: MessageContent) {
            if (messages.contains(messageContent.chatId)) {
                messages[messageContent.chatId].add(messageContent)
            } else {
                val newChat = IDList<MessageContent>()
                newChat.uuid = messageContent.chatId
                newChat.add(messageContent)
                messages.add(newChat)
            }
        }
    }
}