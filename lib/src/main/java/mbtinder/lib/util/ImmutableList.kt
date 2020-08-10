package mbtinder.lib.util

import mbtinder.lib.component.CloneableContent

class ImmutableList<E: CloneableContent<E>>: ArrayList<E>, CloneableContent<ImmutableList<E>> {
    constructor(): super()

    constructor(collection: Collection<E>): super(collection)

    override fun get(index: Int) = super.get(index).getCloned()

    private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
        when {
            fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
            fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
            toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
        }
    }

    fun find(predicate: (E) -> Boolean): E? {
        for (element in this) {
            if (predicate(element)) {
                return element.getCloned()
            }
        }

        return null
    }

    fun binarySearch(fromIndex: Int = 0, toIndex: Int = size, comparison: (E) -> Int): Int {
        rangeCheck(size, fromIndex, toIndex)

        var low = fromIndex
        var high = toIndex - 1

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midVal = get(mid)
            val cmp = comparison(midVal)

            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return mid
            }
        }
        return -(low + 1)
    }

    override fun clone() = ImmutableList(this)

    override fun getCloned() = ImmutableList(this)
}

fun <T: CloneableContent<T>> Collection<T>.toImmutableList(): ImmutableList<T> {
    return ImmutableList(this)
}