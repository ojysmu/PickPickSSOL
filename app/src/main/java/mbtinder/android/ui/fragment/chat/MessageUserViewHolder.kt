package mbtinder.android.ui.fragment.chat

import android.view.View
import android.widget.TextView
import mbtinder.android.R

class MessageUserViewHolder(itemView: View) : MessageViewHolder(itemView) {
    val contentTextView: TextView = itemView.findViewById(R.id.card_chat_content)
}