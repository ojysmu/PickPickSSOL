package mbtinder.android.ui.model.recycler_view

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class AdaptableViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun adapt(content: T)
}