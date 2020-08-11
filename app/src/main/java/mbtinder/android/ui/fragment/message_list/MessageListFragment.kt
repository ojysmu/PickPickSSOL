package mbtinder.android.ui.fragment.message_list

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_message_list.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ThreadUtil
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.toIDList

class MessageListFragment: Fragment(R.layout.fragment_message_list) {
    private lateinit var chatAdapter: ChatAdapter

    override fun initializeView() {
        message_list_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        message_list_recycler_view.itemAnimator = DefaultItemAnimator()

        ThreadUtil.runOnBackground {
            if (updateLastMessages()) {
                ThreadUtil.runOnUiThread {
                    chatAdapter = ChatAdapter(this, lastMessages!!)
                    message_list_recycler_view.adapter = chatAdapter
                    message_list_recycler_view.visibility = View.VISIBLE
                    message_list_progress_bar.visibility = View.INVISIBLE
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

    companion object {
        var lastMessages: IDList<MessageContent>? = null
    }
}