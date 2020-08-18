package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.CardStackContent

class CardStackViewHolder(private val view: View): BaseCardStackViewHolder(view) {
    private var imageView: AsyncImageView = view.findViewById(R.id.card_main_stack_image)
    private var recyclerView: RecyclerView = view.findViewById(R.id.card_main_stack_recycler_view)
    private var pickContainer: FrameLayout = view.findViewById(R.id.card_main_stack_pick_container)
    private var cardPick: MaterialCardView = view.findViewById(R.id.card_main_stack_pick)
    private var cardPickContent: TextView = view.findViewById(R.id.card_main_stack_pick_content)
    private var nopeContainer: FrameLayout = view.findViewById(R.id.card_main_stack_nope_container)
    private var cardNope: MaterialCardView = view.findViewById(R.id.card_main_stack_nope)
    private var cardNopeContent: TextView = view.findViewById(R.id.card_main_stack_nope_content)

    fun setPickTransparency(ratio: Float) {
        pickContainer.setBackgroundColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio / 2))
        cardPick.strokeColor = ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio)
        cardPickContent.setTextColor(ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio))
    }

    fun setNopeTransparency(ratio: Float) {
        nopeContainer.setBackgroundColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio / 2))
        cardNope.strokeColor = ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio)
        cardNopeContent.setTextColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio))
    }

    override fun adapt(content: BaseCardStackContent) {
        if (content !is CardStackContent) {
            error("Assertion failed")
        }

        val cardStackItemContents = content.contents.mapTo(ArrayList()) { it.selectable.getString(it.selected) }
        if (content.description.isNotBlank()) {
            cardStackItemContents.add(content.description)
        }
        cardStackItemContents.add(content.userName + ", " + content.jsonObject.getInt("age"))
        imageView.setImage(content)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        recyclerView.adapter = CardStackContentAdapter(cardStackItemContents)
        setPickTransparency(0.0f)
        setNopeTransparency(0.0f)
    }
}