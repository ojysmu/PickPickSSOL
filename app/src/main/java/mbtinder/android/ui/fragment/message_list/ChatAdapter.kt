package mbtinder.android.ui.fragment.message_list

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.FilterableAdapter
import mbtinder.android.util.putUUID
import mbtinder.lib.component.MessageContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.indexOf
import java.util.*

class ChatAdapter(private val fragment: MessageListFragment, contents: IDList<MessageContent>)
    : FilterableAdapter<MessageContent>(R.layout.card_chat_list, contents, ChatViewHolder::class.java) {

    init {
        filterBy = MessageContent::opponentName
    }

    override fun onBindViewHolder(holder: AdaptableViewHolder<MessageContent>, position: Int) {
        super.onBindViewHolder(holder, position)

        (holder as ChatViewHolder).adapter = this
        holder.setIsRecyclable(true)
    }

    fun requestNavigate(chatId: UUID, opponentId: UUID, opponentName: String) {
        val arguments = Bundle()
        arguments.putUUID("chat_id", chatId)
        arguments.putUUID("opponent_id", opponentId)
        arguments.putString("opponent_name", opponentName)

        fragment.findNavController().navigate(R.id.action_to_chat, arguments)
    }

    fun removeContent(chatId: UUID) = super.removeContent(getFilteredList().indexOf(chatId))
}