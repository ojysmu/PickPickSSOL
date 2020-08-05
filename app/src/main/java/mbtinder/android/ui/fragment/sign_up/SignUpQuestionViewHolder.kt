package mbtinder.android.ui.fragment.sign_up

import android.view.View
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.lib.component.SignUpQuestionContent

class SignUpQuestionViewHolder(itemView: View) :
    AdaptableViewHolder<SignUpQuestionContent>(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.card_sign_up4_question_title)
    private val selectableGroup: ChipGroup = itemView.findViewById(R.id.card_sign_up4_question_selector)

    override fun adapt(content: SignUpQuestionContent) {
        titleTextView.text = content.question
        (0 until content.selectable.length()).forEach {
            selectableGroup.addView(Chip(itemView.context).apply {
                id = it
                text = content.selectable.getString(it)
                isCheckable = true
            })
        }
    }

    fun getCheckItemPosition() = selectableGroup.checkedChipId
}