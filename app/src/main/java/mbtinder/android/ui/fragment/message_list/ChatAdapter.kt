package mbtinder.android.ui.fragment.message_list

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.MessageContent
import java.util.*

class ChatAdapter(private val fragment: MessageListFragment, private val contents: MutableList<MessageContent>)
    : Adapter<MessageContent>(R.layout.card_chat_list, contents, ChatViewHolder::class.java) {


    override fun onBindViewHolder(holder: AdaptableViewHolder<MessageContent>, position: Int) {
        (holder as ChatViewHolder).adapter = this
        holder.adapt(contents[position])
    }

    fun requestNavigate(chatId: UUID) {
        val arguments = Bundle()
        arguments.putString("chat_id", chatId.toString())

        fragment.findNavController().navigate(R.id.action_to_chat, arguments)
    }

    fun updateFilter(string: String) {
        updateContents(contents.filter { it.opponentName.contains(string) }.toMutableList())
    }
}