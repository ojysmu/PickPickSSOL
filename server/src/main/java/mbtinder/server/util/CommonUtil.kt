package mbtinder.server.util

fun <T> findAllTwice(collection: Collection<T>, update: () -> Collection<T>, comparison: (v: T) -> Boolean)
        = collection.find { comparison.invoke(it) } ?: update.invoke().find(comparison)

fun <T> findBinaryTwice(list: List<T>, update: () -> List<T>, compare: (T) -> Int): T? {
    println("findBinaryTwice(): list=$list")
    val index = list.binarySearch { compare.invoke(it) }
    println("findBinaryTwice(): index=$index")
    return if (index < 0) {
        val updated = update.invoke()
//        val twice = update.invoke().binarySearch { compare.invoke(it) }
        val twice = updated.binarySearch { compare.invoke(it) }
        println("findBinaryTwice(): twice=$twice")
        if (twice < 0) {
            null
        } else {
            list[twice]
        }
    } else {
        list[index]
    }
}