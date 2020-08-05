package mbtinder.android.ui.fragment.sign_up

import android.view.View
import android.widget.TextView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.lib.component.MBTIContent

class MBTIViewHolder(itemView: View) : AdaptableViewHolder<MBTIContent>(itemView) {
    val titleTextView: TextView = itemView.findViewById(R.id.card_sign_up4_mbti_title)
    val contentTextView: TextView = itemView.findViewById(R.id.card_sign_up4_mbti_content)

    override fun adapt(content: MBTIContent) {
        this.titleTextView.text = content.title
        this.contentTextView.text = content.content
    }
}