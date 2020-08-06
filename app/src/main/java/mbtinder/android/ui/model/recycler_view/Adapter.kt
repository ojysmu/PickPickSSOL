package mbtinder.android.ui.model.recycler_view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.lang.reflect.Constructor

abstract class Adapter<T>(@LayoutRes private val rootView: Int,
                          private var contents: MutableList<T>,
                          private val clazz: Class<out AdaptableViewHolder<T>>): RecyclerView.Adapter<AdaptableViewHolder<T>>() {
    protected lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdaptableViewHolder<T> {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(rootView, parent, false)

        return clazz.getDeclaredConstructor(View::class.java).newInstance(view)
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    fun addContent(element: T) {
        contents.add(element)
        notifyItemInserted(contents.size - 1)
    }

    fun addContents(elements: Collection<T>) {
        contents.addAll(elements)
        notifyItemRangeChanged(contents.size - elements.size - 1, elements.size)
    }

    fun removeAt(index: Int) {
        contents.removeAt(index)
        notifyItemRemoved(index)
    }

    fun updateContents(content: MutableList<T>) {
        this.contents = content
        notifyDataSetChanged()
    }
}