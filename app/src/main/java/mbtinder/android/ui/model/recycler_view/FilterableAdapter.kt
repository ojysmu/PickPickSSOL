package mbtinder.android.ui.model.recycler_view

import android.widget.Filter
import android.widget.Filterable
import mbtinder.android.util.Log
import mbtinder.lib.component.IDContent
import mbtinder.lib.util.IDList
import java.util.*
import kotlin.reflect.KMutableProperty1

abstract class FilterableAdapter<T: IDContent>(rootView: Int, contents: IDList<T>, clazz: Class<out AdaptableViewHolder<T>>) :
    Adapter<T>(rootView, contents, clazz), Filterable {

    private val unfiltered = IDList(contents)
    private var filtered = IDList(contents)
    var filterBy: KMutableProperty1<T, String>? = null

    override fun onBindViewHolder(holder: AdaptableViewHolder<T>, position: Int) {
        holder.bind(filtered[position])
    }

    override fun getItemCount() = filtered.size

    fun getContent(uuid: UUID) = unfiltered.find { it.getUUID() == uuid }

    fun <R: Comparable<R>> addContent(element: T, sortBy: ((T) -> R?)?) {
        val index = unfiltered.indexOf(element.getUUID())
        if (index != -1) {
            unfiltered[index] = element
            sortBy?.let { unfiltered.sortBy(it); unfiltered.reverse() }
            if (unfiltered.size == filtered.size) {
                filtered[index] = element
                sortBy?.let { filtered.sortBy(it); filtered.reverse() }
                notifyDataSetChanged()
            }
        } else {
            unfiltered.add(0, element)
            filtered.add(0, element)
            if (filtered.size == unfiltered.size) {
                notifyItemInserted(0)
            }
        }
    }

    fun removeContent(index: Int): T {
        val removed = unfiltered.removeAt(index)
        if (unfiltered.size == filtered.size - 1) {
            filtered.removeAt(index)
            notifyItemRemoved(index)
        }
        return removed
    }

    fun getFilteredList(): List<T> = filtered

    override fun getItemId(position: Int) = filtered[position].getUUID().mostSignificantBits and Long.MAX_VALUE

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                if (constraint.isBlank()) {
                    Log.v("Filterable.performFiltering(): filtered=${unfiltered.map { content: T -> filterBy?.get(content) }}")
                    return FilterResults().apply { values = unfiltered }
                }

                filterBy?.let {
                    filtered = unfiltered.filterTo(IDList()) { element -> it.get(element).contains(constraint) }
                    Log.v("Filterable.performFiltering(): filtered=${filtered.map { content: T -> it.get(content) }}")
                    return FilterResults().apply { values = filtered }
                } ?: return FilterResults().apply { values = unfiltered }
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filtered = results.values as IDList<T>
                notifyDataSetChanged()
            }
        }
    }
}