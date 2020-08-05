package mbtinder.android.ui.fragment.home

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter

class CardStackContentAdapter(private val contents: MutableList<String>)
    : Adapter<String>(R.layout.card_main_stack_content, contents, CardStackContentViewHolder::class.java) {

    override fun onBindViewHolder(holder: AdaptableViewHolder<String>, position: Int) {
        holder.adapt(contents[position])
    }
}