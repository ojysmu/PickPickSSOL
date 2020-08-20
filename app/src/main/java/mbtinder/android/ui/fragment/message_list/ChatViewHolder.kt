package mbtinder.android.ui.fragment.message_list

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.lib.component.MessageContent

class ChatViewHolder(itemView: View) : AdaptableViewHolder<MessageContent>(itemView) {
    lateinit var adapter: ChatAdapter

    private val cardView: ConstraintLayout = itemView.findViewById(R.id.card_chat_list)
    private val userImageView: AsyncImageView = itemView.findViewById(R.id.card_chat_list_image)
    private val userNameTextView: TextView = itemView.findViewById(R.id.card_chat_list_user_name)
    private val lastMessageTextView: TextView = itemView.findViewById(R.id.card_chat_list_last_message)

    override fun adapt(content: MessageContent) {
        val opponentId = content.getOpponentId(StaticComponent.user.userId)

        StaticComponent.setUserImage(opponentId, userImageView)
        userNameTextView.text = content.opponentName
        lastMessageTextView.text = content.body

        cardView.setOnClickListener { adapter.requestNavigate(content.chatId, opponentId, content.opponentName) }
    }
}