package mbtinder.android.ui.model.recycler_view

import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Constructor

data class MultiTypeForm<T: MultiTypeContent, VH: RecyclerView.ViewHolder>(val c: Class<out T>, val vh: Class<out VH>, val constructor: Constructor<VH>? = null, val bind: (VH, T, Int) -> Unit)