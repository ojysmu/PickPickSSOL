package mbtinder.lib.util

import mbtinder.lib.component.IDContent
import java.util.*
import kotlin.collections.ArrayList

class IDList<E: IDContent>: ArrayList<E>, IDContent {
    var uuid: UUID = UUID.randomUUID()

    constructor(): super()

    constructor(collection: Collection<E>): super(collection)

    fun indexOf(uuid: UUID): Int {
        return sync(this) {
            for (element in withIndex()) {
                if (element.value.getUUID() == uuid) {
                    return@sync element.index
                }
            }

            return@sync -1
        }
    }

    operator fun get(uuid: UUID) = super.get(indexOf(uuid))

    operator fun set(uuid: UUID, element: E) = sync(this) { super.set(indexOf(uuid), element) }

    operator fun contains(uuid: UUID) = indexOf(uuid) != -1

    override fun add(element: E) = sync(this) { super.add(element) }

    override fun removeAt(index: Int) = sync(this) { super.removeAt(index) }

    override fun remove(element: E): Boolean = sync(this) { super.remove(element) }

    fun remove(uuid: UUID) = sync(this) { super.removeAt(indexOf(uuid)) }

    fun getIds() = map { it.getUUID() }

    override fun getUUID(): UUID = uuid
}

fun <E: IDContent> Collection<E>.toIDList() = IDList(this)

fun <E: IDContent> idListOf(vararg elements: E) = IDList(elements.toList())