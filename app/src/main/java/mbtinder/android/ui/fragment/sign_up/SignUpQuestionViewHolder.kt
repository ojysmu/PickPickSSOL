package mbtinder.android.ui.fragment.sign_up

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.SignUpQuestionContent

class SignUpQuestionViewHolder(itemView: View) : AdaptableViewHolder<SignUpQuestionContent>(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.card_sign_up4_question_title)
    private val selectableGroup: ChipGroup = itemView.findViewById(R.id.card_sign_up5_question_selector)

    @SuppressLint("InflateParams")
    override fun adapt(content: SignUpQuestionContent) {
        titleTextView.text = content.question
        (0 until content.selectable.length()).forEach {
            selectableGroup.addView(getChip().apply {
                id = it
                text = content.selectable.getString(it)
            })
        }

        selectableGroup.setOnCheckedChangeListener { group, checkedId ->
            group.children.forEach {
                (it as Chip).chipStrokeWidth = 0F
            }
            group.findViewById<Chip>(checkedId)?.chipStrokeWidth = ViewUtil.dp2px(itemView.context, 4)
        }
    }

    fun getCheckItemPosition() = selectableGroup.checkedChipId

    @SuppressLint("InflateParams")
    private fun getChip() = LayoutInflater.from(itemView.context).inflate(R.layout.chip_sign_up4_question, null) as Chip
}