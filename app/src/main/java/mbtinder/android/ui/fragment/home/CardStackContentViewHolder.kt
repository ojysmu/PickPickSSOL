package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder

class CardStackContentViewHolder(view: View): AdaptableViewHolder<String>(view) {
    var isDark: Boolean = true
    private val context = view.context
    private val cardView: MaterialCardView = view.findViewById(R.id.card_main_stack_content)
    private val contentTextView: TextView = view.findViewById(R.id.card_main_stack_content_body)

    override fun adapt(content: String) {
        contentTextView.text = content

        if (!isDark) {
            cardView.strokeColor = context.getColor(android.R.color.black)
            contentTextView.setTextColor(context.getColor(android.R.color.black))
        }
    }
}