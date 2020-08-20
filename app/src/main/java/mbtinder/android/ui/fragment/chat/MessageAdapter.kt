package mbtinder.android.ui.fragment.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.util.Log
import mbtinder.lib.component.MessageContent
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

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
        val content = contents[position]
        val (h, m) = getTimestamp(content.timestamp)
        holder.contentTextView.text = addNewLine(content.body)
        holder.timestampTextView.text = context.getString(R.string.chat_timestamp_format, h, m)
    }

    private fun bindOpponent(holder: MessageOpponentViewHolder, position: Int) {
        val content = contents[position]
        val (h, m) = getTimestamp(content.timestamp)
        StaticComponent.setUserImage(content.senderId, holder.opponentImageView)
        holder.contentTextView.text = addNewLine(content.body)
        holder.timestampTextView.text = context.getString(R.string.chat_timestamp_format, h, m)
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

    private fun getTimestamp(timestamp: Long): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
    }

    fun getChatId() = contents[0].chatId

    companion object {
        private const val TYPE_USER = R.layout.card_chat_user
        private const val TYPE_OPPONENT = R.layout.card_chat_opponent
    }
}