package mbtinder.android.ui.fragment.home

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.Adapter
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.CardStackContent


class CardStackViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
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
}

class CardStackAdapter(private val contents: MutableList<CardStackContent>): Adapter<CardStackContent>(
    R.layout.card_main_stack,
    contents,
    CardStackViewHolder::class.java) {

    val holders = arrayListOf<CardStackViewHolder>()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is CardStackViewHolder) {
            error("Assertion failed")
        }

        holders.add(holder)

        val content = contents[position]
        holder.imageView.setImageDrawable(ImageUtil.byteArrayToDrawable(context, content.getImage()))
        holder.recyclerView.itemAnimator = DefaultItemAnimator()
        holder.recyclerView.layoutManager = LinearLayoutManager(context)
        holder.recyclerView.adapter = CardStackContentAdapter(content.contents.toMutableList())
        holder.setPickTransparency(0.0f)
        holder.setNopeTransparency(0.0f)
    }
}