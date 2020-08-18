package mbtinder.android.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent

class CardStackAdapter(private val fragment: HomeFragment): RecyclerView.Adapter<BaseCardStackViewHolder>() {
    val holders = arrayListOf<BaseCardStackViewHolder>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseCardStackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when (viewType) {
            TYPE_CARD_STACK_CONTENT -> CardStackViewHolder(view)
            TYPE_DAILY_QUESTION_CONTENT -> DailyQuestionViewHolder(view)
            TYPE_EMPTY_CONTENT -> EmptyViewHolder(view, fragment)
            else -> throw AssertionError("View type error: viewType=$viewType")
        }.apply { setIsRecyclable(false) }
    }

    override fun onBindViewHolder(holder: BaseCardStackViewHolder, position: Int) {
        holders.add(holder)
        holder.adapt(HomeFragment.cardStackContents[position])
    }

    override fun getItemCount() = HomeFragment.cardStackContents.size

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) {
            return TYPE_EMPTY_CONTENT
        }

        return when (HomeFragment.cardStackContents[position]) {
            is CardStackContent -> TYPE_CARD_STACK_CONTENT
            is DailyQuestionContent -> TYPE_DAILY_QUESTION_CONTENT
            else -> throw AssertionError("View type error: position=$position")
        }
    }

    override fun getItemId(position: Int): Long {
        val uuid = HomeFragment.cardStackContents[position].getUUID()
        return uuid.leastSignificantBits + uuid.mostSignificantBits
    }

    fun getUserIds() = HomeFragment.cardStackContents.map { it.getUUID() }

    fun getLeftContents(currentPosition: Int) = itemCount - currentPosition

    fun getUserId(position: Int) = (HomeFragment.cardStackContents[position] as CardStackContent).userId

    fun addAt(position: Int, content: BaseCardStackContent) {
        HomeFragment.cardStackContents.add(content)
        notifyItemInserted(position)
    }

    fun removeAt(position: Int) {
        HomeFragment.cardStackContents.removeAt(position)
        notifyItemRemoved(position)
    }

    companion object {
        const val TYPE_CARD_STACK_CONTENT = R.layout.card_main_stack
        const val TYPE_DAILY_QUESTION_CONTENT = R.layout.card_home_daily_question
        const val TYPE_EMPTY_CONTENT = R.layout.card_home_empty
    }
}