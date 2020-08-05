package mbtinder.android.ui.fragment.sign_up

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.MBTIContent

class MBTIAdapter(private val contents: MutableList<MBTIContent>)
    : Adapter<MBTIContent>(R.layout.card_sign_up4_mbti, contents, MBTIViewHolder::class.java) {

    override fun onBindViewHolder(holder: AdaptableViewHolder<MBTIContent>, position: Int) {
        holder.adapt(contents[position])
    }
}