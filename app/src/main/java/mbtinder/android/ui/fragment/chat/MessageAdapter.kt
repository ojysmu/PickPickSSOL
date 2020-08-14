package mbtinder.android.ui.fragment.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.util.Log
import mbtinder.lib.component.MessageContent

class MessageAdapter(val recyclerView: RecyclerView, private val contents: ArrayList<MessageContent>) : RecyclerView.Adapter<MessageViewHolder>() {
    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        context = parent.context

        val view = LayoutInflater.from(context).inflate(viewType, parent, false)

        return if (viewType == R.layout.card_chat_user) {
            MessageUserViewHolder(view)
        } else {
            MessageOpponentViewHolder(view)
        }
    }

    override fun getItemCount() = contents.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        if (holder is MessageUserViewHolder) {
            bindUser(holder, position)
        } else {
            bindOpponent(holder as MessageOpponentViewHolder, position)
        }
    }

    override fun getItemViewType(position: Int) =
        if (contents[position].senderId == StaticComponent.user.userId) { TYPE_USER } else { TYPE_OPPONENT }

    fun addContent(messageContent: MessageContent) {
        contents.add(messageContent)
        notifyItemInserted(contents.size - 1)
    }

    fun getLastIndex() = contents.size - 1

    private fun bindUser(holder: MessageUserViewHolder, position: Int) {
        holder.contentTextView.text = addNewLine(contents[position].body)
    }

    private fun bindOpponent(holder: MessageOpponentViewHolder, position: Int) {
        val content = contents[position]
        holder.opponentImageView.setImage(StaticComponent.getUserImage(content.senderId))
        holder.contentTextView.text = addNewLine(content.body)
    }

    private fun addNewLine(body: String, width: Int = 20): String {
        var original = StringBuilder(body)
        val added = StringBuilder()

        while (original.length > width) {
            val formatted = String.format("%.${width}s", original.toString()).trim()
            added.append(formatted).append('\n')
            original = original.delete(0, width)
        }

        added.append(original)
        return added.toString()
    }

    companion object {
        private const val TYPE_USER = R.layout.card_chat_user
        private const val TYPE_OPPONENT = R.layout.card_chat_opponent
    }
}