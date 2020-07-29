package mbtinder.lib.util

import mbtinder.lib.component.IDContent
import java.util.*

class IDList<E: IDContent>: ArrayList<E>() {
    fun indexOf(uuid: UUID) = (0 until size).firstOrNull { get(it).getUUID() == uuid } ?: -1

    operator fun get(uuid: UUID) = super.get(indexOf(uuid))

    operator fun contains(uuid: UUID) = indexOf(uuid) != -1

    override fun add(element: E) = synchronized(this) { super.add(element) }

    override fun removeAt(index: Int) = synchronized(this) { super.removeAt(index) }

    override fun remove(element: E): Boolean = synchronized(this) { return super.remove(element) }

    fun remove(uuid: UUID) = synchronized(this) { removeAt(indexOf(uuid)) }
}