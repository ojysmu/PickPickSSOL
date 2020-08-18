package mbtinder.android.ui.fragment.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent

class CardStackAdapter(val contents: MutableList<BaseCardStackContent>, private val fragment: HomeFragment): RecyclerView.Adapter<BaseCardStackViewHolder>() {
    val holders = arrayListOf<BaseCardStackViewHolder>()

    init {
        // 마지막 항목 추가
        contents.add(EmptyContent())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseCardStackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when (viewType) {
            TYPE_CARD_STACK_CONTENT -> CardStackViewHolder(view)
            TYPE_DAILY_QUESTION_CONTENT -> DailyQuestionViewHolder(view)
            TYPE_EMPTY_CONTENT -> EmptyViewHolder(view, fragment)
            else -> throw AssertionError("View type error: viewType=$viewType")
        }
    }

    override fun onBindViewHolder(holder: BaseCardStackViewHolder, position: Int) {
        holders.add(holder)
        holder.adapt(contents[position])
    }

    override fun getItemCount() = contents.size

    override fun getItemViewType(position: Int) = when (contents[position]) {
        is CardStackContent -> TYPE_CARD_STACK_CONTENT
        is DailyQuestionContent -> TYPE_DAILY_QUESTION_CONTENT
        is EmptyContent -> TYPE_EMPTY_CONTENT
        else -> throw AssertionError("View type error: position=$position")
    }

    /**
     * HomeFragment에서 새 카드 목록을 불러왔을 때 호출
     */
    fun addContents(list: List<BaseCardStackContent>) {
        val lastIndex = contents.size - 1
        // 마지막(EmptyContent) 삭제
        contents.removeAt(lastIndex)
        // 새 목록 추가
        contents.addAll(list)
        // 마지막(EmptyContent) 추가
        contents.add(EmptyContent())
        notifyItemRangeInserted(lastIndex, lastIndex + list.size)
    }

    /**
     * [CardStackContent]를 Pick하거나, [DailyQuestionContent]를 Pick, Nope했을 때 호출
     */
    fun removeContent(position: Int) {
        contents.removeAt(position)
        notifyItemRemoved(position)
    }

    /**
     * 남은 contents의 수 반환
     */
    fun getLeftContents(currentPosition: Int) = itemCount - currentPosition

    private companion object {
        const val TYPE_CARD_STACK_CONTENT = R.layout.card_main_stack
        const val TYPE_DAILY_QUESTION_CONTENT = R.layout.card_home_daily_question
        const val TYPE_EMPTY_CONTENT = R.layout.card_home_empty
    }
}