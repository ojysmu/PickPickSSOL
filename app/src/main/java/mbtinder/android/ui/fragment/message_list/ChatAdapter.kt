package mbtinder.android.ui.fragment.message_list

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.android.util.putUUID
import mbtinder.lib.component.MessageContent
import java.util.*

class ChatAdapter(private val fragment: MessageListFragment, private val contents: MutableList<MessageContent>)
    : Adapter<MessageContent>(R.layout.card_chat_list, contents, ChatViewHolder::class.java) {


    override fun onBindViewHolder(holder: AdaptableViewHolder<MessageContent>, position: Int) {
        (holder as ChatViewHolder).adapter = this
        holder.bind(contents[position])
        holder.setIsRecyclable(false)
    }

    fun requestNavigate(chatId: UUID, opponentId: UUID, opponentName: String) {
        val arguments = Bundle()
        arguments.putUUID("chat_id", chatId)
        arguments.putUUID("opponent_id", opponentId)
        arguments.putString("opponent_name", opponentName)

        fragment.findNavController().navigate(R.id.action_to_chat, arguments)
    }

    fun updateFilter(string: String) {
        updateContents(contents.filter { it.opponentName.contains(string) }.toMutableList())
    }
}