package mbtinder.lib.util

import mbtinder.lib.component.IDContent
import java.util.*
import kotlin.collections.ArrayList

class IDList<E: IDContent>: ArrayList<E>() {
    fun indexOf(uuid: UUID) = (0 until size).firstOrNull { get(it).getUUID() == uuid } ?: -1

    operator fun get(uuid: UUID) = super.get(indexOf(uuid))

    operator fun contains(uuid: UUID) = indexOf(uuid) != -1

    override fun add(element: E) = sync(this) { super.add(element) }

    override fun removeAt(index: Int) = sync(this) { super.removeAt(index) }

    override fun remove(element: E): Boolean = sync(this) { super.remove(element) }

    fun remove(uuid: UUID) = sync(this) { super.removeAt(indexOf(uuid)) }
}