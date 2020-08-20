package mbtinder.android.ui.fragment.sign_up

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.user.MBTIContent

class MBTIAdapter(private val contents: MutableList<MBTIContent>)
    : Adapter<MBTIContent>(R.layout.card_sign_up5_mbti, contents, MBTIViewHolder::class.java) {
    private val holders = ArrayList<MBTIViewHolder>()

    override fun onBindViewHolder(holder: AdaptableViewHolder<MBTIContent>, position: Int) {
        holders.add(holder as MBTIViewHolder)
        holder.adapter = this
        holder.bind(contents[position])
    }

    fun uncheckAll() = holders.forEach { it.uncheck() }

    fun getCheckedIndex() = contents.first { it.isChecked }
}