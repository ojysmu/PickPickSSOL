package mbtinder.android.ui.fragment.chat

import android.view.View
import android.widget.TextView
import mbtinder.android.R
import mbtinder.android.ui.view.AsyncImageView

class MessageOpponentViewHolder(itemView: View) : MessageViewHolder(itemView) {
    val opponentImageView: AsyncImageView = itemView.findViewById(R.id.card_chat_opponent_image)
    val contentTextView: TextView = itemView.findViewById(R.id.card_chat_content)
}