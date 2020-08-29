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
    private val lastMessages = IDList<MessageContent>()

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE

        initializeFocusableEditText(message_list_search, this::onSearchChanged)

        message_list_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        message_list_recycler_view.itemAnimator = DefaultItemAnimator()

        runOnBackground { updateLastMessages() }
    }

    private fun updateLastMessages() {
        lastMessages.clear()
        lastMessages.addAll(CommandProcess.getLastMessages())

        onLastMessageUpdated()
    }

    private fun onLastMessageUpdated() {
        aliveAdapter = ChatAdapter(this, lastMessages)
        aliveAdapter!!.setHasStableIds(true)

        runOnUiThread {
            message_list_recycler_view?.adapter = aliveAdapter
            message_list_recycler_view?.visibility = View.VISIBLE
            message_list_progress_bar?.visibility = View.INVISIBLE
        }
    }

    private fun onSearchChanged(editable: Editable?) {
        editable?.let {
            aliveAdapter?.filter?.filter(it)
        }
    }

    override fun onStop() {
        super.onStop()

        aliveAdapter = null
    }

    companion object {
        private var aliveAdapter: ChatAdapter? = null

        fun setLastMessage(messageContent: MessageContent) {
            val opponentName = aliveAdapter?.getContent(messageContent.getUUID())?.opponentName
            opponentName?.let { messageContent.opponentName = opponentName }
            aliveAdapter?.addContent(messageContent) { it.timestamp }
        }

        fun deleteLastMessage(chatId: UUID): MessageContent? {
            return aliveAdapter?.removeContent(chatId)
        }
    }
}