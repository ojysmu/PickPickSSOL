package mbtinder.android.ui.model.fragment

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children

abstract class ViewHolder(val rootView: ViewGroup) {
    val views: HashMap<Int, View> = HashMap()

    init {
        rootView.children.forEach { views[it.id] = rootView.findViewById(it.id) }
    }

    abstract fun adapt(content: FragmentContent)
}