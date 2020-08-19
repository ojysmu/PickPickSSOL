package mbtinder.lib.util

fun <T> Collection<T>.findAll(predicate: (T) -> Boolean): ArrayList<T> {
    val asList = arrayListOf<T>()
    for (element in this) {
        if (predicate(element)) {
            asList.add(element)
        }
    }

    return asList
}