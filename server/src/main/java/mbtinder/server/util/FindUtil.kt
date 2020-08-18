package mbtinder.server.util

import mbtinder.lib.component.CloneableContent
import mbtinder.lib.util.ImmutableList

fun <T: CloneableContent<T>> findAllTwice(immutableList: ImmutableList<T>, update: () -> Collection<T>, comparison: (v: T) -> Boolean)
        = immutableList.find { comparison.invoke(it) } ?: update.invoke().find(comparison)

fun <T: CloneableContent<T>> findBinaryTwice(immutableList: ImmutableList<T>, compare: (v: T) -> Int): T? {
    val index = immutableList.binarySearch { compare.invoke(it) }
    return if (index < 0) {
        val twice = immutableList.binarySearch { compare.invoke(it) }
        if (twice < 0) {
            null
        } else {
            immutableList[twice]
        }
    } else {
        immutableList[index]
    }
}