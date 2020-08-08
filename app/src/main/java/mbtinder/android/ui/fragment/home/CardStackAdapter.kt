package mbtinder.android.ui.fragment.home

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.CardStackContent

class CardStackAdapter(private val contents: MutableList<CardStackContent>, private val fragment: HomeFragment): Adapter<CardStackContent>(
    R.layout.card_main_stack,
    contents,
    CardStackViewHolder::class.java) {

    val holders = arrayListOf<CardStackViewHolder>()

    override fun onBindViewHolder(holder: AdaptableViewHolder<CardStackContent>, position: Int) {
        holders.add(holder as CardStackViewHolder)
        holder.adapt(contents[position])
        holder.adapter = this
    }

    fun requestUpdate(onUpdateFinished: (isSucceed: Boolean, content: CardStackContent?) -> Unit) {
        fragment.updateCardStack(onUpdateFinished)
    }
}