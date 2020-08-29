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
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.fragment.message_list.MessageListFragment
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.DialogFactory
import mbtinder.android.util.getUUID
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private val chatId by lazy { requireArguments().getUUID("chat_id")!! }
    private val opponentId by lazy { requireArguments().getUUID("opponent_id")!! }
    private val opponentName by lazy { requireArguments().getString("opponent_name")!! }

    private val messages = IDList<MessageContent>()

    override fun initializeView() {
        fragment = this
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        chat_title.text = opponentName
        chat_send.isEnabled = false
        chat_recycler_view.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        chat_recycler_view.itemAnimator = DefaultItemAnimator()

        runOnBackground { updateMessages() }

        chat_block.setOnClickListener { onBlockClicked() }
        chat_send.setOnClickListener { onSendClicked() }
        chat_back.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onStop() {
        super.onStop()

        aliveAdapter = null
        fragment = null
    }

    private fun onBlockClicked() {
        DialogFactory.getContentedDialog(requireContext(), R.string.chat_block_alert, onPositive = {
            val waitDialog = DialogFactory.getWaitDialog(requireContext())
            waitDialog.show()
            runOnBackground {
                CommandProcess.blockUser(StaticComponent.user.userId, opponentId, chatId)
                SQLiteConnection.getInstance().executeUpdate("DELETE FROM chat where chat_id='$chatId'")
                SQLiteConnection.getInstance().executeUpdate("DROP TABLE '$chatId'")
                runOnUiThread {
                    MessageListFragment.deleteLastMessage(chatId)
                    waitDialog.dismiss()
                    findNavController().popBackStack()
                }
            }
        }).show()
    }

    private fun onSendClicked() {
        val body = chat_content.text.toString()
        if (body.isBlank()) {
            return
        } else {
            chat_content.setText("")
        }

        runOnBackground {
            val result = CommandProcess.sendMessage(
                chatId = chatId,
                senderId = StaticComponent.user.userId,
                receiverId = opponentId,
                opponentName = opponentName,
                body = body
            )

            if (result.isSucceed) {
                val messageContent = MessageContent(
                    chatId = chatId,
                    senderId = StaticComponent.user.userId,
                    receiverId = opponentId,
                    opponentName = opponentName,
                    timestamp = result.result!!,
                    body = body
                )

                SQLiteConnection.getInstance().executeUpdate(messageContent.getLocalInsertMessageSql())

                runOnUiThread {
                    aliveAdapter?.addContent(messageContent)
                    chat_recycler_view.scrollToPosition(aliveAdapter?.getLastIndex() ?: 0)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(requireContext(), R.string.chat_failed_to_send, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateMessages() {
        messages.addAll(CommandProcess.getMessages(chatId, opponentName))
        runOnUiThread(this::onMessageUpdated)
    }

    private fun onMessageUpdated() {
        aliveAdapter = MessageAdapter(chat_recycler_view, messages)
        aliveAdapter!!.setHasStableIds(true)

        chat_send.isEnabled = true
        chat_recycler_view.adapter = aliveAdapter!!
        chat_recycler_view.scrollToPosition(aliveAdapter!!.getLastIndex())
        chat_recycler_view.visibility = View.VISIBLE
        chat_progress_bar.visibility = View.INVISIBLE
    }

    companion object {
        // Chat ID 단위로 구별되는 cache
        private var aliveAdapter: MessageAdapter? = null
        private var fragment: ChatFragment? = null

        fun addMessage(messageContent: MessageContent): Boolean {
            aliveAdapter?.let {
                it.addContent(messageContent)
                it.recyclerView.scrollToPosition(it.getLastIndex())
            }

            return aliveAdapter == null
        }

        fun isAlive(chatId: UUID) = aliveAdapter?.getChatId() == chatId

        fun getFragment() = fragment!!
    }
}