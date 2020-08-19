package mbtinder.android.ui.model.recycler_view

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class ViewHolder(itemView: View, isRecyclable: Boolean = true) : RecyclerView.ViewHolder(itemView) {
    protected val context: Context = itemView.context

    init {
        setIsRecyclable(isRecyclable)
    }
}