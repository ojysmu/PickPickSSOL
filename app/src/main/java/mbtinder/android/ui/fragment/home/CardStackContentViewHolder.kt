package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.TextView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder

class CardStackContentViewHolder(view: View): AdaptableViewHolder<String>(view) {
    var contentTextView: TextView = view.findViewById(R.id.card_main_stack_content)

    override fun adapt(content: String) {
        contentTextView.text = content
    }
}