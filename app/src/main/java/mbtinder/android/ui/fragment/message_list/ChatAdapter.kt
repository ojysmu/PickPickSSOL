package mbtinder.android.ui.fragment.message_list

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.MessageContent

class ChatAdapter(private val contents: MutableList<MessageContent>)
    : Adapter<MessageContent>(R.layout.card_chat_list, contents, ChatViewHolder::class.java) {

    override fun onBindViewHolder(holder: AdaptableViewHolder<MessageContent>, position: Int) {
        holder.adapt(contents[position])
    }
}