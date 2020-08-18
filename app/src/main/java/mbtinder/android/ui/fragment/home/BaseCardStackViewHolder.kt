package mbtinder.android.ui.fragment.home

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import mbtinder.lib.component.card_stack.BaseCardStackContent

abstract class BaseCardStackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun adapt(content: BaseCardStackContent)
}