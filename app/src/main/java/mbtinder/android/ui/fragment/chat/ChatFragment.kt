package mbtinder.android.ui.fragment.chat

import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_chat.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ViewUtil
import mbtinder.android.util.getUUID
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.toIDList
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val chatId by lazy { requireArguments().getUUID("chat_id")!! }
    private val opponentId by lazy { requireArguments().getUUID("opponent_id")!! }
    private val opponentName by lazy { requireArguments().getString("opponent_name")!! }

    private lateinit var adapter: MessageAdapter

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        chat_send.isEnabled = false

        chat_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        chat_recycler_view.itemAnimator = DefaultItemAnimator()

        runOnBackground {
            val result = updateMessages()
            if (result) {
                runOnUiThread {
                    adapter = MessageAdapter(messages[chatId])

                    chat_send.isEnabled = true
                    chat_recycler_view.adapter = adapter
                    chat_recycler_view.visibility = View.VISIBLE
                    chat_progress_bar.visibility = View.INVISIBLE
                }
            }
        }

        chat_send.setOnClickListener {
            val body = chat_content.text.toString()
            if (body.isBlank()) {
                return@setOnClickListener
            } else {
                chat_content.setText("")
            }

            val (isSucceed, result) = runOnBackground<Pair<Boolean, Long?>> {
                val result = CommandProcess.sendMessage(
                    chatId = chatId,
                    senderId = StaticComponent.user.userId,
                    receiverId = opponentId,
                    opponentName = opponentName,
                    body = body
                )
                Pair(result.isSucceed, result.result)
            }

            if (isSucceed) {
                val content = MessageContent(
                    chatId = chatId,
                    senderId = StaticComponent.user.userId,
                    receiverId = opponentId,
                    opponentName = opponentName,
                    timestamp = result!!,
                    body = body
                )
                messages[chatId].add(content)
                adapter.addContent(content)
            } else {
                Toast.makeText(requireContext(), R.string.chat_failed_to_send, Toast.LENGTH_SHORT).show()
            }
        }

        chat_back.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updateMessages(): Boolean {
        if (messages.contains(chatId)) {
            val messages = messages[chatId]
            val refreshResult = CommandProcess.refreshMessage(
                userId = StaticComponent.user.userId,
                chatId = chatId,
                lastTimestamp = messages.last().timestamp
            )

            if (refreshResult.isSucceed) {
                messages.addAll(refreshResult.result!!)
            }

            return refreshResult.isSucceed
        } else {
            val getResult = CommandProcess.getMessages(StaticComponent.user.userId, chatId)
            if (getResult.isSucceed) {
                messages.add(getResult.result!!.toIDList().apply { uuid = chatId })
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