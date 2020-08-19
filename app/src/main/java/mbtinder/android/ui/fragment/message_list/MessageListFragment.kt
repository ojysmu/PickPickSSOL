package mbtinder.android.ui.fragment.message_list

import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_message_list.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.toIDList
import java.util.*

class MessageListFragment: Fragment(R.layout.fragment_message_list) {
    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE

        initializeFocusableEditText(message_list_search, this::onSearchChanged)

        message_list_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        message_list_recycler_view.itemAnimator = DefaultItemAnimator()

        runOnBackground {
            Log.v("MessageListFragment.initializeView(): 1")
            if (updateLastMessages()) {
                Log.v("MessageListFragment.initializeView(): 2-1")
                aliveAdapter = ChatAdapter(this, lastMessages!!)

                runOnUiThread {
                    Log.v("MessageListFragment.initializeView(): 3-1")
                    message_list_recycler_view.adapter = aliveAdapter
                    message_list_recycler_view.visibility = View.VISIBLE
                    message_list_progress_bar.visibility = View.INVISIBLE
                }
            } else {
                Log.v("MessageListFragment.initializeView(): 2-2")
                runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to load last messages.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateLastMessages(): Boolean {
        val getResult = CommandProcess.getLastMessages(StaticComponent.user.userId)

        if (getResult.isSucceed) {
            lastMessages = getResult.result!!.sorted().toIDList()
        }

        return getResult.isSucceed
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
            if (lastMessages != null) {
                val found = lastMessages!!.find { it.chatId == messageContent.chatId }
                if (found != null) {
                    lastMessages!!.remove(found)
                    lastMessages!!.add(messageContent)
                    aliveAdapter?.notifyDataSetChanged()
                } else {
                    lastMessages!!.add(messageContent)
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