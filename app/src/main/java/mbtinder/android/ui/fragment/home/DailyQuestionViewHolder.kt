package mbtinder.android.ui.fragment.home

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent

class DailyQuestionViewHolder(itemView: View) : BaseCardStackViewHolder(itemView) {
    private val nopeImageView: ImageView = itemView.findViewById(R.id.card_home_daily_question_nope_image)
    private val nopeContainer: MaterialCardView = itemView.findViewById(R.id.card_main_stack_nope_container)
    private val nopeTextView: TextView = itemView.findViewById(R.id.card_home_daily_question_nope)
    private val pickImageView: ImageView = itemView.findViewById(R.id.card_home_daily_question_pick_image)
    private val pickContainer: MaterialCardView = itemView.findViewById(R.id.card_main_stack_pick_container)
    private val pickTextView: TextView = itemView.findViewById(R.id.card_home_daily_question_pick)

    private val colorPrimary = context.getColor(R.color.colorPrimary)
    private val colorWhite = context.getColor(android.R.color.white)
    private val colorBlack = context.getColor(android.R.color.black)

    fun bind(content: DailyQuestionContent) {
        nopeTextView.text = content.nopeContent
        pickTextView.text = content.pickContent
    }

    fun disableAll() {
        disableNope()
        disablePick()
    }

    fun enableNope() {
        nopeImageView.setColorFilter(colorPrimary)
        nopeContainer.foregroundTintList = ColorStateList.valueOf(colorPrimary)
        nopeTextView.setTextColor(colorWhite)
    }

    fun disableNope() {
        nopeImageView.setColorFilter(colorWhite)
        nopeContainer.foregroundTintList = ColorStateList.valueOf(colorWhite)
        nopeTextView.setTextColor(colorBlack)
    }

    fun enablePick() {
        pickImageView.setColorFilter(colorPrimary)
        pickContainer.foregroundTintList = ColorStateList.valueOf(colorPrimary)
        pickTextView.setTextColor(colorWhite)
    }

    fun disablePick() {
        pickImageView.setColorFilter(colorWhite)
        pickContainer.foregroundTintList = ColorStateList.valueOf(colorWhite)
        pickTextView.setTextColor(colorBlack)
    }
}