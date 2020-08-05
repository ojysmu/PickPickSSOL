package mbtinder.android.ui.fragment.sign_up

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.lib.component.MBTIContent

class MBTIViewHolder(itemView: View) : AdaptableViewHolder<MBTIContent>(itemView) {
    lateinit var adapter: MBTIAdapter
    lateinit var content: MBTIContent

    private val cardView: MaterialCardView = itemView.findViewById(R.id.card_sign_up4_mbti)
    private val titleTextView: TextView = itemView.findViewById(R.id.card_sign_up4_mbti_title)
    private val contentTextView: TextView = itemView.findViewById(R.id.card_sign_up4_mbti_content)
    private val checkImageView: ImageView = itemView.findViewById(R.id.card_sign_up4_mbti_check)

    override fun adapt(content: MBTIContent) {
        this.content = content

        titleTextView.text = content.title
        contentTextView.text = content.content
        if (content.isChecked) {
            check()
        } else {
            uncheck()
        }

        cardView.setOnClickListener {
            adapter.uncheckAll()
            check()
        }
    }

    private fun check() {
        checkImageView.visibility = View.VISIBLE
        cardView.strokeColor = itemView.context.getColor(R.color.colorPrimary)
        content.isChecked = true
    }

    fun uncheck() {
        checkImageView.visibility = View.GONE
        cardView.strokeColor = itemView.context.getColor(android.R.color.transparent)
        content.isChecked = false
    }
}