package mbtinder.lib.util

import mbtinder.lib.component.CloneableContent

class ImmutableList<E: CloneableContent<E>>: ArrayList<E>, CloneableContent<ImmutableList<E>> {
    constructor(): super()

    constructor(collection: Collection<E>): super(collection)

    override fun get(index: Int) = super.get(index).getCloned()

    override fun clone() = ImmutableList(this)

    override fun getCloned() = ImmutableList(this)
}

fun <T: CloneableContent<T>> Collection<T>.toImmutableList(): ImmutableList<T> {
    return ImmutableList(this)
}