package mbtinder.android.ui.fragment.message_list

import android.text.Editable
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_message_list.*
import mbtinder.android.R
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import java.util.*

class MessageListFragment: Fragment(R.layout.fragment_message_list) {
    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE

        initializeFocusableEditText(message_list_search, this::onSearchChanged)

        message_list_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        message_list_recycler_view.itemAnimator = DefaultItemAnimator()

        runOnBackground { updateLastMessages() }
    }

    private fun updateLastMessages() {
        lastMessages = CommandProcess.getLastMessages()

        onLastMessageUpdated()
    }

    private fun onLastMessageUpdated() {
        aliveAdapter = ChatAdapter(this, lastMessages!!)

        runOnUiThread {
            message_list_recycler_view?.adapter = aliveAdapter
            message_list_recycler_view?.visibility = View.VISIBLE
            message_list_progress_bar?.visibility = View.INVISIBLE
        }
    }

    private fun onSearchChanged(editable: Editable?) {
        editable?.let {
            if (editable.isNotBlank()) {
                aliveAdapter!!.updateFilter(editable.toString())
            } else {
                aliveAdapter!!.updateContents(lastMessages!!)
            }
        }
    }

    companion object {
        var lastMessages: IDList<MessageContent>? = null
        private var aliveAdapter: ChatAdapter? = null

        fun setLastMessage(messageContent: MessageContent) {
            lastMessages?.let { messages ->
                val found = lastMessages!!.find { it.chatId == messageContent.chatId }
                if (found != null) {
                    messages.remove(found)
                    messages.add(messageContent)
                    messages.sort()
                    messages.reverse()
                    aliveAdapter?.notifyDataSetChanged()
                } else {
                    messages.add(messageContent)
                    messages.sort()
                    messages.reverse()
                    aliveAdapter?.notifyItemInserted(lastMessages!!.size - 1)
                }
            }
        }

        fun deleteLastMessage(chatId: UUID) {
            lastMessages?.let {
                it.find { messageContent -> messageContent.chatId == chatId }
                    ?.let { messageContent -> it.remove(messageContent) }
            }
            aliveAdapter?.notifyDataSetChanged()
        }
    }
}