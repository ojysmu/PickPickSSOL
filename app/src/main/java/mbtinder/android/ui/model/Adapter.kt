package mbtinder.android.ui.model

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

abstract class Adapter<T>(@LayoutRes private val rootView: Int,
                          private var contents: MutableList<T>,
                          private val clazz: Class<out RecyclerView.ViewHolder>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(rootView, parent, false)

        return clazz.getDeclaredConstructor(View::class.java).newInstance(view) as RecyclerView.ViewHolder
    }

    override fun getItemCount(): Int {
        return contents.size
    }

    fun addContent(element: T) {
        contents.add(element)
    }

    fun updateContents(content: MutableList<T>) {
        this.contents = content
        notifyDataSetChanged()
    }
}