package mbtinder.server.util

fun <T> findAllTwice(collection: Collection<T>, update: () -> Collection<T>, comparison: (v: T) -> Boolean)
        = collection.find { comparison.invoke(it) } ?: update.invoke().find(comparison)

fun <T> findBinaryTwice(list: List<T>, update: () -> List<T>, compare: (T) -> Int): T? {
    val index = list.binarySearch { compare.invoke(it) }
    return if (index < 0) {
        val twice = update.invoke().binarySearch { compare.invoke(it) }
        if (twice < 0) {
            null
        } else {
            list[twice]
        }
    } else {
        list[index]
    }
}