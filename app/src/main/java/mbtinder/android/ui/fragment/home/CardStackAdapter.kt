package mbtinder.android.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent

class CardStackAdapter(private val fragment: HomeFragment): RecyclerView.Adapter<BaseCardStackViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseCardStackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when (viewType) {
            TYPE_CARD_STACK_CONTENT -> CardStackViewHolder(view)
            TYPE_DAILY_QUESTION_CONTENT -> DailyQuestionViewHolder(view)
            TYPE_TUTORIAL -> TutorialViewHolder(view)
            TYPE_EMPTY_CONTENT -> EmptyViewHolder(view, fragment)
            else -> throw AssertionError("View type error: viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseCardStackViewHolder, position: Int) {
        when (holder) {
            is CardStackViewHolder -> holder.bind(HomeFragment.visibleContents[position] as CardStackContent)
            is DailyQuestionViewHolder -> holder.bind(HomeFragment.visibleContents[position] as DailyQuestionContent)
            is TutorialViewHolder -> {}
            is EmptyViewHolder -> holder.bind()
        }
    }

    override fun getItemCount() = HomeFragment.visibleContents.size

    override fun getItemViewType(position: Int): Int {
        return when (HomeFragment.visibleContents[position]) {
            is CardStackContent -> TYPE_CARD_STACK_CONTENT
            is DailyQuestionContent -> TYPE_DAILY_QUESTION_CONTENT
            is TutorialContent -> TYPE_TUTORIAL
            is EmptyContent -> TYPE_EMPTY_CONTENT
            else -> throw AssertionError("View type error: position=$position")
        }
    }

    override fun getItemId(position: Int): Long {
        val uuid = HomeFragment.visibleContents[position].getUUID()
        return uuid.leastSignificantBits + uuid.mostSignificantBits
    }

    companion object {
        const val TYPE_CARD_STACK_CONTENT = R.layout.card_home_stack
        const val TYPE_DAILY_QUESTION_CONTENT = R.layout.card_home_daily_question
        const val TYPE_TUTORIAL = R.layout.card_home_tutorial
        const val TYPE_EMPTY_CONTENT = R.layout.card_home_empty
    }
}