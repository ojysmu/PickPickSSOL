package mbtinder.android.ui.fragment.sign_up

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.lib.component.user.MBTIContent

class MBTIViewHolder(itemView: View) : AdaptableViewHolder<MBTIContent>(itemView) {
    lateinit var adapter: MBTIAdapter
    lateinit var content: MBTIContent

    private val cardView: MaterialCardView = itemView.findViewById(R.id.card_sign_up5_mbti)
    private val titleTextView: TextView = itemView.findViewById(R.id.card_sign_up5_mbti_title)
    private val contentTextView: TextView = itemView.findViewById(R.id.card_sign_up5_mbti_content)
    private val checkImageView: ImageView = itemView.findViewById(R.id.card_sign_up5_mbti_check)

    override fun bind(content: MBTIContent) {
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
        cardView.strokeColor = itemView.context.getColor(R.color.colorPrimary)
        titleTextView.setTextColor(itemView.context.getColor(R.color.colorPrimary))
        contentTextView.setTextColor(itemView.context.getColor(R.color.colorPrimary))
        checkImageView.visibility = View.VISIBLE
        content.isChecked = true
    }

    fun uncheck() {
        cardView.strokeColor = itemView.context.getColor(android.R.color.transparent)
        titleTextView.setTextColor(itemView.context.getColor(android.R.color.black))
        contentTextView.setTextColor(itemView.context.getColor(android.R.color.black))
        checkImageView.visibility = View.GONE
        content.isChecked = false
    }
}