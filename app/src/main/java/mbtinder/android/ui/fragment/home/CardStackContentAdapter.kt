package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.android.ui.model.Adapter

class CardStackContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var contentTextView: TextView = view.findViewById(R.id.card_main_stack_content)
}

class CardStackContentAdapter(private val contents: MutableList<String>): Adapter<String>(R.layout.card_main_stack_content, contents, CardStackContentViewHolder::class.java) {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is CardStackContentViewHolder) {
            error("Assertion failed")
        }

        holder.contentTextView.text = contents[position]
    }
}