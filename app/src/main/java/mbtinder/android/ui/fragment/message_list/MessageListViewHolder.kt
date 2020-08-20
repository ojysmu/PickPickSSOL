package mbtinder.android.ui.fragment.message_list

import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R

class MessageListViewHolder(rootView: ViewGroup) {
    val chatTitleTextView: TextView = rootView.findViewById(R.id.chat_title)
    val sendButton: Button = rootView.findViewById(R.id.chat_send)
    val chatRecyclerView: RecyclerView = rootView.findViewById(R.id.chat_recycler_view)
}