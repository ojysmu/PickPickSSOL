package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.CardStackContent

class CardStackViewHolder(private val view: View): AdaptableViewHolder<CardStackContent>(view) {
    var imageView: AsyncImageView = view.findViewById(R.id.card_main_stack_image)
    var recyclerView: RecyclerView = view.findViewById(R.id.card_main_stack_recycler_view)
    var cardPick: MaterialCardView = view.findViewById(R.id.card_main_stack_pick)
    var cardPickContent: TextView = view.findViewById(R.id.card_main_stack_pick_content)
    var cardNope: MaterialCardView = view.findViewById(R.id.card_main_stack_nope)
    var cardNopeContent: TextView = view.findViewById(R.id.card_main_stack_nope_content)

    fun setPickTransparency(ratio: Float) {
        cardPick.strokeColor = ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio)
        cardPickContent.setTextColor(ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio))
    }

    fun setNopeTransparency(ratio: Float) {
        cardNope.strokeColor = ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio)
        cardNopeContent.setTextColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio))
    }

    override fun adapt(content: CardStackContent) {
        imageView.setImageDrawable(ImageUtil.byteArrayToDrawable(itemView.context, content.getImage()))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        recyclerView.adapter = CardStackContentAdapter(content.contents.toMutableList())
        setPickTransparency(0.0f)
        setNopeTransparency(0.0f)
    }
}